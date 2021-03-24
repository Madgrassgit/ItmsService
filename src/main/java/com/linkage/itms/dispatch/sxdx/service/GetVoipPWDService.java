package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.sxdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.sxdx.obj.GetVoipPWDXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetVoipPWDService extends ServiceFather {
    private static Logger logger = LoggerFactory.getLogger(GetVoipPWDService.class);
    private GetVoipPWDXML dealXML;

    public GetVoipPWDService(String methodName) {
        super(methodName);
    }

    /*
     * int result
    0:没有符合要求的终端；
    1：成功；
    -1：终端不在线；
    -2：没有开通业务；
    -1000：其他错误

     * */
    public String work(String inXml) {
        logger.warn(methodName + "执行，入参为：{}", inXml);
        dealXML = new GetVoipPWDXML(methodName);
        // 验证入参
        if (null == dealXML.getXML(inXml)) {
            logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证没通过[{}]", dealXML.returnXML());
            return "";
        }
        logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证通过.");
        CpeInfoDao dao = new CpeInfoDao();

        Map<String, String> infomap = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
        if (null == infomap || infomap.isEmpty() || StringUtil.isEmpty(StringUtil.getStringValue(infomap, "device_id"))) {
            return "";
        }
        String user_id = StringUtil.getStringValue(infomap, "user_id");
        List<HashMap<String, String>> passinfoList = dao.getVoipPWD(user_id);
        if (passinfoList == null || passinfoList.isEmpty()||passinfoList.size()<=0) {
            return "";
        } else {
        	StringBuffer ret = new StringBuffer();
        	for (HashMap<String, String> map : passinfoList) {
        		ret.append(StringUtil.getStringValue(map, "line_id"))
        		.append(",")
        		.append(StringUtil.getStringValue(map, "voip_passwd"))
        		.append(";");
        	}
            return ret.substring(0, ret.length() - 1);
        }
    }
}
