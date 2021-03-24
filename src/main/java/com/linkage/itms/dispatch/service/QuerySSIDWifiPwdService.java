package com.linkage.itms.dispatch.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.commom.enums.ErrorCodeEnum;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.QueryDevDAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dispatch.obj.QuerySSIDWifiPwdChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 宁夏电信获取终端SSID和wifi密码
 *
 */
public class QuerySSIDWifiPwdService implements IService{
    private static final Logger logger = LoggerFactory.getLogger(QuerySSIDWifiPwdService.class);

    private final String SSID_STR = ".SSID";
    private final String WIFI_PWD_STR = ".PreSharedKey";
    private final String DEV_NUMBER = ".TotalAssociations";
    private QuerySSIDWifiPwdChecker checker;

    @Override
    public String work(String inXml) {
        logger.warn("begin QuerySSIDWifiPwdService.work,inXml=({})",inXml);
        //1、入参解析和合法性校验
        checker = new QuerySSIDWifiPwdChecker(inXml);
        if(!checker.check()){
            logger.warn("QuerySSIDWifiPwdChecker not pass,return=({})",checker.getReturnXml());
            return checker.getReturnXml();
        }

        //2、根据入参用户类型信息获取对应deviceId
        List<HashMap<String, String>> userMap = getUserInfoMap();

        //若用户信息为空 或存在多条用户记录 或未绑定设备 等异常情况直接返回
        if (!checkUserMap(userMap)){
            return checker.getReturnXml();
        }

        //3、校验设备是否是在线状态 只有在线状态才可以采集设备节点信息
        ACSCorba corba = new ACSCorba();
        String deviceId = userMap.get(0).get("device_id");
        //设备正在被操作 或者 设备不在线 直接返回   返回flag=1 表示设备正常在线
        if (!checkDeviceStatus(corba, deviceId)){
            return checker.getReturnXml();
        }

        //4、获取设备节点值 首先获取节点路径 整理需要的所有路径 批量获取路径信息
        //该节点下可能有多个节点
        String path = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.";
        //4.1 获取该节点下的所有节点路径
        List<String> wlanPathList = corba.getParamNamesPath(deviceId,path,0);
        if( null == wlanPathList || wlanPathList.size() == 0){
            checker.setResult(ErrorCodeEnum.SYSTEM_ERROR.getCode());
            checker.setResultDesc(ErrorCodeEnum.SYSTEM_ERROR.getDesc());
            logger.warn("QuerySSIDWifiPwdService with get path null,return=({})",checker.getReturnXml());
            return checker.getReturnXml();
        }

        //4.2 提取出ssid和wifi密码所在路径 若二者路径都不存在 直接返回
        List<String> validPathList = new ArrayList<String>();
        if (!getValidPathList(wlanPathList, validPathList)){
            return checker.getReturnXml();
        }

        //4.3 批量获取这些路径对应的值
        Map<String, String> paramValueMap = corba.getParaValueMap(deviceId, validPathList.toArray(new String[0]));
        if( null == paramValueMap || paramValueMap.isEmpty()){
            checker.setResult(ErrorCodeEnum.PATH_VALUE_NULL.getCode());
            checker.setResultDesc(ErrorCodeEnum.PATH_VALUE_NULL.getDesc());
            logger.warn("QuerySSIDWifiPwdService with get ssid and wifiPwd value null,return=({})",checker.getReturnXml());
            return checker.getReturnXml();
        }

        //5、组装采集的ssid和wifi密码的值返回
        setDataResult(paramValueMap);

        // 记录日志
        new RecordLogDAO().recordDispatchLog(checker, "showWIFIPasswd",checker.getUserInfo());
        logger.warn("cmdId:{}|QuerySSIDWifiPwdService end success with result:{}",checker.getCmdId(),checker.getReturnXml());
        return checker.getReturnXml();
    }

    private List<HashMap<String, String>> getUserInfoMap() {
        QueryDevDAO qdnDao = new QueryDevDAO();
        List<HashMap<String, String>> userMap = new ArrayList<HashMap<String, String>>();
        switch (checker.getUserInfoType()){
            case 1 : {
                //用户宽带账号
                userMap = qdnDao.queryUserByNetAccount(checker.getUserInfo());
                break;
            }
            case 2 : {
                //loid
                userMap = qdnDao.queryUserByLoid(checker.getUserInfo());
                break;
            }
            case 3 :{
                //设备序列号后6位
                userMap = qdnDao.queryUserByDevSN(checker.getUserInfo(),checker.getUserInfo());
                break;
            }
        }
        return userMap;
    }

    private boolean getValidPathList(List<String> wlanPathList, List<String> validPathList) {
        for(String subWlanPath : wlanPathList){
            //SSID路径 wifi密码路径
            if(subWlanPath.endsWith(SSID_STR) || subWlanPath.endsWith(WIFI_PWD_STR) || subWlanPath.endsWith(DEV_NUMBER)){
                validPathList.add(subWlanPath);
            }
        }

        if(validPathList.isEmpty()){
            checker.setResult(ErrorCodeEnum.SYSTEM_ERROR.getCode());
            checker.setResultDesc(ErrorCodeEnum.SYSTEM_ERROR.getDesc());
            logger.warn("QuerySSIDWifiPwdService with ssid and wifiPwd not exist,return=({})",checker.getReturnXml());
            return false;
        }
        logger.warn("cmdId:{}|QuerySSIDWifiPwdService.getValidPathList with get validPath:{}",checker.getCmdId(),validPathList);
        return true;
    }

    private void setDataResult(Map<String, String> paramValueMap) {
        StringBuilder ssidResult = new StringBuilder();
        StringBuilder wifiPwdResult = new StringBuilder();
        StringBuilder devNumberResult = new StringBuilder();
        for (Map.Entry<String, String> entry : paramValueMap.entrySet())
        {
            logger.warn("cmdId:{}|QuerySSIDWifiPwdService path:{},value:{}", checker.getCmdId(),entry.getKey(),entry.getValue());
            String paramName = entry.getKey();
            if(paramName.indexOf(SSID_STR) > 0){
                ssidResult.append(entry.getValue()).append(',');
            }
            if(paramName.indexOf(WIFI_PWD_STR) > 0){
                wifiPwdResult.append(entry.getValue()).append(',');
            }
            if(paramName.indexOf(DEV_NUMBER) > 0){
                devNumberResult.append(entry.getValue()).append(',');
            }
        }
        checker.setSsid(StringUtil.isEmpty(ssidResult.toString()) ? "" : ssidResult.toString().substring(0,ssidResult.toString().length() - 1));
        checker.setWifiPasswd(StringUtil.isEmpty(wifiPwdResult.toString()) ? "" : wifiPwdResult.toString().substring(0,wifiPwdResult.toString().length() - 1));
        checker.setDevNumber(StringUtil.isEmpty(devNumberResult.toString()) ? "" : devNumberResult.toString().substring(0,devNumberResult.toString().length() - 1));
    }


    private boolean checkDeviceStatus(ACSCorba corba, String deviceId) {
        GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
        int flag = getStatus.testDeviceOnLineStatus(deviceId, corba);
        logger.warn("cmdId:{}|QuerySSIDWifiPwdService.checkDeviceStatus with get flag:{}",checker.getCmdId(),flag);
        // 设备正在被操作，不能获取节点值
        if (flag == -3) {
            checker.setResult(ErrorCodeEnum.DEVICE_IS_BUSY.getCode());
            checker.setResultDesc(ErrorCodeEnum.DEVICE_IS_BUSY.getDesc());
            logger.warn("the device is busy now,deviceId={},return=({})", deviceId,checker.getReturnXml());
            return false;
        }
        //设备不在线 直接返回
        if(flag != 1){
            checker.setResult(ErrorCodeEnum.DEVICE_NOT_ONLINE.getCode());
            checker.setResultDesc(ErrorCodeEnum.DEVICE_NOT_ONLINE.getDesc());
            logger.warn("QuerySSIDWifiPwdService with the device not online,deviceId={},return=({})",deviceId,checker.getReturnXml());
            return false;
        }
        return true;
    }

    private boolean checkUserMap(List<HashMap<String, String>> userMap) {
        //用户信息为空
        if ( null == userMap || userMap.isEmpty())
        {
            checker.setResult(ErrorCodeEnum.USER_NOT_EXIST.getCode());
            checker.setResultDesc(ErrorCodeEnum.USER_NOT_EXIST.getDesc());
            logger.warn("QuerySSIDWifiPwdService with the user not exist,return=({})",checker.getReturnXml());
            return false;
        }
        //非用户宽带账号类型 不可存在多条记录
        if (userMap.size() > 1 && checker.getUserInfoType() != 1)
        {
            checker.setResult(ErrorCodeEnum.MORE_DEVICE.getCode());
            checker.setResultDesc(ErrorCodeEnum.MORE_DEVICE.getDesc());
            logger.warn("QuerySSIDWifiPwdService with more records,return=({})",checker.getReturnXml());
            return false;
        }
        //未绑定设备 则返回
        if (StringUtil.isEmpty(userMap.get(0).get("device_id")))
        {
            checker.setResult(ErrorCodeEnum.USER_NO_DEVICE.getCode());
            checker.setResultDesc(ErrorCodeEnum.USER_NO_DEVICE.getDesc());
            logger.warn("QuerySSIDWifiPwdService with the user has no device,return=({})",checker.getReturnXml());
            return false;
        }
        return true;
    }

}
