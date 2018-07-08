package com.jinghan.backend.soa.controller;

import com.jinghan.backend.soa.util.IpUtil;
import com.jinghan.backend.soa.vo.NodeTree;
import com.jinghan.backend.soa.util.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * http://192.168.2.143:8888/jinghan-soa/soa/dubbo/serviceIndex
 * 135 + 2181 = 2316
 * @author Bruce
 * @date 2017/10/11
 */
@Controller
@RequestMapping("/soa/dubbo")
public class DubboController {

    private Logger LOGGER = Logger.getLogger(this.getClass());

    /**
     * 跳转到服务首页
     * @return
     * @throws Exception
     */
    @RequestMapping("/serviceIndex")
    public String serviceIndex(HttpServletRequest request, ModelMap modelMap) throws Exception {
        // 用户本机IP
        String localHost = request.getRemoteAddr();
        if("127.0.0.1".equals(localHost)){
            localHost = IpUtil.getLocalHostIp();
        }
        if(StringUtils.isNotEmpty(localHost) && localHost.split("\\.").length == 4){
            int mac = Integer.parseInt(localHost.split("\\.")[3]);
            int port = mac + 2181;
            String proxyZk = "192.168.2.143:" + port;

            modelMap.put("proxyZk", proxyZk);
        }

        return "dubbo/service_index";
    }

    /**
     * 加载服务列表
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/serviceList", method = RequestMethod.POST)
    public String serviceList(HttpServletRequest request, ModelMap modelMap) throws Exception {
        // 远程zk服务池
        String remoteZk = request.getParameter("remoteZk");
        // 代理zk服务池
        String proxyZk = request.getParameter("proxyZk");
        // 本地zk服务池
        String localZk = request.getParameter("localZk");

        // 如果采用本地zk,会覆盖代理zk
        if(StringUtils.isNotEmpty(localZk)){
            proxyZk = localZk;
        }

        // 用户本机IP
        String localHost = request.getRemoteAddr();
        LOGGER.info("当前请求主机地址====>" + localHost);

        // 构建服务状态节点树
        List<NodeTree> nodeTrees = ZkClient.getInstance().build(remoteZk, proxyZk, localHost);
        modelMap.put("nodeTrees", nodeTrees);

        modelMap.put("remoteZk", remoteZk);
        modelMap.put("proxyZk", proxyZk);

        return "dubbo/service_list";
    }

    /**
     * 接入或移除应用或服务
     * @param opt  register unregister
     * @param appName
     * @param serviceName
     * @param defaultProviderHost
     * @param remoteZk
     * @param proxyZk
     * @return
     * @throws Exception
     */
    @RequestMapping("/registerApp/{opt}")
    @ResponseBody
    public Boolean registerApp(HttpServletRequest request, @PathVariable String opt,
                               @RequestParam(required = false) String appName,
                               @RequestParam(required = false) String serviceName,
                               @RequestParam(required = false) String defaultProviderHost,
                               @RequestParam String remoteZk, @RequestParam String proxyZk) throws Exception {

        LOGGER.info(String.format("当前操作=====>localHost=%s,opt=%s,appName=%s,serviceName=%s,defaultProviderHost=%s,remoteZk=%s,proxyZk=%s",
                request.getRemoteAddr(), opt, appName, serviceName, defaultProviderHost, remoteZk, proxyZk));

        if(!ZkClient.pattern.matcher(remoteZk).find() || !ZkClient.pattern.matcher(proxyZk).find()){
            return Boolean.FALSE;
        }

        ZkClient zkClient = ZkClient.getInstance();
        if(NodeTree.OPT_REGISTER.equals(opt.toLowerCase())){
            // 接入zk应用或服务
            return zkClient.registerApp(remoteZk, proxyZk, appName, serviceName, defaultProviderHost);
        }else if(NodeTree.OPT_UNREGISTER.equals(opt.toLowerCase())){
            // 移除zk应用或服务
            return zkClient.unRegisterApp(proxyZk, appName, serviceName, defaultProviderHost);
        }else{
            return Boolean.FALSE;
        }
    }

}
