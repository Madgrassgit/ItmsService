package com.linkage.itms.socket.userBandSyn.bean;

/**
 * AAA系统用户带宽变更同步bean
 * create by lingmin on 2019/11/07
 */
public class UserBandSynBean {

    //报文唯一序列号
    private String serialNo;

    //用户的宽带账号
    private String account;

    //用户上行带宽
    private String upBandwidth;

    //用户下行带宽
    private String downBandwidth;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUpBandwidth() {
        return upBandwidth;
    }

    public void setUpBandwidth(String upBandwidth) {
        this.upBandwidth = upBandwidth;
    }

    public String getDownBandwidth() {
        return downBandwidth;
    }

    public void setDownBandwidth(String downBandwidth) {
        this.downBandwidth = downBandwidth;
    }
}
