package com.linkage.itms.socket.userBandSyn;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.socket.core.MsgAction;
import com.linkage.itms.socket.core.MsgListener;
import com.linkage.itms.socket.userBandSyn.bio.UserBandSynBIO;

/**
 * AAA系统用户宽带变更同步至Itms系统
 * Socket接口文本协议： AAAASynUserBandRateInterface{SerialNo} {Account} {UpBandwidth} {DownBandwidth}
 * 参数含义:
 * SerialNo: 由AAA系统根据当前时间秒数生成的随机数
 * Account: 用户的宽带账号
 * UpBandwidth: 用户上行带宽
 * DownBandwidth: 用户下行带宽
 *
 * 示例：AAAASynUserBandRateInterface {157249330226} {17794944318} {100M} {20M}
 * 当接受Socket消息以AAAASynUserBandRateInterface开头则是用户宽带同步
 *
 * create by lingmin on 2019/11/07
 */
public class UserBandSynListener implements MsgListener {

    private static final String SYN_BAND_FLAG = "AAAASynUserBandRateInterface";

    @Override
    public MsgAction handleMessage(String message) {
        //非用户宽带同步消息不处理
        if(StringUtil.isEmpty(message) || (!StringUtil.isEmpty(message) && !message.startsWith(SYN_BAND_FLAG))){
            return null;
        }
        return new UserBandSynBIO();
    }
}
