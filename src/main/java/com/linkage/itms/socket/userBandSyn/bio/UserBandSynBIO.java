package com.linkage.itms.socket.userBandSyn.bio;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.socket.core.DefaultMsgAction;
import com.linkage.itms.socket.userBandSyn.bean.UserBandSynBean;
import com.linkage.itms.socket.userBandSyn.dao.UserBandSynDAO;

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
 * create by lingmin on 2019/11/07
 */
public class UserBandSynBIO extends DefaultMsgAction {

    private UserBandSynBean bandSynBean = null;

    @Override
    public String execute(String message) {
        logger.warn("UserBandSynBIO with message is {}", message);
        //1、解析报文
        bandSynBean = analysis(message);
        if(StringUtil.isEmpty(bandSynBean.getAccount())){
            logger.warn("UserBandSynBIO with result: the account param null");
            setResult("2", "入参用户账号为空");
            return returnMsg();
        }

        if(StringUtil.isEmpty(bandSynBean.getUpBandwidth()) || StringUtil.isEmpty(bandSynBean.getDownBandwidth())){
            setResult("2", "入参带宽为空");
            logger.warn("UserBandSynBIO with result: the bandwidth params null");
            return returnMsg();
        }

        //2、更新
        UserBandSynDAO bandSynDAO = new UserBandSynDAO();
        int rows = bandSynDAO.updateNetServBand(bandSynBean);
        if(rows == 0){
            setResult("1", "用户不存在");
            logger.warn("UserBandSynBIO with result: user not exist,username:{}",bandSynBean.getAccount());
            return returnMsg();
        }
        setResult("0", "同步成功");
        logger.warn("UserBandSynBIO with result: success");
        return returnMsg();
    }

    private UserBandSynBean analysis(String message)
    {
        UserBandSynBean bean = new UserBandSynBean();
        char startCh = '{';
        char endCh = '}';
        int startIx = 0;
        for (int i = 0; (startIx = message.indexOf(startCh, startIx)) > 0; i++)
        {
            int endIx = message.indexOf(endCh, startIx);
            if (endIx > startIx)
            {
                String content = message.substring(startIx + 1, endIx);
                if (i == 0)
                {
                    bean.setSerialNo(content);
                }
                else if (i == 1)
                {
                    bean.setAccount(content);
                }
                else if (i == 2)
                {
                    bean.setUpBandwidth(content);
                }
                else if (i == 3)
                {
                    bean.setDownBandwidth(content);
                }
            }
            startIx = endIx;
        }
        return bean;
    }

    private String returnMsg()
    {
        return "{" + bandSynBean.getSerialNo() + "} {" + resultCode +
                "} {" + resultMsg + "}\n";
    }
}
