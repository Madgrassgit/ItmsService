package com.linkage.itms.dispatch.gsdx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import PreProcess.UserInfo;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.gsdx.dao.CpeInfoDao;
import com.linkage.itms.dispatch.gsdx.obj.ResetServiceXML;

public class ResetService extends ServiceFather {
    private static Logger logger = LoggerFactory.getLogger(ResetService.class);
    private ResetServiceXML dealXML;

    public ResetService(String methodName) {
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
        dealXML = new ResetServiceXML(methodName);
        // 验证入参
        if (null == dealXML.getXML(inXml)) {
            logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证没通过[{}]", dealXML.returnXML());
            return -1000;
        }
        logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证通过.");
        CpeInfoDao dao = new CpeInfoDao();

        Map<String, String> infomap = dao.queryUserInfo(StringUtil.getIntegerValue(dealXML.getiParaType()), dealXML.getValue());
        if (null == infomap || infomap.isEmpty() || StringUtil.isEmpty(StringUtil.getStringValue(infomap, "device_id"))) {
            return 0;
        }
        String deviceId = StringUtil.getStringValue(infomap, "device_id");
        String user_id = StringUtil.getStringValue(infomap, "user_id");
        List<HashMap<String, String>> deviceinfo = dao.getDeviceInfo(deviceId);
        if (deviceinfo == null || deviceinfo.isEmpty()) {
            return 0;
        }
        //查询业务
        String serv_type_id = dao.getServTypeIdBytype(user_id);

        if (serv_type_id != null && "14".equals(serv_type_id)) {

            int a = dao.getServOpenStatus(user_id);
            if(a<=0){
                logger.warn("{}更新业务状态失败{}", new Object[]{deviceId, a});
                return -1000;
            }
        } else {
            logger.warn("{}业务类型不为语音{}", new Object[]{deviceId, serv_type_id});
            return -1000;
        }

        logger.warn(methodName + "[" + dealXML.getOpId() + "],根据条件查询结果{}", infomap);
        UserInfo[] userInfo = new UserInfo[1];
        userInfo[0] = new UserInfo();
        userInfo[0].deviceId = deviceId;
        userInfo[0].oui = StringUtil.getStringValue(deviceinfo.get(0), "oui");
        userInfo[0].deviceSn = StringUtil.getStringValue(deviceinfo.get(0), "device_serialnumber");
        userInfo[0].gatherId = "1";  // 采集点
        userInfo[0].userId = StringUtil.getStringValue(infomap, "user_id");
        userInfo[0].servTypeId = "14";
        userInfo[0].operTypeId = "1";
        logger.warn("调配置模块，下发业务，deviceId={}", new Object[]{deviceId});
        int result = 1;

        try {
            //调用机顶盒网关下发
            result = CreateObjectFactory.createPreProcess(Global.GW_TYPE_ITMS).processServiceInterface(userInfo);
        } catch (Throwable e) {
            logger.warn("调配置模块下发业务出现异常:{}", ExceptionUtils.getStackTrace(e));
        }

        logger.warn("调配置模块下发业务结果{}", new Object[]{result});
        if (-2 == result) {
            logger.warn("调配置模块下发业务失败{}", new Object[]{deviceId});
            return -1000;
        } else {
            logger.warn("调配置模块下发业务成功", new Object[]{deviceId});
            return 1;
        }
    }

}
