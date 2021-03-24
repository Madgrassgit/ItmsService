package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dispatch.obj.QueryRouterSpeedChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author guank
 * @version 1.0
 * @category
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * @since 2020/7/20.
 */
public class QueryRouterSpeedService implements IService {

    private static Logger logger = LoggerFactory.getLogger(QueryRouterSpeedService.class);
    private static final String DEV_MODLE_ONE = "I-120E";
    private static final String DEV_MODLE_TWO = "I-240W";
    private static final String DEV_HARDVERSION1_ONE = "3FE45252";
    private static final String DEV_HARDVERSION2_ONE = "3FE55923ACAA01";
    private static final String DEV_HARDVERSION_TWO = "3FE45643BAAA";
    private DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();

    @Override
    public String work(String inXml) {

        String vendor_id = "3";//华为厂家编号
        String pathName="X_CU_AdaptRate";
        logger.warn("QueryRouterSpeedService==>inXml({})", inXml);
        boolean hasOneLanFlag = false;
        QueryRouterSpeedChecker checker = new QueryRouterSpeedChecker(inXml);
        if (false == checker.check()) {
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
                    new Object[]{checker.getCmdId(), checker.getUserInfo(),
                            checker.getReturnXml()});
            return checker.getReturnXml();
        }

        // 1：根据用户宽带帐号
        List<HashMap<String, String>> deviceInfoList = null;
        if (1 == checker.getUserInfoType()) {
            // 1：根据用户宽带帐号
            deviceInfoList = deviceInfoDAO.queryUserVendorByNetAccount(checker.getUserInfo());
        }else{
            checker.setResult(1);
            checker.setResultDesc("数据格式错误");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]数据格式错误",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }

        if (null == deviceInfoList || deviceInfoList.size() == 0) {
            checker.setResult(1001);
            checker.setResultDesc("根据宽带查询不到记录");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]根据宽带查询不到记录",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }
        if (deviceInfoList.size() > 1) {
            checker.setResult(1003);
            checker.setResultDesc("宽带账号查询到多条记录");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]宽带账号查询到多条记录",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }
        HashMap<String, String> deviceInfoMap = deviceInfoList.get(0);
        // 设备不存在
        if (null == deviceInfoMap || deviceInfoMap.isEmpty()) {
            checker.setResult(1002);
            checker.setResultDesc("用户未绑定设备");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]查无此设备",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }

        // 设备不存在
        if ("".equals(StringUtil.getStringValue(deviceInfoMap, "device_id"))
                || null == StringUtil.getStringValue(deviceInfoMap, "device_id")) {
            checker.setResult(1002);
            checker.setResultDesc("用户未绑定设备");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]用户未绑定设备",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }

        String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
        checker.setCityId(StringUtil.getStringValue(deviceInfoMap, "city_id"));
        checker.setUserName(StringUtil.getStringValue(deviceInfoMap, "username"));
        checker.setDevModel(StringUtil.getStringValue(deviceInfoMap, "device_model"));
        checker.setDevSN(StringUtil.getStringValue(deviceInfoMap, "device_serialnumber"));

        //这里查下设备类型返回 SDLT-REQ-ITMS-20201229-FMK-001(山东联通RMS系统修改路由器适配速率采集接口)
        //update on 2020/12/30 by lingmin
        if("sd_lt".equals(Global.G_instArea)){
            checker.setDevType(getDevType(checker.getDevModel(),checker.getDevSN(),deviceInfoMap.get("hardwareversion")));
        }

        GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
        ACSCorba corba = new ACSCorba();
        int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
        logger.warn("deviceId:{},testDeviceOnLineStatus get result:{}",deviceId,flag);
        if (-6 == flag) {
            checker.setResult(1000);
            checker.setResultDesc("设备正在被操作");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]设备正在被操作，无法获取节点值",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }
        if (-1 == flag || -2 == flag) {
            checker.setResult(1004);
            checker.setResultDesc("设备不在线");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]设备不在线，无法获取节点值",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }
        if (1 == flag) {
            logger.warn("[{}]设备在线，正在进行采集操作", deviceId);

            String gatherPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.X_CU_AdaptRate";

            /*
             * SDLT-REQ-RMS-20200117-fanmk-001(山东联通RMS平台修改协商速率采集节点)
             * 华为厂商，较为特殊，采集路径变更
             * InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{i}.
             * X_HW_Speed
             *
             */
            if (vendor_id.equals(StringUtil.getStringValue(deviceInfoMap, "vendor_id")))
            {
                gatherPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.X_HW_Speed";
                pathName = "X_HW_Speed";
            }

            ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,
                    gatherPath);
            if ((null == objLlist) || (objLlist.isEmpty()))
            {
                //华为厂家在采集不到私有节点的情况下，再次尝试采集共有节点，都没有则放弃该次采集。
                gatherPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.MaxBitRate";
                pathName = "MaxBitRate";
                objLlist = corba.getValue(deviceId,gatherPath);

            }

            if ((null == objLlist) || (objLlist.isEmpty())) {
                if ((null == objLlist) || (objLlist.isEmpty())) {
                    checker.setResult(1005);
                    checker.setResultDesc("采集节点不存在");
                    logger.warn(
                            "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]获取objLlist失败，返回",
                            new Object[]{checker.getCmdId(), checker.getUserInfo()});
                    return checker.getReturnXml();
                }
            }
            logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId,
                    Integer.valueOf(objLlist.size()));
            for (ParameValueOBJ pvobj : objLlist) {
                    if (pvobj.getName().contains(pathName)) {
                        checker.setRouterSpeed(pvobj.getValue());
                    }
            }

        } else {
            checker.setResult(1004);
            checker.setResultDesc("设备不在线");
            logger.warn(
                    "servicename[QueryRouterSpeedService]cmdId[{}]userInfo[{}]设备不在线",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return checker.getReturnXml();
        }
        String returnXML = checker.getReturnXml();
        logger.warn("QueryRouterSpeedService==>returnXML:" + returnXML);
        return returnXML;
    }

    /**
     * 判断设备类型 GE/FE
     * @param devModel
     * @param devSN
     * @param hardVersion
     * @return
     */
    private String getDevType(String devModel,String devSN,String hardVersion){
        if(!StringUtil.IsEmpty(devModel) && !StringUtil.IsEmpty(hardVersion) && devModel.equals(DEV_MODLE_ONE)
                && (hardVersion.equals(DEV_HARDVERSION1_ONE) || hardVersion.equals(DEV_HARDVERSION2_ONE))){
            return "GE";
        }
        if(!StringUtil.IsEmpty(devModel) && !StringUtil.IsEmpty(hardVersion) && devModel.equals(DEV_MODLE_TWO)
                && hardVersion.equals(DEV_HARDVERSION_TWO)){
            return "GE";
        }
        if(!StringUtil.IsEmpty(devModel) && deviceInfoDAO.queryCountByModel(devModel)){
            return "GE";
        }

        if(!StringUtil.IsEmpty(devSN) && deviceInfoDAO.queryCountBySn(devSN)){
            return "GE";
        }
        return "FE";
    }
}
