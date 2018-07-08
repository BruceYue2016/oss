package com.jinghan.backend.soa.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruce
 * @date 2017/9/28
 */
public class NodeTree implements Serializable {

    public static final String OPT_REGISTER = "register";
    public static final String OPT_UNREGISTER = "unregister";

    private String nodeName;

    private String defaultProvider;

    private boolean register; // true-接入; false-移除

    private List<String> alternativeProviders;

    private List<NodeTree> children; // 子节点

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public List<String> getAlternativeProviders() {
        return alternativeProviders;
    }

    public void setAlternativeProviders(List<String> alternativeProviders) {
        this.alternativeProviders = alternativeProviders;
    }

    public List<NodeTree> getChildren() {
        return children;
    }

    public void setChildren(List<NodeTree> children) {
        this.children = children;
    }
}
