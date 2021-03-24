package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.service.ServiceFather;
import com.linkage.itms.dispatch.sxdx.obj.GetSnRstXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSnRstService extends ServiceFather {
    private static Logger logger = LoggerFactory.getLogger(GetSnRstService.class);
    private GetSnRstXML dealXML;

    public GetSnRstService(String methodName) {
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
    public int work(String inXml) {
        logger.warn(methodName + "执行，入参为：{}", inXml);
        dealXML = new GetSnRstXML (methodName);
        // 验证入参
        if (null == dealXML.getXML(inXml)) {
            logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证没通过[{}]", dealXML.returnXML());
            return -1000;
        }
        logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证通过.");
        CpeInfoDao dao = new CpeInfoDao();

        Map<String, String> infomap = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
        if (null == infomap || infomap.isEmpty() || StringUtil.isEmpty(StringUtil.getStringValue(infomap, "device_id"))) {
            return 0;
        }
        String deviceId = StringUtil.getStringValue(infomap, "device_id");
        String user_id = StringUtil.getStringValue(infomap, "user_id");
        List<HashMap<String, String>> deviceinfo = dao.getDeviceInfo(deviceId);
        if (deviceinfo == null || deviceinfo.isEmpty()) {
            return 0;
        } else {
            return deviceinfo.size();
        }


    }

}
