package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.InternetDetailGSDXObj;
import com.linkage.itms.dispatch.obj.InternetDetailsChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GSDX-REQ-ITMS-20200918-LWX-001 宽带上网诊断能力封装接口及loid和宽带账号一对多问题优化需求
 * 采集宽带通道：连接方式、连接状态、绑定端口   光功率
 *
 * @Author lingmin
 * @Date 2020/9/21
 **/
public class InternetDetailsService implements IService{
    private static final Logger LOGGER = LoggerFactory.getLogger(InternetDetailsService.class);
    private DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
    private ACSCorba corba = new ACSCorba();
    private String WAN_VLANID = "41";
    private InternetDetailsChecker checker;
    private String deviceId;

    @Override
    public String work(String inXml) {
        LOGGER.warn("begin InternetDetailsService...");
        checker = new InternetDetailsChecker(inXml);
        //1、入参合法性校验
        if (!checker.check()) {
            LOGGER.error("[InternetDetailsService]cmdId[{}] userinfo[{}]验证未通过，返回：{}",
                    checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml());
            return checker.getReturnXml();
        }

        //2、处理用户信息 校验用户信息是否存在
        Map<String, String> userInfoMap = deviceInfoDAO.queryUserInfoForGS(checker.getUserInfoType(), checker.getUserInfo());
        if (null == userInfoMap || userInfoMap.isEmpty()) {
            LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]无此用户",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            checker.setResult(1002);
            checker.setResultDesc("无此用户信息");
            return checker.getReturnXml();
        }

        //3、用户信息存在 获取设备id 和设备类型、型号
        if (!getDeviceInfo(userInfoMap)) {
            return checker.getReturnXml();
        }

        //4、开始采集节点
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]开始采集deviceId[{}]",
                checker.getCmdId(), checker.getUserInfo(), deviceId);

        //4.1 检查设备是否在线 不在线则组装返回
        if (!checkDevOnline()){
            return checker.getReturnXml();
        }

        //4.2 设备在线 组装需要采集的节点路径 这里先采集wan下所有一级节点i
        //读取上行方式
        String accessType = getAccessType();
        if (StringUtil.IsEmpty(accessType) || (!accessType.contains("GPON") && !accessType.contains("EPON"))) {
            LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]get accessType[{}] is not GPON or EPON",
                    checker.getCmdId(), checker.getUserInfo(), accessType);
            return getResult(1000,"设备上行方式[" + accessType + "]无效");
        }
        String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
        ArrayList<String> wanChildren = corba.getParamNamesPath(deviceId,wanPath,1);
        if(wanChildren == null || wanChildren.size() == 0){
            //获取子节点失败 组装返回
            return getResult(1000,"节点采集失败");
        }
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}] gather WANConnectionDevice end，deviceId:{}," +
                "childrenList:{}", checker.getCmdId(), checker.getUserInfo(), deviceId, wanChildren);

        //4.3 子节点 取vlanIdMark:41 的节点 采集宽带通道下的连接方式、连接状态、绑定端口
        gatherConnectInfos(wanChildren, accessType);

        //4.5 获取accessType 根据Epon 和 Gpon 采集光功率节点
        gatherPowerNode(accessType);

        //5、返回
        return getResult(0,"成功");
    }

    private boolean getDeviceInfo(Map<String, String> deviceInfoMap) {
        deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");
        if (StringUtil.IsEmpty(deviceId)) {
            LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]未绑定设备",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            checker.setResult(1004);
            checker.setResultDesc("此用户未绑定设备");
            return false;
        }
        Map<String,String> deviceTypeInfoMap = deviceInfoDAO.queryDeviceType(deviceId);
        checker.setCpeType(StringUtil.getStringValue(deviceTypeInfoMap, "device_type"));
        checker.setCpeModel(StringUtil.getStringValue(deviceTypeInfoMap, "device_model"));
        return true;
    }

    private void gatherPowerNode(String accessType) {
        //根据上行方式 区分 Gpon Epon
        List<String> powerPathList = new ArrayList<String>();
        String powerPathCommon = "InternetGatewayDevice.WANDevice.1.";
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]get accessType[{}]", checker.getCmdId(), checker.getUserInfo(), accessType);
        String powerPath = getPowerPath(accessType);
        powerPathList.add(powerPathCommon + powerPath + "TXPower");
        powerPathList.add(powerPathCommon + powerPath + "RXPower");
        ArrayList<ParameValueOBJ> resultList = callGatherParams(powerPathList);
        if(resultList == null || resultList.size() == 0){
            LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}] gather power fail",checker.getCmdId(), checker.getUserInfo());
            return;
        }
        for(ParameValueOBJ obj : resultList){
            if(obj.getName().contains("TXPower")){
                checker.setTxPower(obj.getValue());
            }else if(obj.getName().contains("RXPower")){
                checker.setRxPower(obj.getValue());
            }
        }
    }

    private String getVlanIdPath(String accessType) {
        String vlanIdPath = "";
        if (accessType.contains("GPON")) {
            vlanIdPath = "X_CT-COM_WANGponLinkConfig.VLANIDMark";
        } else if (accessType.contains("EPON")) {
            vlanIdPath = "X_CT-COM_WANEponLinkConfig.VLANIDMark";
        }
        return vlanIdPath;
    }

    private String getPowerPath(String accessType) {
        String powerPath = "";
        if (accessType.contains("GPON")) {
            powerPath = "X_CT-COM_GponInterfaceConfig.";
        } else if (accessType.contains("EPON")) {
            powerPath = "X_CT-COM_EponInterfaceConfig.";
        }
        return powerPath;
    }

    private void gatherConnectInfos(ArrayList<String> wanChildren,String accessType) {
        List<InternetDetailGSDXObj> internetDetailList = new ArrayList<InternetDetailGSDXObj>();
        String vlanIdPath = getVlanIdPath(accessType);
        for(String child : wanChildren){
            ArrayList<ParameValueOBJ> vlanId = corba.getValue(deviceId,child + vlanIdPath);
            LOGGER.warn("[InternetDetailsService]userinfo[{}] gather vlanIdPath:{},value:{}",checker.getUserInfo(),child + vlanIdPath,vlanId);
            if(!vlanId.get(0).getValue().contains(WAN_VLANID)){
                continue;
            }
            List<String> pathList = new ArrayList<String>();
            //上网连接类型
            String connectType = "WANPPPConnection.1.ConnectionType";
            pathList.add(child + connectType);
            //连接状态
            String connectStatus = "WANPPPConnection.1.ConnectionStatus";
            pathList.add(child + connectStatus);
            //绑定端口
            String bindPort = "WANPPPConnection.1.X_CT-COM_LanInterface";
            pathList.add(child + bindPort);
            //批量采集节点
            ArrayList<ParameValueOBJ> resultList = callGatherParams(pathList);
            if(resultList == null || resultList.size() == 0){
                LOGGER.warn("[InternetDetailsService]callGatherParams with result null");
                continue;
            }
            internetDetailList.add(getInternetDetailGSDXObj(connectType, connectStatus, bindPort, resultList));
        }
        checker.setInternetDetails(internetDetailList);
    }

    private InternetDetailGSDXObj getInternetDetailGSDXObj(String connectType, String connectStatus, String bindPort, ArrayList<ParameValueOBJ> resultList) {
        InternetDetailGSDXObj internetDetail = new InternetDetailGSDXObj();
        for(ParameValueOBJ obj : resultList){
            String nodeValue = obj.getValue();
            if(obj.getName().contains(connectType)){
                internetDetail.setConnectionType(nodeValue);
            }else if(obj.getName().contains(connectStatus)){
                internetDetail.setConnectionStatus(nodeValue);
            }else if(obj.getName().contains(bindPort) && !StringUtil.IsEmpty(nodeValue)){
                internetDetail.setLanInterfaceBind(getBindPort(nodeValue));
            }
        }
        return internetDetail;
    }

    private String getBindPort(String nodeValue) {
        //绑定端口做下截取 LAN1 or WLAN1
        String[] bindPortArray = nodeValue.split(",");
        StringBuilder bindPortStr = new StringBuilder();
        for (String portStr : bindPortArray) {
            String portNum = portStr.substring(portStr.lastIndexOf('.') + 1);
            if (portStr.contains("WLAN")) {
                bindPortStr.append("WLAN").append(portNum).append(",");
            } else {
                bindPortStr.append("LAN").append(portNum).append(",");
            }
        }
        return bindPortStr.substring(0,bindPortStr.length() - 1);
    }

    private String getAccessType() {
        String accessType = UserDeviceDAO.getAccType(deviceId);
        if (!StringUtil.IsEmpty(accessType)) {
            return accessType;
        }
        String accessTypePatg = "InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType";
        ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, accessTypePatg);
        if (null == objLlist || objLlist.size() == 0) {
            LOGGER.error("[InternetDetailsService]cmdId[{}] userinfo[{}]gather accessType fail!", checker.getCmdId(), checker.getUserInfo());
            accessType = "";
        } else {
            accessType = objLlist.get(0).getValue();
        }
        return accessType;
    }

    private ArrayList<ParameValueOBJ> callGatherParams(List<String> pathList) {
        String[] gatherPathArray = new String[pathList.size()];
        pathList.toArray(gatherPathArray);
        ArrayList<ParameValueOBJ> resultList;
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}] batch gather params start with pathList:[{}]",
                checker.getCmdId(), checker.getUserInfo(), gatherPathArray);
        resultList = corba.getValue(deviceId, gatherPathArray);
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}] batch gather params end with deviceId[{}],resultList[{}]",
                checker.getCmdId(), checker.getUserInfo(), deviceId, resultList);
        return resultList;
    }


    private boolean checkDevOnline() {
        int onlineStatus = DevOnlineCAO.devOnlineTest(deviceId);
        LOGGER.warn("[InternetDetailsService]cmdId[{}] userinfo[{}]get onlineStatus[{}]",
                checker.getCmdId(), checker.getUserInfo(), onlineStatus);
        if (onlineStatus == -3) {
            checker.setResult(1007);
            checker.setResultDesc("设备正在被操作，无法读取！");
            return false;
        }
        if (onlineStatus != 1) {
            checker.setResult(1005);
            checker.setResultDesc("设备不在线，无法读取！");
            checker.setCpeOnlineStatus("2");
            return false;
        }
        checker.setCpeOnlineStatus("1");
        return true;
    }

    private String getResult(int code, String msg){
        checker.setResult(code);
        checker.setResultDesc(msg);
        return checker.getReturnXml();
    }
}
