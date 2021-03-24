package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.LatestOnlineTimeDAO;
import com.linkage.itms.dao.QueryIsMulticastVlanDAO;
import com.linkage.itms.dispatch.obj.LatestOnlineTimeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guankai (AILK No.300401)
 * @version 1.0
 * @category ailk-itms-ItmsService
 * @since 2019/11/26
 */
public class LatestOnlineTimeService implements IService{
    private static Logger logger = LoggerFactory.getLogger(LatestOnlineTimeService.class);
    @Override
    public String work(String inXml) {
        logger.warn("LatestOnlineTimeService==>inXml({})",inXml);

        LatestOnlineTimeChecker checker = new LatestOnlineTimeChecker(inXml);
        if (false == checker.check())
        {
            logger.warn("查询庭网关最新在线时间接口入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
                    new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
            logger.warn("LatestOnlineTimeService==>retParam={}", checker.getReturnXml());
            return checker.getReturnXml();
        }

        logger.warn("查询庭网关最新在线时间接口，入参验证通过，UserInfoType=[{}]，UserInfo=[{}]",
                new Object[] { checker.getUserInfoType(),checker.getUserInfo() });
        String deviceId = "";
        String oui_devSn = "";


        List<HashMap<String,String>> userMapList = null;
        List<HashMap<String,String>> deviceMapList = null;
        LatestOnlineTimeDAO dao = new LatestOnlineTimeDAO();

        if(!Global.G_CityIds.contains(checker.getCityId())){
            logger.warn("未知属地代码");
            checker.setResult(1007);
            checker.setResultDesc("属地非法");
            return checker.getReturnXml();
        }

        // 用户信息类型:1：用户宽带帐号;2：LOID;3：IPTV宽带帐号;4：VOIP业务电话号码;5：VOIP认证帐号
        if(checker.getUserInfoType() == 1)
        {
            userMapList = dao.queryUserByNetAccount(checker.getUserInfo());
        }else if (checker.getUserInfoType() == 2)
        {
            userMapList = dao.queryUserByLoid(checker.getUserInfo());
        }
        else if (checker.getUserInfoType() == 3)
        {
            userMapList = dao.queryUserByIptvAccount(checker.getUserInfo());
        }
        else if (checker.getUserInfoType() == 4)
        {
            userMapList = dao.queryUserByVoipPhone(checker.getUserInfo());
        }
        else if (checker.getUserInfoType() == 5)
        {
            userMapList = dao.queryUserByVoipAccount(checker.getUserInfo());
        }

        if (userMapList == null || userMapList.isEmpty())
        {
            logger.warn("查无此客户");
            checker.setResult(1000);
            checker.setResultDesc("查无此客户");
            return checker.getReturnXml();
        }


        String devSn = checker.getDevSn();
        if(devSn==null || devSn.trim().length()==0){
            if(userMapList.size()>1){
                logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
                checker.setResult(1006);
                checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
                return checker.getReturnXml();
            }else{
                deviceId = StringUtil.getStringValue(userMapList.get(0), "device_id", "");
                if (StringUtil.IsEmpty(deviceId))
                {
                    logger.warn("用户未绑定设备");
                    checker.setResult(1002);
                    checker.setResultDesc("用户未绑定设备");
                    return checker.getReturnXml();
                }


            }
        }else{
            devSn = devSn.trim();
            if(devSn.length()<6){
                logger.warn("设备序列号非法，按设备序列号查询时，查询序列号字段少于6位");
                checker.setResult(1005);
                checker.setResultDesc("设备序列号非法");
                return checker.getReturnXml();
            }else{
                deviceMapList = dao.queryDeviceByDevSN(devSn);
                if(deviceMapList==null || deviceMapList.size()==0){
                    logger.warn("没有查到设备");
                    checker.setResult(1000);
                    checker.setResultDesc("没有查到设备");
                    return checker.getReturnXml();
                }else if(deviceMapList.size()>1){
                    logger.warn("查到多台设备,请输入更多位序列号或完整序列号进行查询");
                    checker.setResult(1006);
                    checker.setResultDesc("查到多台设备,请输入更多位序列号或完整序列号进行查询");
                    return checker.getReturnXml();
                }else{
                    deviceId = StringUtil.getStringValue(deviceMapList.get(0), "device_id", "");
                    boolean flagTemp = false;
                    logger.warn("userMapList:{}",userMapList);
                    logger.warn("deviceId:"+deviceId);
                    for(HashMap<String,String> userMap : userMapList){
                        if(userMap.containsValue(deviceId)){
                            flagTemp = true;
                            break;
                        }
                    }
                    if(false==flagTemp){
                        logger.warn("用户未绑定该设备");
                        checker.setResult(1000);
                        checker.setResultDesc("用户未绑定该设备");
                        return checker.getReturnXml();
                    }
                }
            }
        }


        List<HashMap<String, String>> latestOnlineTime = dao.queryLatestOnlineTimeByDevId(deviceId);
        if(latestOnlineTime==null || latestOnlineTime.size()==0){
            logger.warn("latestOnlineTime为空，device_id={}", deviceId);
            checker.setResult(1000);
            checker.setResultDesc("查询家庭网关最新在线时间数据为0");
            return checker.getReturnXml();
        }


        String latestTime = StringUtil.getStringValue(latestOnlineTime.get(0), "last_time");
        if ("".equals(latestTime)){
            checker.setResult(1000);
            checker.setResultDesc("查询家庭网关最新在线时间结果为空");
        }else{
            checker.setResult(0);
            checker.setResultDesc("成功");
            checker.setLatestOnlineTime(DateUtil.transTime(Long.parseLong(latestTime),"yyyy-MM-dd HH:mm:ss"));
            oui_devSn = StringUtil.getStringValue(latestOnlineTime.get(0), "oui", "")+"-"+StringUtil.getStringValue(latestOnlineTime.get(0), "device_serialnumber", "");
            if(!"-".equals(oui_devSn)) {
                checker.setOui_devSn(oui_devSn);
            }
        }

        return checker.getReturnXml();
    }
}
