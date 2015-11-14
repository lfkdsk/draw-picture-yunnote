package com.lfk.drawapictiure.Info;

/**
 * Created by liufengkai on 15/9/15.
 */
public class NodeInfo {
    private String nodeTime;
    private String nodeTimeMin;
    private String nodeContent;
    private String nodeName;

    public NodeInfo(String nodeTime, String nodeTimeMin,
                    String nodeContent, String nodeName) {
        this.nodeTime = nodeTime;
        this.nodeTimeMin = nodeTimeMin;
        this.nodeContent = nodeContent;
        this.nodeName = nodeName;
    }

    public String getNodeTimeMin() {
        return nodeTimeMin;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeTime() {
        return nodeTime;
    }

    public String getNodeContent() {
        return nodeContent;
    }
}
