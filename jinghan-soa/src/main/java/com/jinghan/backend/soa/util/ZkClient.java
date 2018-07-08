package com.jinghan.backend.soa.util;

import com.jinghan.backend.soa.vo.NodeTree;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.BeanUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bruce
 * @date 2017/9/25
 */
public class ZkClient implements Watcher {

    private static final int SESSION_TIME_OUT = 50000;

    public static final Pattern pattern = Pattern.compile("(192.168.2.\\d{1,3})");

    private static final CountDownLatch CONNECTED_SEMAPHORE = new CountDownLatch(1);

    private static ZkClient instance;

    private ZkClient() {
    }

    public static ZkClient getInstance(){
        if(instance == null){
           synchronized (CONNECTED_SEMAPHORE){
               instance = new ZkClient();
           }
        }
        return instance;
    }

    /**
     * 获取ZK服务节点树
     * @param remoteZkConnection
     * @param proxyZkConnection
     * @param localHost
     * @return
     * @throws Exception
     */
    public List<NodeTree> build(String remoteZkConnection, String proxyZkConnection, String localHost) throws Exception {
        if(StringUtils.isEmpty(remoteZkConnection) || StringUtils.isEmpty(proxyZkConnection) || StringUtils.isEmpty(localHost)) return null;

        ZooKeeper remoteZk = null, proxyZk = null;
        try {
            remoteZk = new ZooKeeper(remoteZkConnection, SESSION_TIME_OUT, new ZkClient());
            proxyZk = new ZooKeeper(proxyZkConnection, SESSION_TIME_OUT, new ZkClient());

            CONNECTED_SEMAPHORE.await();

            String remoteHost = remoteZkConnection.split(":")[0];
            List<NodeTree> remoteNodeTree = this.buildNodeTree(remoteZk, localHost, remoteHost, false);
            List<NodeTree> proxyNodeTree = this.buildNodeTree(proxyZk, localHost, remoteHost, true);

            return this.mergeNodeTree(remoteNodeTree, proxyNodeTree);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if(remoteZk != null){
                remoteZk.close();
            }
            if(proxyZk != null){
                proxyZk.close();
            }
        }
    }

    /**
     * 构建Zk节点树
     * @param zk
     * @param localHost
     * @param remoteHost
     * @param isProxyZk
     * @return
     * @throws Exception
     */
    private List<NodeTree> buildNodeTree(ZooKeeper zk, String localHost, String remoteHost, Boolean isProxyZk) throws Exception {
        // 按 Application 分类 Service
        TreeMap<String, List<String>> appServiceMap = this.getAppServices(zk);
        if(appServiceMap == null || appServiceMap.isEmpty()){
            return null;
        }

        // Application
        List<NodeTree> appNodes = new ArrayList<>();
        for(Map.Entry<String, List<String>> appService : appServiceMap.entrySet()){
            // Service
            String appName = appService.getKey();
            List<String> services = appService.getValue();
            if(CollectionUtils.isNotEmpty(services)){
                int localServiceCount = 0, remoteDefaultServiceCount = 0, remoteOtherServiceCount = 0;
                List<NodeTree> serviceNodes = new ArrayList<>();
                List<String> appAlternativeProviders = new ArrayList<>();
                // 统计远程ZK其他主机提供服务的个数
                Map<String, Integer> serviceProviderCountMap = new HashMap<>();
                for(String serviceName : services){
                    String remoteOtherProvider = null;
                    boolean hasRemoteDefaultProvider = false, hasRemoteOtherProvider = false, hasLocalProvider = false;
                    List<String> serviceAlternativeProviders = new ArrayList<>();
                    List<String> providers = zk.getChildren("/dubbo/" + serviceName + "/providers", true);
                    if(CollectionUtils.isNotEmpty(providers)){
                        // Provider
                        for(String provider : providers){
                            provider = URLDecoder.decode(provider, "UTF-8");
                            Matcher matcher = pattern.matcher(provider);
                            if(matcher.find()){
                                String ip = matcher.group();
                                if(!serviceAlternativeProviders.contains(ip)){
                                    serviceAlternativeProviders.add(ip);
                                }
                                if(!appAlternativeProviders.contains(ip)){
                                    appAlternativeProviders.add(ip);
                                }
                                if(isProxyZk){
                                    if(ip.equals(remoteHost)){
                                        hasRemoteDefaultProvider = true;
                                    }else if(ip.equals(localHost)){
                                        hasLocalProvider = true;
                                    }else{
                                        hasRemoteOtherProvider = true;
                                        remoteOtherProvider = ip;
                                        serviceProviderCountMap.merge(ip, 1, (a, b) -> a + b);
                                    }
                                }
                            }
                        }
                    }

                    // 判断默认服务是否接入
                    String defaultProvider = null;
                    boolean register = false;
                    if(isProxyZk){
                        if(hasRemoteDefaultProvider){
                            defaultProvider = remoteHost;
                            register = true;
                            remoteDefaultServiceCount++;
                        }
                        if(hasRemoteOtherProvider){
                            defaultProvider = remoteOtherProvider;
                            register = true;
                            remoteOtherServiceCount++;
                        }
                        if(hasLocalProvider){
                            defaultProvider = localHost;
                            register = true;
                            localServiceCount++;
                        }
                    }else{
                        defaultProvider = remoteHost;
                    }
                    if(defaultProvider == null){
                        // 若代理zk服务器上既没有本地服务也没有接入远程服务，就任取一个远程服务
                        if(CollectionUtils.isNotEmpty(serviceAlternativeProviders)){
                            defaultProvider = serviceAlternativeProviders.get(0);
                        }
                    }

                    // 备选服务排除默认服务
                    serviceAlternativeProviders.remove(defaultProvider);

                    // 构建service节点
                    NodeTree serviceNode = new NodeTree();
                    serviceNode.setNodeName(serviceName);
                    serviceNode.setRegister(register);
                    serviceNode.setDefaultProvider(defaultProvider);
                    serviceNode.setAlternativeProviders(serviceAlternativeProviders);

                    serviceNodes.add(serviceNode);
                }

                // 构建app节点
                NodeTree appNode = new NodeTree();
                appNode.setNodeName(appName);
                appNode.setChildren(serviceNodes);

                // 获取远程ZK提供服务最多者的其他主机地址
                final String[] remoteOtherProviderCount = {"", "0"};
                if(!serviceProviderCountMap.isEmpty()){
                    serviceProviderCountMap.forEach((String ip, Integer times) -> {
                        int count = Integer.parseInt(remoteOtherProviderCount[1]);
                        if(times > count){
                            remoteOtherProviderCount[0] = ip;
                            count = times;
                            remoteOtherProviderCount[1] = count + "";
                        }
                    });
                }

                // 根据服务提供数量排序，选出提供服务数量最多者
                int[] providerSourceCounts = new int[]{remoteDefaultServiceCount, remoteOtherServiceCount, localServiceCount};
                Arrays.sort(providerSourceCounts);
                int mostSourceCount = providerSourceCounts[2];
                if(remoteDefaultServiceCount == mostSourceCount){
                    // 主要为远程默认服务
                    appAlternativeProviders.remove(remoteHost);
                    appNode.setDefaultProvider(remoteHost);
                    appNode.setAlternativeProviders(appAlternativeProviders);
                    appNode.setRegister(remoteDefaultServiceCount != 0);
                }else if(remoteOtherServiceCount == mostSourceCount){
                    // 主要为远程其他服务
                    appAlternativeProviders.remove(remoteOtherProviderCount[0]);
                    appNode.setDefaultProvider(remoteOtherProviderCount[0]);
                    appNode.setAlternativeProviders(appAlternativeProviders);
                    appNode.setRegister(Integer.parseInt(remoteOtherProviderCount[1]) != 0);
                }else if(localServiceCount == mostSourceCount){
                    // 主要为本地服务
                    appAlternativeProviders.remove(localHost);
                    appNode.setDefaultProvider(localHost);
                    appNode.setAlternativeProviders(appAlternativeProviders);
                    appNode.setRegister(true);
                }

                appNodes.add(appNode);
            }
        }

        return appNodes;
    }

    /**
     * 合并Zk节点树
     * @param remoteNodeTree
     * @param proxyNodeTree
     * @return
     */
    private List<NodeTree> mergeNodeTree(List<NodeTree> remoteNodeTree, List<NodeTree> proxyNodeTree){
        if(CollectionUtils.isEmpty(remoteNodeTree)){
            return proxyNodeTree;
        }
        if(CollectionUtils.isEmpty(proxyNodeTree)){
            return remoteNodeTree;
        }

        remoteNodeTree.forEach(remote -> proxyNodeTree.forEach(proxy -> {
            if(remote.getNodeName().equals(proxy.getNodeName())){
                remote.getChildren().forEach(remoteSP -> proxy.getChildren().forEach(proxySP -> {
                    if(remoteSP.getNodeName().equals(proxySP.getNodeName())){
                        // 服务去重
                        proxySP.getAlternativeProviders().removeAll(remoteSP.getAlternativeProviders());
                        proxySP.getAlternativeProviders().addAll(remoteSP.getAlternativeProviders());
                        proxySP.getAlternativeProviders().add(remoteSP.getDefaultProvider());
                        proxySP.getAlternativeProviders().remove(proxySP.getDefaultProvider());
                    }
                }));
                // 应用去重
                proxy.getAlternativeProviders().removeAll(remote.getAlternativeProviders());
                proxy.getAlternativeProviders().addAll(remote.getAlternativeProviders());
                proxy.getAlternativeProviders().add(remote.getDefaultProvider());
                proxy.getAlternativeProviders().remove(proxy.getDefaultProvider());
                BeanUtils.copyProperties(proxy, remote);
            }
        }));

        return remoteNodeTree;
    }

    /**
     * 按 Application 分类 Service
     * @param zk
     * @return
     * @throws Exception
     */
    private TreeMap<String, List<String>> getAppServices(ZooKeeper zk) throws Exception {
        TreeMap<String, List<String>> appServiceMap = null;
        Stat stat = zk.exists("/", true);
        if(stat != null){
            stat = zk.exists("/dubbo", true);
            if(stat != null){
                List<String> serviceChildren = zk.getChildren("/dubbo", true);
                if(CollectionUtils.isNotEmpty(serviceChildren)){
                    appServiceMap = new TreeMap<>();
                    for(String service : serviceChildren){
                        // com.jinghan.backend.*
                        String appName = service.split("\\.")[3];
                        if("monitor".equals(appName)){
                            continue;
                        }
                        List<String> services = appServiceMap.get(appName);
                        if(services == null){
                            services = new ArrayList<>();
                        }

                        services.add(service);
                        appServiceMap.put(appName, services);
                    }
                }
            }
        }

        return appServiceMap;
    }

    /**
     * 接入Zk应用或服务
     * @param remoteZkConnection
     * @param proxyZkConnection
     * @param appName
     * @param serviceName
     * @param defaultProviderHost
     * @return
     * @throws Exception
     */
    public Boolean registerApp(String remoteZkConnection, String proxyZkConnection, String appName,
                               String serviceName, String defaultProviderHost) throws Exception {
        if(StringUtils.isEmpty(remoteZkConnection) || StringUtils.isEmpty(proxyZkConnection)) return Boolean.FALSE;

        ZooKeeper remoteZk = null, proxyZk = null;
        try {
            remoteZk = new ZooKeeper(remoteZkConnection, SESSION_TIME_OUT, new ZkClient());
            proxyZk = new ZooKeeper(proxyZkConnection, SESSION_TIME_OUT, new ZkClient());

            CONNECTED_SEMAPHORE.await();

            if(StringUtils.isNotEmpty(appName)){
                // 按 Application 分类 Service
                TreeMap<String, List<String>> remoteAppServiceMap = getAppServices(remoteZk);
                List<String> services = remoteAppServiceMap.get(appName);
                if(CollectionUtils.isNotEmpty(services)){
                    for(String service : services){
                        Boolean registerFlag = this.registerService(remoteZk, proxyZk, service, defaultProviderHost);
                        if(!registerFlag){
                            return registerFlag;
                        }
                    }
                }
            }else{
                if(StringUtils.isNotEmpty(serviceName) && StringUtils.isNotEmpty(defaultProviderHost)){
                    return this.registerService(remoteZk, proxyZk, serviceName, defaultProviderHost);
                }
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }finally {
            if(remoteZk != null){
                remoteZk.close();
            }
            if(proxyZk != null){
                proxyZk.close();
            }
        }

        return Boolean.TRUE;
    }

    /**
     * 接入ZK服务节点
     * @param remoteZk
     * @param proxyZk
     * @param serviceName
     * @param defaultProviderHost
     * @return
     */
    private Boolean registerService(ZooKeeper remoteZk, ZooKeeper proxyZk, String serviceName, String defaultProviderHost) {
        try {
            List<String> remoteProviders = remoteZk.getChildren("/dubbo/" + serviceName +  "/providers", true);
            if(CollectionUtils.isEmpty(remoteProviders)) return Boolean.FALSE;

            String defaultProvider = null;
            for(String provider : remoteProviders){
                if(provider.contains(defaultProviderHost)){
                    defaultProvider = provider;
                    break;
                }
            }
            // 如果远程ZK服务器没有本地注册的服务，就取该ZK服务器注册的任意其他服务
            if(defaultProvider == null){
                defaultProvider = remoteProviders.get(0);
            }

            String providerPath = "/dubbo";
            Boolean nodeStat = this.createOrReplaceNode(proxyZk, providerPath, false);
            if(nodeStat){
                providerPath += "/" + serviceName;
                nodeStat = this.createOrReplaceNode(proxyZk, providerPath, false);
                if(nodeStat){
                    providerPath += "/providers";
                    nodeStat = this.createOrReplaceNode(proxyZk, providerPath, true);
                    if(nodeStat){
                        providerPath += "/" + defaultProvider;
                        Stat stat = proxyZk.exists(providerPath, true);
                        if(stat != null){
                            proxyZk.delete(providerPath, -1);
                        }
                        // 接入新的服务
                        String path = proxyZk.create(providerPath, defaultProvider.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        if(StringUtils.isEmpty(path)){
                            return Boolean.FALSE;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    /**
     * 移除ZK应用或服务
     * @param zkConnection
     * @param appName
     * @param serviceName
     * @param defaultProviderHost
     * @return
     * @throws Exception
     */
    public Boolean unRegisterApp(String zkConnection, String appName, String serviceName, String defaultProviderHost) throws Exception {
        if(StringUtils.isEmpty(zkConnection)) return Boolean.FALSE;

        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(zkConnection, SESSION_TIME_OUT, new ZkClient());
            CONNECTED_SEMAPHORE.await();

            if(StringUtils.isNotEmpty(appName)){
                // 按 Application 分类 Service
                TreeMap<String, List<String>> appServiceMap = getAppServices(zk);

                List<String> services = appServiceMap.get(appName);
                if(CollectionUtils.isNotEmpty(services)){
                    for(String appService : services){
                        Boolean removeZkNodeFlag = this.unRegisterService(zk, appService, defaultProviderHost);
                        if(!removeZkNodeFlag){
                            return removeZkNodeFlag;
                        }
                    }
                }
            }else{
                if(StringUtils.isNotEmpty(serviceName)){
                    return this.unRegisterService(zk, serviceName, defaultProviderHost);
                }
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }finally {
           if(zk != null){
               zk.close();
           }
        }

        return Boolean.TRUE;
    }

    /**
     * 删除zk服务节点
     * @param zk
     * @param serviceName
     * @param defaultProviderHost
     * @return
     */
    private Boolean unRegisterService(ZooKeeper zk, String serviceName, String defaultProviderHost) {
        try {
            List<String> providers = zk.getChildren("/dubbo/" + serviceName + "/providers", true);
            if(CollectionUtils.isNotEmpty(providers)){
                for(String provider : providers){
                    String path = "/dubbo/" + serviceName + "/providers/" + provider;
                    Stat stat = zk.exists(path, true);
                    if(stat != null){
                        String decodePath = URLDecoder.decode(path, "UTF-8");
                        if(decodePath.contains("dubbo://" + defaultProviderHost)){
                            zk.delete(path, -1);
                        }
                    }
                }
            }
        } catch (Exception e) {
           return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    /**
     * 创建或替换节点
     * @param zk
     * @param nodePath
     * @param isReplace
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private Boolean createOrReplaceNode(ZooKeeper zk, String nodePath, boolean isReplace) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(nodePath, true);
        if(stat == null){
            // 创建父节点
            String path = zk.create(nodePath, nodePath.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            if(StringUtils.isEmpty(path)){
                return Boolean.FALSE;
            }
        }else{
            if(isReplace){
                List<String> providers = zk.getChildren(nodePath, true);
                if(CollectionUtils.isNotEmpty(providers)){
                    String providerList = "";
                    for(int i = 0; i < providers.size(); i++){
                        if(i == 0){
                            providerList = providers.get(i);
                        }else{
                            providerList = providerList + "," + providers.get(i);
                        }
                    }
                    String path = nodePath + "/" + providerList;
                    stat = zk.exists(path, true);
                    if(stat != null){
                        // 踢除旧的服务
                        zk.delete(path, -1);
                    }
                }
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            CONNECTED_SEMAPHORE.countDown();
        }
    }

    /**
     * PERSISTENT (0, false, false)
     * 持久节点：节点创建后，会一直存在，不会因客户端会话失效而删除；
     *
     * PERSISTENT_SEQUENTIAL (2, false, true),
     * 持久顺序节点：基本特性与持久节点一致，创建节点的过程中，zookeeper会在其名字后自动追加一个单调增长的数字后缀，作为新的节点名；
     *
     * EPHEMERAL
     * 临时节点：客户端会话失效或连接关闭后，该节点会被自动删除，且不能再临时节点下面创建子节点，否则报如下错
     * org.apache.zookeeper.KeeperException$NoChildrenForEphemeralsException: KeeperErrorCode = NoChildrenForEphemerals for /node/child
     *
     * EPHEMERAL_SEQUENTIAL
     * 临时顺序节点：基本特性与临时节点一致，创建节点的过程中，zookeeper会在其名字后自动追加一个单调增长的数字后缀，作为新的节点名；
     *
     * ZOO_OPEN_ACL_UNSAFE使所有ACL都“开放”了：任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
     * @link http://ifeve.com/zookeeper-access-control-using-acls/
     *
     * dubbo://192.168.2.6:29067/com.jinghan.backend.mcnt.service.dubbo.MerchantDubboService?anyhost=true&application=merchant_provider&dubbo=2.4.10&interface=com.jinghan.backend.mcnt.service.dubbo.MerchantDubboService&methods=queryMerchantPayAccountByMerchantId,deleteMealPeriods,deleteExtraCharge,putMerchantRegisterToMap,insertOrUpdateMealPeriodInfo,checkAccountIsHasMessageAmount,deleteVideo,queryMerchantInfoByName,queryMerchantDiscountById,updateTradeInfoOrderStatus,insertMultipart,getAllMerchantIds,getAllMerchantIdsByPId,checkContract,createContract,updateDictionary,getMealPeriodList,queryTradeInfoOrderByTradeCode,updateExtraCharge,queryMerchantInfoByMerchantId,getStoreList,getMerchantForBindingAdd,addVideoCategory,getMerchantIdByBasicInfoId,selectDealtCustomerList,queryMerchantPictureListByObject,insertBossTradeInfoByObject,queryNegativeMerchantCommentsByMerchantId,getMcntBasicInfo,selectListCRMMerchantDictionaries,addContact,updateCustomerBasicInfo,queryNegativeMerchantCommentsCount,addPicture,platformModifyMcnt,selectByPrimaryKey,getCrmReportStatistics,getCrmReportStatisticsByDaily,deleteVideoCategory,getMcntRegister,createQualification,addPictureCategory,obtainCustomer,deleteDictionary,getFinancialStatus,queryMerchantRegisterByIds,queryByName,deleteCrmContact,queryMerchantTableByID,queryMerchantMealPeriodListByObject,queryBossMerchantInfoByMerchant,getPendingMerchants,selectListPageByObject,selectListPageBossTradeInfoByDto,createTransferAccount,isSupportNewLandPayMode,getBrandList,queryTableCount,updateVideo,register,queryInventedAccountByMerchantId,openInventedAccount,queryMerchantTableListByObject,deletePicture,addVideo,addCustomer,updatePicture,selectListPageCRMMerchantByDto,queryMerchantTableIdsByTableAreaId,queryMerchantTableByMerchantId,getCiqSaleFlowList,updateMerchantRegister,getMerchantStockList,selectMultipart,queryMerchantRegisterByMobile,queryMerchantParameterInfoByMerchantId,updateVideoCategory,updateMessageAmountByMerchantId,deletePictureCategory,getOrderCode,queryMerchantObligatoryContentByObject,queryMerchantCommentsByAndOrderCode,getCustomerInfo,getParentMerchantRegisterByMerchantId,insertMessageInfoByObject,addDictionary,queryMappedMcntTableByStatus,getContractStatus,queryMerchantPaymentListByObject,updatePictureCategory,modifyBrandInfo,queryBranchFromMerchantRegisterByMerchant,getCrmBasicInfoByMerchantId,saveBrandInfo,selectListPageBossMessageByDto,queryMerchantBasicInfoByMerchantId,getMcntInfo,getMerchantInfo,queryMerchantDictSettingListByObject,urgeCheck,queryDishesInfoByMerchantId,updateMerchantInfo,addExtraCharge,deleteMultiparts,revokeThirdPayStatus,platformAddMcnt,queryMerchantRegisterById,getAllStoreList,revokeContract,batchDeleteMcnt&pid=23146&retries=0&revision=2.0.0-SNAPSHOT&side=provider&timeout=20000&timestamp=1506142770836
     * @param args
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws Exception {
        String url = "dubbo%3A%2F%2F192.168.2.203%3A29011%2Fcom.jinghan.backend.pymt.service.dubbo.EnterprisePymtDubboService%3Fanyhost%3Dtrue%26application%3Ddubbo_payment_provider%26dubbo%3D2.4.10%26interface%3Dcom.jinghan.backend.pymt.service.dubbo.EnterprisePymtDubboService%26methods%3DwithdrawRewardPayToUser%26pid%3D23416%26retries%3D0%26revision%3D2.0.0-SNAPSHOT%26side%3Dprovider%26timeout%3D20000%26timestamp%3D1507715305300";
        url = URLDecoder.decode(url, "UTF-8");
        System.out.println(url);
    }

}
