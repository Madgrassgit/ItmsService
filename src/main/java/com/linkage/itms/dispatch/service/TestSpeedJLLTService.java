package com.linkage.itms.dispatch.service;

import ACS.DevRpc;
import ACS.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.corba.DevRPCManager;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.TestSpeedCheckerJLLT;
import com.linkage.itms.dispatch.util.TimeCheckUtil;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.AnyObject;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 吉林联通测速接口 Service
 *
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年11月18日
 */
public class TestSpeedJLLTService implements IService {

    private static Logger logger = LoggerFactory.getLogger(TestSpeedJLLTService.class);
    private final String methodName = "TestSpeedService4JLLT";

    public static void main(String[] args) {
        new TestSpeedJLLTService().work("<?xml version=\"1.0\" encoding=\"GBK\"?>\n" +
                "<root>\n" +
                "\t<CmdID>123456789012345</CmdID>\n" +
                "\t<CmdType>CX_01</CmdType>\n" +
                "\t<ClientType>5</ClientType>\n" +
                "\t<Param>\n" +
                "<UserInfoType>1</UserInfoType>\n" +
                "<UserInfo>0311test</UserInfo>\n" +
                "<UserSpeed>200</UserSpeed>    <TestSpeedReportUrl>http://218.27.253.1:8094/speedfile/services/contractrate/results/insert/</TestSpeedReportUrl><TestSpeedDownUrl>http://218.27.253.1:8094/speedfile/services/contractrate/contract/rate/</TestSpeedDownUrl>\n" +
                "\t</Param>\n" +
                "</root>");
    }

    @Override
    public String work(String inParam) {
        logger.warn("TestSpeedService4JLLT==>inParam({})", inParam);
        // 解析获得入参
        TestSpeedCheckerJLLT checker = new TestSpeedCheckerJLLT(inParam.trim());

        // 验证入参
        if (!checker.check()) {
            logger.warn("入参验证没通过,TestSpeedService4JLLT==>inParam({})", inParam);
            logger.warn("work==>inParam=" + checker.getReturnXml());

            return checker.getReturnXml();
        }

        /**
         * 检验接口调用频次
         */
        logger.warn("开始检验接口调用频次");
        boolean inTime = new TimeCheckUtil().isInTimeCheck("testSpeed");
        if(!inTime){
            checker.setResult(1006);
            checker.setResultDesc("请求超限");
            return checker.getReturnXml();
        }

        // 查询用户设备信息
        UserDeviceDAO dao = new UserDeviceDAO();

        //web会直接传值device_id
        Map<String, String> queryUserInfo = dao.queryUserInfoByLasttime(StringUtil.getIntegerValue(checker.getUserInfoType()), checker.getUserInfo());
        logger.warn(methodName + "[" + checker.getOpId() + "],根据条件查询结果{}", queryUserInfo);

        if (null == queryUserInfo || queryUserInfo.size() == 0) {
            checker.setResult(1001);
            checker.setResultDesc("无此用户信息");
            return checker.getReturnXml();
        }
        String deviceId = StringUtil.getStringValue(queryUserInfo, "device_id");

        if (StringUtil.IsEmpty(deviceId)) {
            logger.warn(methodName + "[" + checker.getOpId() + "],deviceId{}", deviceId);
            checker.setResult(1002);
            checker.setResultDesc("此用户未绑定");
            return checker.getReturnXml();
        }
        checker.setCity_id(StringUtil.getStringValue(queryUserInfo, "city_id"));
        logger.warn("deviceId=" + deviceId);
        String returnXml = "";
        try {
            returnXml = doTest(checker, deviceId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]处理结束，返回响应信息:{}", new Object[]{returnXml});

        return returnXml;

    }


    public String doTest(TestSpeedCheckerJLLT checker, String deviceId) throws Exception {
        String city_id = checker.getCity_id();
        if("".equals(city_id)){
            checker.setResult(1001);
            checker.setResultDesc("无此用户信息");
            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]cityid为空, ReturnXml:" + checker.getReturnXml());
            return checker.getReturnXml();
        }else if(!"00".equals(city_id)){
            city_id=city_id.substring(0,3);
        }
        Map euUser = UserDeviceDAO.getEuUser(checker.getUserSpeed(),city_id);

        String eupppoename =StringUtil.getStringValue(euUser,"net_account");
        String eupassword=StringUtil.getStringValue(euUser,"net_password");
        if("".equals(eupppoename)||null==eupppoename){
            checker.setResult(1005);
            checker.setResultDesc("无对应仿真账号");
            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]无对应仿真账号, ReturnXml:" + checker.getReturnXml());
            return checker.getReturnXml();
        }

        logger.warn("eupppoename:{},eupassword:{}",eupppoename,eupassword);
        ACSCorba acsCorba = new ACSCorba();
        GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
        int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
        String wanPassageWay = "";
        if (flag == 1) {
            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]入参不带WAN通道，开始获取：");
            // 获取wan通道
            Map<String, String> wanPassageWayMap = gatherWanPassageWay(deviceId, acsCorba);

            if (null != wanPassageWayMap && !wanPassageWayMap.isEmpty()) {
                for (String key : wanPassageWayMap.keySet()) {
                    if (key.startsWith("INTERNET")) {
                        wanPassageWay = wanPassageWayMap.get(key);
                        break;
                    }
                }
            } else {
                checker.setResult(1004);
                checker.setResultDesc("终端不支持测速");
                logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]获取wan通道失败, ReturnXml:" + checker.getReturnXml());
                return checker.getReturnXml();
            }

            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]的WAN通道" + wanPassageWay + "，开始下发测速！");


            DevRpc[] devRPCArr = new DevRpc[1];


            SetParameterValues setParameterValues = new SetParameterValues();

            ParameterValueStruct[] ParameterValueStruct = null;

            if ("serverMode".equals(checker.getSpeedTest_testMode())) {
                    ParameterValueStruct = new ParameterValueStruct[9];
            } else {
                    ParameterValueStruct = new ParameterValueStruct[8];

            }


            if ("serverMode".equals(checker.getSpeedTest_testMode())) {
                ParameterValueStruct[0] = new ParameterValueStruct();
                ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
                AnyObject anyObject = new AnyObject();
                anyObject.para_value = checker.getSpeedTest_testMode();
                anyObject.para_type_id = "1";
                ParameterValueStruct[0].setValue(anyObject);

                ParameterValueStruct[1] = new ParameterValueStruct();
                ParameterValueStruct[1].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testURL");
                anyObject = new AnyObject();
                anyObject.para_value = checker.getSpeedTest_downloadURL();
                anyObject.para_type_id = "1";
                ParameterValueStruct[1].setValue(anyObject);

                ParameterValueStruct[2] = new ParameterValueStruct();
                ParameterValueStruct[2].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
                anyObject = new AnyObject();
                anyObject.para_value = wanPassageWay;
                anyObject.para_type_id = "1";
                ParameterValueStruct[2].setValue(anyObject);

                ParameterValueStruct[3] = new ParameterValueStruct();
                ParameterValueStruct[3].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
                anyObject = new AnyObject();
                anyObject.para_value = "Requested";
                anyObject.para_type_id = "1";
                ParameterValueStruct[3].setValue(anyObject);

                ParameterValueStruct[4] = new ParameterValueStruct();
                ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
                anyObject = new AnyObject();
                anyObject.para_value = checker.getUserInfo();
                anyObject.para_type_id = "1";
                ParameterValueStruct[4].setValue(anyObject);

                ParameterValueStruct[5] = new ParameterValueStruct();
                ParameterValueStruct[5].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
                anyObject = new AnyObject();
                anyObject.para_value =eupppoename;
                anyObject.para_type_id = "1";
                ParameterValueStruct[5].setValue(anyObject);

                ParameterValueStruct[6] = new ParameterValueStruct();
                ParameterValueStruct[6].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
                anyObject = new AnyObject();
                anyObject.para_value = eupassword;
                anyObject.para_type_id = "1";
                ParameterValueStruct[6].setValue(anyObject);

                ParameterValueStruct[7] = new ParameterValueStruct();
                ParameterValueStruct[7].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.reportURL");
                anyObject = new AnyObject();
                anyObject.para_value = checker.getSpeedTest_reportURL();
                anyObject.para_type_id = "1";
                ParameterValueStruct[7].setValue(anyObject);

                ParameterValueStruct[8] = new ParameterValueStruct();
                ParameterValueStruct[8].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.backgroundsize");
                anyObject = new AnyObject();
                anyObject.para_value = "";
                anyObject.para_type_id = "1";
                ParameterValueStruct[8].setValue(anyObject);

            } else {
                ParameterValueStruct[0] = new ParameterValueStruct();
                ParameterValueStruct[0].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.testMode");
                AnyObject anyObject = new AnyObject();
                anyObject.para_value = checker.getSpeedTest_testMode();
                anyObject.para_type_id = "1";
                ParameterValueStruct[0].setValue(anyObject);

                ParameterValueStruct[1] = new ParameterValueStruct();
                ParameterValueStruct[1].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.downloadURL");
                anyObject = new AnyObject();
                anyObject.para_value = checker.getSpeedTest_downloadURL();
                anyObject.para_type_id = "1";
                ParameterValueStruct[1].setValue(anyObject);

                ParameterValueStruct[2] = new ParameterValueStruct();
                ParameterValueStruct[2].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.WANInterface");
                anyObject = new AnyObject();
                anyObject.para_value = wanPassageWay;
                anyObject.para_type_id = "1";
                ParameterValueStruct[2].setValue(anyObject);

                ParameterValueStruct[3] = new ParameterValueStruct();
                ParameterValueStruct[3].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState");
                anyObject = new AnyObject();
                anyObject.para_value = "Requested";
                anyObject.para_type_id = "1";
                ParameterValueStruct[3].setValue(anyObject);

                ParameterValueStruct[4] = new ParameterValueStruct();
                ParameterValueStruct[4].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName");
                anyObject = new AnyObject();
                anyObject.para_value = checker.getUserInfo();
                anyObject.para_type_id = "1";
                ParameterValueStruct[4].setValue(anyObject);

                ParameterValueStruct[5] = new ParameterValueStruct();
                ParameterValueStruct[5].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupppoename");
                anyObject = new AnyObject();
                anyObject.para_value =eupppoename;
                anyObject.para_type_id = "1";
                ParameterValueStruct[5].setValue(anyObject);

                ParameterValueStruct[6] = new ParameterValueStruct();
                ParameterValueStruct[6].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Eupassword");
                anyObject = new AnyObject();
                anyObject.para_value = eupassword;
                anyObject.para_type_id = "1";
                ParameterValueStruct[6].setValue(anyObject);

                ParameterValueStruct[7] = new ParameterValueStruct();
                ParameterValueStruct[7].setName("InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.backgroundsize");
                anyObject = new AnyObject();
                anyObject.para_value = "";
                anyObject.para_type_id = "1";
                ParameterValueStruct[7].setValue(anyObject);

            }


            setParameterValues.setParameterList(ParameterValueStruct);
            setParameterValues.setParameterKey("downLoad");
            logger.warn("setParameterValues定义完毕--------,setParameterValues=" + setParameterValues);
            GetParameterValues getParameterValues = new GetParameterValues();

            //本次测试PPPOE账号
            String pppoeName = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeName";
            //本次测试IP
            String pppoeIP = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.pppoeIP";
            //平均下载速率，单位是M，小数点两位
            String Aspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Aspeed";
            //用户签约速率，单位是M，小数点两位
            String Bspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Bspeed";
            //当前下载速率，单位是M，小数点两位
            String Cspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Cspeed";
            //最大下载速率，单位是M，小数点两位
            String maxspeed = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.maxspeed";
            //开始下载时间，时间戳格式，精确到秒
            String starttime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.starttime";
            //结束下载时间，格式同开始下载时间
            String endtime = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.endtime";

            String DiagnosticsState = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.DiagnosticsState";
            //测速状态
            String status = "InternetGatewayDevice.X_CU_Function.RMS_SpeedTest.Status";

            String[] parameterNamesArr = null;
            parameterNamesArr = new String[10];
            parameterNamesArr[0] = pppoeName;
            parameterNamesArr[1] = pppoeIP;
            parameterNamesArr[2] = Aspeed;
            parameterNamesArr[3] = Bspeed;
            parameterNamesArr[4] = Cspeed;
            parameterNamesArr[5] = maxspeed;
            parameterNamesArr[6] = starttime;
            parameterNamesArr[7] = endtime;
            parameterNamesArr[8] = DiagnosticsState;
            parameterNamesArr[9] = status;

            getParameterValues.setParameterNames(parameterNamesArr);
            logger.warn("getParameterValues定义完毕--------");

            devRPCArr[0] = new DevRpc();
            devRPCArr[0].devId = deviceId;
            Rpc[] rpcArr = new Rpc[2];
            rpcArr[0] = new Rpc();
            rpcArr[0].rpcId = "1";
            rpcArr[0].rpcName = "SetParameterValues";
            rpcArr[0].rpcValue = setParameterValues.toRPC();
            rpcArr[1] = new Rpc();
            rpcArr[1].rpcId = "2";
            rpcArr[1].rpcName = "GetParameterValues";
            rpcArr[1].rpcValue = getParameterValues.toRPC();
            devRPCArr[0].rpcArr = rpcArr;

            List<DevRpcCmdOBJ> devRPCRep = null;
            DevRPCManager devRPCManager = new DevRPCManager("1");
            logger.warn("即将devRPCManager.execRPC");
            devRPCRep = devRPCManager.execRPC(devRPCArr, Global.DiagCmd_Type);

            String errMessage = "";
            Map PPPoEMap = null;
            if (devRPCRep == null || devRPCRep.size() == 0) {
                logger.warn("[{}]List<DevRpcCmdOBJ>返回为空！", deviceId);
                errMessage = "设备无法连接";
                checker.setResult(1003);
                checker.setResultDesc(errMessage);
                logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]" + errMessage + " ReturnXml:" + checker.getReturnXml());
                return checker.getReturnXml();

            } else if (devRPCRep.get(0) == null) {
                logger.warn("[{}]DevRpcCmdOBJ返回为空！", deviceId);
                errMessage = "设备无法连接";
                checker.setResult(1003);
                checker.setResultDesc(errMessage);
                logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]" + errMessage + " ReturnXml:" + checker.getReturnXml());
                return checker.getReturnXml();
            } else {
                int stat = devRPCRep.get(0).getStat();
                if (stat != 1) {
                    errMessage = Global.G_Fault_Map.get(stat).getFaultDesc();
                    checker.setResult(-1000);
                    checker.setResultDesc(errMessage);
                    logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]" + errMessage + "， ReturnXml:" + checker.getReturnXml());
                    return checker.getReturnXml();
                } else {
                    errMessage = "系统内部错误";
                    if (devRPCRep.get(0).getRpcList() == null
                            || devRPCRep.get(0).getRpcList().size() == 0) {
                        logger.warn("[{}]List<ACSRpcCmdOBJ>返回为空！", deviceId);
                        checker.setResult(1000);
                        checker.setResultDesc(errMessage);
                        logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]" + errMessage + "，ReturnXml:" + checker.getReturnXml());
                        return checker.getReturnXml();
                    } else {
                        List<com.ailk.tr069.devrpc.obj.mq.Rpc> rpcList = devRPCRep.get(0).getRpcList();
                        if (rpcList != null && !rpcList.isEmpty()) {
                            for (int k = 0; k < rpcList.size(); k++) {
                                if ("GetParameterValuesResponse".equals(rpcList.get(k).getRpcName())) {
                                    String resp = rpcList.get(k).getValue();
                                    logger.warn("[{}]设备返回：{}", deviceId, resp);
//									Fault fault = null;
                                    if (resp == null || "".equals(resp)) {
                                        logger.debug("[{}]DevRpcCmdOBJ.value == null", deviceId);
                                        checker.setResult(1000);
                                        checker.setResultDesc("系统内部错误，无返回值");
                                        logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]系统内部错误，无返回值， ReturnXml:" + checker.getReturnXml());
                                        return checker.getReturnXml();
                                    } else {
                                        SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(resp));
                                        if (soapOBJ != null) {
//											fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
                                            Element element = soapOBJ.getRpcElement();
                                            if (element != null) {
                                                GetParameterValuesResponse getParameterValuesResponse = XmlToRpc
                                                        .GetParameterValuesResponse(element);
                                                if (getParameterValuesResponse != null) {
                                                    ParameterValueStruct[] parameterValueStructArr = getParameterValuesResponse
                                                            .getParameterList();
                                                    PPPoEMap = new HashMap<String, String>();
                                                    for (int j = 0; j < parameterValueStructArr.length; j++) {
                                                        PPPoEMap.put(parameterValueStructArr[j].getName(),
                                                                parameterValueStructArr[j].getValue().para_value);
                                                    }
                                                } else {
                                                    checker.setResult(1000);
                                                    checker.setResultDesc("系统内部错误，无返回值");
                                                    logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]系统内部错误，无返回值， ReturnXml:" + checker.getReturnXml());
                                                    return checker.getReturnXml();
                                                }
                                            } else {
                                                checker.setResult(1000);
                                                checker.setResultDesc("系统内部错误，无返回值");
                                                logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]系统内部错误，无返回值， ReturnXml:" + checker.getReturnXml());
                                                return checker.getReturnXml();
                                            }
                                        } else {
                                            checker.setResult(1000);
                                            checker.setResultDesc("系统内部错误，无返回值");
                                            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]系统内部错误，无返回值， ReturnXml:" + checker.getReturnXml());
                                            return checker.getReturnXml();
                                        }
                                    }
                                }
                            }
                        } else {
                            checker.setResult(1000);
                            checker.setResultDesc("系统内部错误，无返回值");
                            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]系统内部错误，无返回值， ReturnXml:" + checker.getReturnXml());
                            return checker.getReturnXml();
                        }
                    }

                    if (PPPoEMap == null) {
                        checker.setResult(1000);
                        checker.setResultDesc("返回值为空，PPPoE拨号仿真失败");
                        logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]返回值为空，PPPoE拨号仿真失败, ReturnXml:" + checker.getReturnXml());
                        return checker.getReturnXml();
                    } else {
                        checker.setResult(0);
                        checker.setResultDesc("成功");
                        checker.setDevSn(checker.getDevSn());
                        checker.setStatus("" + PPPoEMap.get(DiagnosticsState));
                        checker.setAspeed("" + PPPoEMap.get(Aspeed));
                        checker.setBspeed("" + PPPoEMap.get(Bspeed));
                        checker.setCspeed("" + PPPoEMap.get(Cspeed));
                        checker.setMaxspeed("" + PPPoEMap.get(maxspeed));
                        checker.setStarttime("" + PPPoEMap.get(starttime));
                        checker.setEndtime("" + PPPoEMap.get(endtime));
                        checker.setNetAccount(checker.getUserInfo());
                        logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]成功, ReturnXml:" + checker.getReturnXml());
                        return checker.getReturnXml();
                    }
                }
            }
        } else {
            checker.setResult(1003);
            checker.setResultDesc("设备无法连接");
            logger.warn(methodName + "[" + checker.getOpId() + "]" + "[" + deviceId + "]终端不在线, ReturnXml:" + checker.getReturnXml());
            return checker.getReturnXml();
        }
    }

    public Map<String, String> gatherWanPassageWay(String deviceId, ACSCorba corba) {
        String SERV_LIST_INTERNET = "INTERNET";
        Map<String, String> restMap = new HashMap<String, String>();

        // logger.warn("设备在线，可以进行采集操作，device_id={}", deviceId);
        String wanConnPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
        String wanServiceList = ".X_CU_ServiceList";
        String wanPPPConnection = ".WANPPPConnection.";
        String wanIPConnection = ".WANIPConnection.";
        String wanVlan = ".X_CU_VLAN";
        String connectionType = ".ConnectionType";

        // 默认“InternetGatewayDevice.WANDevice.”下只有实例“1”
        ArrayList<String> wanConnPathsList = corba.getParamNamesPath(deviceId, wanConnPath, 0);

        if (null == wanConnPathsList || wanConnPathsList.isEmpty()) {
            logger.warn("[{}] [{}]获取WANConnectionDevice下所有节点路径失败，逐层获取", deviceId);
            wanConnPathsList = new ArrayList<String>();
            List<String> jList = corba.getIList(deviceId, wanConnPath);
            if (null == jList || jList.isEmpty()) {
                logger.warn("[TestSpeedHBLTService] [{}]获取" + wanConnPath + "下实例号失败，返回", deviceId);
            } else {
                for (String j : jList) {
                    wanConnPathsList.add(wanConnPath + j + wanVlan);

                    // 获取session，
                    List<String> kPPPList = corba.getIList(deviceId, wanConnPath + j + wanPPPConnection);
                    if (null == kPPPList || kPPPList.isEmpty()) {
                        logger.warn("[TestSpeedHBLTService] [{}]获取" + wanConnPath + wanConnPath + j + wanPPPConnection + "下实例号失败", deviceId);
                    } else {
                        for (String kppp : kPPPList) {
                            wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + wanServiceList);
                            wanConnPathsList.add(wanConnPath + j + wanPPPConnection + kppp + connectionType);
                        }
                    }
                }
            }
        }

        if (null != wanConnPathsList && !wanConnPathsList.isEmpty()) {

            List<String> tempWanConnPathsList = new ArrayList<String>();
            for (String wanConnPaths : wanConnPathsList) {
                if (wanConnPaths.endsWith(".X_CU_ServiceList") || wanConnPaths.endsWith(".X_CU_VLAN")
                        || wanConnPaths.endsWith(".ConnectionType")) {
                    tempWanConnPathsList.add(wanConnPaths);
                }
            }

            String[] paramNameArr = new String[tempWanConnPathsList.size()];
            for (int index = 0; index < tempWanConnPathsList.size(); index++) {
                paramNameArr[index] = tempWanConnPathsList.get(index);
            }

            Map<String, String> paramValueMap = corba.getParaValueMap(deviceId, paramNameArr);
            if (null == paramValueMap || paramValueMap.isEmpty()) {
                logger.warn("[TestSpeedHBLTService] [{}]获取ServiceList失败", deviceId);
            } else {
                for (Map.Entry<String, String> entry : paramValueMap.entrySet()) {
                    logger.debug("[{}]{}={} ", new Object[]{deviceId, entry.getKey(), entry.getValue()});
                    String paramName = entry.getKey();
                    if (paramName.endsWith(wanServiceList)) {
                        if (!StringUtil.IsEmpty(entry.getValue())) {
                            String res = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList));
                            String vlanKey = "";
                            String vlanValue = "";
                            String conTypeKey = "";
                            String conTypeValue = "";

                            if (entry.getKey().indexOf(wanPPPConnection) > 0) {
                                vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanPPPConnection)) + wanVlan;
                                vlanValue = paramValueMap.get(vlanKey);
                                conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
                                conTypeValue = paramValueMap.get(conTypeKey);
                            } else {
                                vlanKey = entry.getKey().substring(0, entry.getKey().indexOf(wanIPConnection)) + wanVlan;
                                vlanValue = paramValueMap.get(vlanKey);
                                conTypeKey = entry.getKey().substring(0, entry.getKey().indexOf(wanServiceList)) + connectionType;
                                conTypeValue = paramValueMap.get(conTypeKey);
                            }

                            if (entry.getValue().indexOf(SERV_LIST_INTERNET) >= 0) {
                                restMap.put(SERV_LIST_INTERNET + "###" + vlanValue + "###" + conTypeValue, res);
                            }
                        }
                    }
                }
            }
        }
        return restMap;
    }

}
