package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.HbdxIptvSetValueDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.HbdxIptvSetValueChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class HbdxIptvSetValueService implements IService {
    private static Logger logger = LoggerFactory.getLogger(HbdxIptvSetValueService.class);

    private static String X_CT_COM_MODE = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.X_CT-COM_Mode";

    private static String X_CT_COM_VLAN = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1.X_CT-COM_VLAN";

    @Override
    public String work(String inXml) {
        logger.warn("HbdxIptvSetValueService==>inXml({})",inXml);
        HbdxIptvSetValueChecker checker = new HbdxIptvSetValueChecker(inXml);
        if (false == checker.check())
        {
            logger.warn("湖北电信ITV新增节点设置信息接口入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
                    new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
            logger.warn("HbdxIptvSetValueService==>retParam={}", checker.getReturnXml());
            return checker.getReturnXml();
        }

        HbdxIptvSetValueDAO dao = new HbdxIptvSetValueDAO();
        if(!dao.isExistLoid(checker.getUserInfo()))
        {
            logger.warn("逻辑SN [{}] 不存在。",checker.getUserInfo());
            checker.setResult(1010);
            checker.setResultDesc("逻辑SN不存在");
            return checker.getReturnXml();
        }

        if(!dao.isBindDevice(checker))
        {
            logger.warn("逻辑SN [{}] 未绑定设备。",checker.getUserInfo());
            checker.setResult(1003);
            checker.setResultDesc("未绑定设备");
            return checker.getReturnXml();
        }


        HashMap<String,String> itvServInfoMap = dao.getItvServInfo(checker.getUserInfo());
        if(null != itvServInfoMap && itvServInfoMap.size() > 0)
        {
            logger.warn("[{}] ITV业务已开通 。",checker.getUserInfo());
        }
        else
        {
            logger.warn("逻辑SN [{}] 无开通成功的ITV业务。",checker.getUserInfo());
            checker.setResult(1009);
            checker.setResultDesc("无开通成功的ITV业务");
            return checker.getReturnXml();
        }
        logger.warn("[{}] 校验已完成，开始进行新增节点的下发。",checker.getUserInfo());

        GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
        ACSCorba acsCorba = new ACSCorba();
        String deviceId = checker.getDeviceId();

        int flag = getStatus.testDeviceOnLineStatus(deviceId, acsCorba);
        // 设备正在被操作，不能获取节点值
        if (-3 == flag)
        {
            logger.warn("设备正在被操作，无法获取节点值，device_id={}", deviceId);
            checker.setResult(1012);
            checker.setResultDesc("设备正在被操作，不能正常交互");
            logger.warn("return=({})", checker.getReturnXml()); // 打印回参
            return checker.getReturnXml();
        }
        // 设备在线
        else if (1 == flag)
        {
            String vlanId = StringUtil.getStringValue(itvServInfoMap.get("vlanid"));
            String vlan = "43/"+vlanId;
            ParameValueOBJ pvOBJ = new ParameValueOBJ();
            pvOBJ.setName(X_CT_COM_MODE);
            pvOBJ.setValue("1");
            // 设置参数的类型为3：unsignedInt（电信规范文档中信道 Channel 参数的类型为 unsignedInt）
            pvOBJ.setType("3");
            int setModeResult =  acsCorba.setValue(deviceId,pvOBJ);
            if (0 == setModeResult || 1 == setModeResult)
            {
                pvOBJ = new ParameValueOBJ();
                pvOBJ.setName(X_CT_COM_VLAN);
                pvOBJ.setValue(vlan);
                // 设置参数的类型为3：unsignedInt（电信规范文档中信道 Channel 参数的类型为 unsignedInt）
                pvOBJ.setType("3");

                int setVlanResult =  acsCorba.setValue(deviceId,pvOBJ);
                if (0 == setVlanResult || 1 == setVlanResult)
                {
                    checker.setResult(0);
                    checker.setResultDesc("成功");
                    String returnXml = checker.getReturnXml();
                    // 记录日志
                    new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                            "HbdxIptvSetValueService");
                    logger.warn(
                            "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                            new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                    returnXml });
                    return returnXml;
                }
                else if (-1 == setVlanResult || -6 == setVlanResult)
                {
                    checker.setResult(1000);
                    checker.setResultDesc("设备不能正常交互");
                    String returnXml = checker.getReturnXml();
                    // 记录日志
                    new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                            "HbdxIptvSetValueService");
                    logger.warn(
                            "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                            new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                    returnXml });
                    return returnXml;
                }
                else if (-7 == setVlanResult)
                {
                    checker.setResult(1000);
                    checker.setResultDesc("系统参数错误");
                    String returnXml = checker.getReturnXml();
                    // 记录日志
                    new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                            "HbdxIptvSetValueService");
                    logger.warn(
                            "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                            new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                    returnXml });
                    return returnXml;
                }
                else if (-9 == setVlanResult)
                {
                    checker.setResult(1000);
                    checker.setResultDesc("系统内部错误");
                    String returnXml = checker.getReturnXml();
                    // 记录日志
                    new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                            "HbdxIptvSetValueService");
                    logger.warn(
                            "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                            new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                    returnXml });
                    return returnXml;
                }
                else
                {
                    checker.setResult(1000);
                    checker.setResultDesc("TR069错误");
                    String returnXml = checker.getReturnXml();
                    // 记录日志
                    new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                            "HbdxIptvSetValueService");
                    logger.warn(
                            "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                            new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                    returnXml });
                    return returnXml;
                }

            }
            else if (-1 == setModeResult || -6 == setModeResult)
            {
                checker.setResult(1000);
                checker.setResultDesc("设备不能正常交互");
                String returnXml = checker.getReturnXml();
                // 记录日志
                new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                        "HbdxIptvSetValueService");
                logger.warn(
                        "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                        new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                returnXml });
                return returnXml;
            }
            else if (-7 == setModeResult)
            {
                checker.setResult(1000);
                checker.setResultDesc("系统参数错误");
                String returnXml = checker.getReturnXml();
                // 记录日志
                new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                        "HbdxIptvSetValueService");
                logger.warn(
                        "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                        new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                returnXml });
                return returnXml;
            }
            else if (-9 == setModeResult)
            {
                checker.setResult(1000);
                checker.setResultDesc("系统内部错误");
                String returnXml = checker.getReturnXml();
                // 记录日志
                new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                        "HbdxIptvSetValueService");
                logger.warn(
                        "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                        new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                returnXml });
                return returnXml;
            }
            else
            {
                checker.setResult(1000);
                checker.setResultDesc("TR069错误");
                String returnXml = checker.getReturnXml();
                // 记录日志
                new RecordLogDAO().recordDispatchLog(checker, checker.getUserInfo(),
                        "HbdxIptvSetValueService");
                logger.warn(
                        "servicename[HbdxIptvSetValueService]cmdId[{}]userinfo[{}]处理结束，返回响应信息:{}",
                        new Object[] { checker.getCmdId(), checker.getUserInfo(),
                                returnXml });
                return returnXml;
            }
        }
        else
        {
            logger.warn("设备不在线，无法获取节点值,device_id={}", deviceId);
            checker.setResult(1011);
            checker.setResultDesc("设备不在线");
            logger.warn("return=({})", checker.getReturnXml());
            return checker.getReturnXml();
        }
    }
}
