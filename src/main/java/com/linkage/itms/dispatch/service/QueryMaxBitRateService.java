package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryMaxBitRateChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author songxq
 * @version 1.0
 * @category
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 * @since 2020/6/17.
 */
public class QueryMaxBitRateService implements IService{

    private static Logger logger = LoggerFactory.getLogger(QueryMaxBitRateService.class);

    @Override
    public String work(String inXml) {
        logger.warn("QueryMaxBitRateService==>inXml({})", inXml);
        boolean hasOneLanFlag = false;
        QueryMaxBitRateChecker checker = new QueryMaxBitRateChecker(inXml);
        if (false == checker.check())
        {
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
                    new Object[] { checker.getCmdId(), checker.getUserInfo(),
                            checker.getReturnXml() });
            return checker.getReturnXml();
        }
        DeviceInfoDAO deviceInfoDAO = new DeviceInfoDAO();
        UserDeviceDAO userDevDao = new UserDeviceDAO();
        // 1：根据用户宽带帐号
        List<HashMap<String, String>> deviceInfoList = null;
        if (1 == checker.getUserInfoType())
        {
            // 1：根据用户宽带帐号
            deviceInfoList = deviceInfoDAO.queryUserByNetAccount(checker.getUserInfo());
        }
        else if (2 == checker.getUserInfoType())
        {
            // 1：根据逻辑SN号
            deviceInfoList = deviceInfoDAO.queryUserByLoid(checker.getUserInfo());
        }
        
        if (null == deviceInfoList || deviceInfoList.size() == 0)
        {
            checker.setResult(1002);
            checker.setResultDesc("查无此客户");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]没有查到设备",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }
        /*if (deviceInfoList.size() > 1)
        {
            checker.setResult(1004);
            checker.setResultDesc("查到多组设备，请使用loid模式进行查询");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]查到多组设备，请使用loid模式进行查询",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }*/
        HashMap<String, String> deviceInfoMap = deviceInfoList.get(0);
        // 设备不存在
        if (null == deviceInfoMap || deviceInfoMap.isEmpty())
        {
            checker.setResult(1003);
            checker.setResultDesc("未绑定设备");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]查无此设备",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }

        // 设备不存在
        if ("".equals(StringUtil.getStringValue(deviceInfoMap, "device_id"))
                || null ==StringUtil.getStringValue(deviceInfoMap, "device_id"))
        {
            checker.setResult(1003);
            checker.setResultDesc("未绑定设备");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]查无此设备",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }

        String deviceId = StringUtil.getStringValue(deviceInfoMap, "device_id");

        GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
        ACSCorba corba = new ACSCorba();
        int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
        if (-6 == flag)
        {
            checker.setResult(1008);
            checker.setResultDesc("设备正在被操作");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]设备正在被操作，无法获取节点值",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }
        if (1 == flag)
        {
            logger.warn("[{}]设备在线，正在进行采集操作", deviceId);
            //"InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{i}.Status"
            String lanPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.";
            String path = "";
            String value = "";
            int count = 0;
            List<String> iList = corba.getIList(deviceId, lanPath);
            if ((null == iList) || (iList.isEmpty()))
            {
                checker.setResult(1009);
                checker.setResultDesc("获取iList失败");
                logger.warn(
                        "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]获取iList失败，返回",
                        new Object[] { checker.getCmdId(), checker.getUserInfo() });
                return checker.getReturnXml();
            }
            logger.warn("[{}]获取iList成功，iList.size={}", deviceId,
                    Integer.valueOf(iList.size()));
            for (String i : iList)
            {
                if(!"1".equals(i))
                {
                    hasOneLanFlag = false;
                    continue;
                }
                hasOneLanFlag = true;
                String maxBitRatePath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
                        + i + ".MaxBitRate";
                String statusPath = "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig."
                        + i + ".Status";

                String []gatherPaths = {statusPath,maxBitRatePath};

                ArrayList<ParameValueOBJ> objLlist = corba.getValue(deviceId,
                        gatherPaths);
                if ((null == objLlist) || (objLlist.isEmpty()))
                {
                    if((null == objLlist) || (objLlist.isEmpty()))
                    {
                        checker.setResult(1009);
                        checker.setResultDesc("获取objLlist失败");
                        logger.warn(
                                "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]获取objLlist失败，返回",
                                new Object[] { checker.getCmdId(), checker.getUserInfo() });
                        return checker.getReturnXml();
                    }
                }
                logger.warn("[{}]获取objLlist成功，objLlist.size={}", deviceId,
                        Integer.valueOf(objLlist.size()));
                for (ParameValueOBJ pvobj : objLlist)
                {
                    path = pvobj.getName();
                    value = pvobj.getValue();
                    checker.setLanPortNum(i);
                    if(path.contains("Status"))
                    {
                        checker.setRstState(value);
                    }
                    if(path.contains("MaxBitRate"))
                    {
                        checker.setMaxBitRate(value);
                    }
                }
            }
            if (!hasOneLanFlag)
            {
                checker.setResult(1010);
                checker.setResultDesc("Lan1口不存在协商速率节点");
                logger.warn(
                        "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]Lan1口不存在协商速率节点",
                        new Object[] { checker.getCmdId(), checker.getUserInfo() });
                return checker.getReturnXml();
            }
        }
        else
        {
            checker.setResult(1007);
            checker.setResultDesc("设备不在线");
            logger.warn(
                    "servicename[QueryMaxBitRateService]cmdId[{}]userInfo[{}]设备不在线",
                    new Object[] { checker.getCmdId(), checker.getUserInfo() });
            return checker.getReturnXml();
        }
        String returnXML = checker.getReturnXml();
        logger.warn("QueryMaxBitRateService==>returnXML:" + returnXML);
        return returnXML;
    }
}
