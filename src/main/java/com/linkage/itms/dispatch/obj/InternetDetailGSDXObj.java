package com.linkage.itms.dispatch.obj;

public class InternetDetailGSDXObj {

    private String connectionType;
    private String connectionStatus;
    private String lanInterfaceBind;

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getLanInterfaceBind() {
        return lanInterfaceBind;
    }

    public void setLanInterfaceBind(String lanInterfaceBind) {
        this.lanInterfaceBind = lanInterfaceBind;
    }

    @Override
    public String toString() {
        return "InternetDetailGSDXObj{" +
                "connectionType='" + connectionType + '\'' +
                ", connectionStatus='" + connectionStatus + '\'' +
                ", lanInterfaceBind='" + lanInterfaceBind + '\'' +
                '}';
    }
}
