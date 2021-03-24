package com.linkage.itms.dispatch.gsdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.gsdx.beanObj.*;
import com.linkage.itms.dispatch.gsdx.dao.PublicDAO;
import com.linkage.itms.dispatch.gsdx.obj.GetCpeOnlineInfoDealXML;
import com.linkage.itms.dispatch.util.ConfigUtil;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author guankai (AILK No.300401)
 * @version 1.0
 * @category ailk-itms-ItmsService
 * @since 2019/7/24
 */
public class GetCpeOnlineInfoForDslService extends ServiceFather {
    public GetCpeOnlineInfoForDslService(String methodName) {
        super(methodName);
    }

    private static Logger logger = LoggerFactory.getLogger(GetCpeOnlineInfoForDslService.class);

    //基本信息
    private CpeInfo cpeInfo = new CpeInfo();
    //用户信息
    private UserInfo userInfo = new UserInfo();

    private PublicDAO dao = new PublicDAO();

    private List<Para> paraList = new ArrayList<Para>();

    private GetCpeOnlineInfoDealXML dealXML;

    private CpeOnlineInfoRst result = new CpeOnlineInfoRst();

    private ACSCorba corba = new ACSCorba();

    public CpeOnlineInfoRst work(String inXml) {
        logger.warn(methodName + "执行，入参为：{}", inXml);
        dealXML = new GetCpeOnlineInfoDealXML(methodName);
        // 验证入参
        if (null == dealXML.getXML(inXml)) {
            logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证没通过[{}]", dealXML.returnXML());
            result.setiOpRst(-1);
            return result;
        }
        logger.warn(methodName + "[" + dealXML.getOpId() + "]入参验证通过.");
        ArrayList<HashMap<String, String>> baseList = dao.queryUserDevByUser(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
        logger.warn(methodName + "[" + dealXML.getOpId() + "],根据条件查询结果{}", baseList.toString());
        if (null == baseList || baseList.size() == 0) {
            result.setiOpRst(-1);
            return result;
        } else {
            HashMap<String, String> baseMap = new HashMap<String, String>();
            //若设备有宽带则插入宽带账号，否则为空
            for (int i = 0; i < baseList.size(); i++) {
                if ("10".equals(StringUtil.getStringValue(baseList.get(i), "serv_type_id"))) {
                    baseMap = baseList.get(i);
                } else {
                    baseMap = baseList.get(0);
                    baseMap.put("accessno", "");
                }
            }
            logger.warn(methodName + "[" + dealXML.getOpId() + "],终端信息{}", baseMap.toString());
            String deviceId = StringUtil.getStringValue(baseMap, "device_id");
            cpeInfo.setUserId(StringUtil.getStringValue(baseMap, "user_id"));

            cpeInfo.setAccessNo(StringUtil.getStringValue(baseMap, "accessno"));

            cpeInfo.setDeviceID(deviceId);
            //判断设备编码是否为空
            if (deviceId == null || "".equals(deviceId)) {
                logger.warn(methodName + "[" + dealXML.getOpId() + "],device_id{}", deviceId);
                result.setiOpRst(-1);
                return result;
            }
            List<HashMap<String, String>> deviceList = dao.getDeviceInfo(deviceId);

            //判断是否能查到设备信息
            if (deviceList.size() <= 0) {
                logger.warn(methodName + "[" + dealXML.getOpId() + "],deviceList{}", deviceList);
                result.setiOpRst(-1);
                return result;
            }

            HashMap<String, String> deviceMap = deviceList.get(0);
            paraList.add(setPara("adAccounts", StringUtil.getStringValue(baseMap, "accessno")));//宽带账号
            paraList.add(setPara("areaCode", StringUtil.getStringValue(deviceMap, "city_id")));//地区码
            paraList.add(setPara("registerTime", StringUtil.getStringValue(deviceMap, "last_time")));//上报时间
            paraList.add(setPara("cpeManufacturer", StringUtil.getStringValue(deviceMap, "vendor_name")));//终端厂商
            paraList.add(setPara("cpeType", StringUtil.getStringValue(deviceMap, "device_model")));//终端型号
            paraList.add(setPara("cpeVersion", StringUtil.getStringValue(deviceMap, "hardwareversion")));//终端版本

            // 获取设备类型是gpon or epon
            String accessType = PublicDAO.getAccType(deviceId);
            if (null == accessType || accessType.isEmpty()) {
                logger.warn("[{}]开始从设备获取上行方式", deviceId);
                ConfigUtil cu = new ConfigUtil();
                accessType = cu.getAccessType(deviceId, true);
            }
            ArrayList<String> paramNameList = new ArrayList<String>();
            String parPath = "";
            if ("ADSL".equals(accessType)) {
                paramNameList.add("InternetGatewayDevice.WANDevice.1.WANCommonInterfaceConfig.WANAccessType");
                parPath = "InternetGatewayDevice.WANDevice.1.WANDSLInterfaceConfig.";
            } else if ("EPON".equals(accessType)) {
                parPath = "InternetGatewayDevice.WANDevice.1.X_CT-COM_EponInterfaeConfig.";
            } else if ("GPON".equals(accessType)) {
                parPath = "InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.";
            } else if ("LAN".equals(accessType)) {
            }
            if (!"".equals(parPath)) {

                ArrayList<String> paramChindrenList = new ArrayList<String>();
                paramChindrenList = corba.getParamNamesPath(deviceId, parPath, 0);

                logger.warn("{}，{} 设备子节点获取结果[{}]", deviceId, parPath, paramChindrenList);

                for (int i = 0; i < paramChindrenList.size(); i++) {
                    String namepath = paramChindrenList.get(i);
                    if (!namepath.endsWith(".")) {
                        paramNameList.add(namepath);
                    }
                }
                if (paramNameList.size() > 0) {
                    String[] gatherPathArray = new String[paramNameList.size()];
                    paramNameList.toArray(gatherPathArray);
                    // 处理设备采集结果
                    ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId, gatherPathArray);
                    if (null == objLlist || objLlist.isEmpty()) {
                        logger.warn("{}|[{}],采集{}上行失败，device_id={}", methodName,dealXML.getOpId(),accessType, deviceId);
                    } else {
                        for (ParameValueOBJ pvobj : objLlist) {
                            paraList.add(setPara(pvobj.getName(), pvobj.getValue()));
                        }
                    }
                }
            }


            Para[] paraArray = new Para[paraList.size()];
            paraList.toArray(paraArray);
            cpeInfo.setParaList(paraArray);
            result.setCpeInfo(cpeInfo);
            result.setiOpRst(1);
            return result;
        }

    }


}
