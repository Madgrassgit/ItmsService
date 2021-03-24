package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.cao.DevOnlineCAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.obj.QueryWanTypeChecker;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 吉林联通采集上网方式
 * 由于吉林联通不进行宽带节点的下发 节点值是运维直接改 所以这里需要采集设备获取
 * created by lingmin on 2020/05/21
 */
public class QueryWanTypeJLLTService implements IService{

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryWanTypeJLLTService.class);
    private UserDeviceDAO userDevDao = new UserDeviceDAO();
    private ACSCorba corba = new ACSCorba();
    private String deviceId = null;
    private static final String BAND_VLANID = "44";

    @Override
    public String work(String inXml) {
        //1、入参合法性校验
        QueryWanTypeChecker checker = new QueryWanTypeChecker(inXml);
        if (!checker.check()) {
            LOGGER.error("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}]验证未通过，返回：{}",
                    checker.getCmdId(), checker.getUserInfo(), checker.getReturnXml());
            return checker.getReturnXml();
        }

        //2、处理用户信息 校验用户信息是否存在
        Map<String, String> userInfoMap = userDevDao.queryUserInfo(checker.getUserInfoType(), checker.getUserInfo());
        if (null == userInfoMap || userInfoMap.isEmpty()) {
            LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}]无此用户",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return getResult(checker,1001,"无此用户信息");
        }

        //3、用户信息存在 获取设备id
        deviceId = StringUtil.getStringValue(userInfoMap, "device_id");
        if (StringUtil.IsEmpty(deviceId)) {
            LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}]未绑定设备",
                    new Object[]{checker.getCmdId(), checker.getUserInfo()});
            return getResult(checker,1002,"此用户未绑定设备");
        }

        //4、设备id存在 开始采集节点
        LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}]开始采集[{}]",
                checker.getCmdId(), checker.getUserInfo(), deviceId);

        //4.1 检查设备是否在线 不在线则组装返回
        if (!checkDevOnline(checker)){
            return checker.getReturnXml();
        }

        //4.2 设备在线 组装需要采集的节点路径 这里先采集WAN口下所有的i值
        String wanPath = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.";
        List<String> iList = corba.getIList(deviceId, wanPath);
        LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}] gather WAN iList end，deviceId:{},iList:{}", checker.getCmdId(), checker.getUserInfo(), deviceId, iList);
        if (null == iList || iList.isEmpty()) {
            //获取i值失败 组装返回
            return getResult(checker,1000,"节点采集失败");
        }

        //4.3 遍历i 进行VLANID节点采集：i.X_CU_VLAN  取节点值为44的节点
        String chosedI = getChosedI(checker, wanPath, iList);
        if(chosedI.equals("0")){
            return getResult(checker,1000,"未采集到有效的上网方式节点");
        }

        //4.4 取X_CU_VLAN为44 所在的i设备下采集WANPPPConnection.1.ConnectionType节点值
        return getWanTypeReturn(checker, wanPath, chosedI);
    }

    private String getResult(QueryWanTypeChecker checker,int code,String msg){
        checker.setResult(code);
        checker.setResultDesc(msg);
        return checker.getReturnXml();
    }

    private String getWanTypeReturn(QueryWanTypeChecker checker, String wanPath, String chosedI) {
        String connectionTypePath = wanPath + chosedI + ".WANPPPConnection.1.ConnectionType";
        ArrayList<ParameValueOBJ> wanTypeList = corba.getValue(deviceId, connectionTypePath);
        LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}] gather wanType end，deviceId:{},value:{}",
                checker.getCmdId(), checker.getUserInfo(), deviceId,wanTypeList);
        if(wanTypeList == null || wanTypeList.size() == 0){
            checker.setResult(1000);
            checker.setResultDesc("采集上网方式失败");
            return checker.getReturnXml();
        }
        //IP_Routed-2   PPPoE_Bridged-1
        int wanTypeCode = 1;
        String wanType = wanTypeList.get(0).getValue();
        if(wanType.equals("IP_Routed")){
            wanTypeCode = 2;
        }
        checker.setWanType(wanTypeCode);
        checker.setResult(0);
        checker.setResultDesc("成功");
        return checker.getReturnXml();
    }

    private String getChosedI(QueryWanTypeChecker checker, String wanPath, List<String> iList) {
        String chosedI = "0";
        for(String i : iList){
            String vlanIdPath = wanPath + i + ".X_CU_VLAN";
            ArrayList<ParameValueOBJ> vlanIdList = corba.getValue(deviceId, vlanIdPath);
            LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}] gather vlanId，deviceId:{},path:{},vlanIdList:{}",
                    checker.getCmdId(), checker.getUserInfo(), deviceId,vlanIdPath,vlanIdList);
            if(vlanIdList == null || vlanIdList.size() == 0){
                continue;
            }
            if(vlanIdList.get(0).getValue().equals(BAND_VLANID)){
                chosedI = i;
                break;
            }
        }
        return chosedI;
    }

    private boolean checkDevOnline(QueryWanTypeChecker checker) {
        int onlineStatus = DevOnlineCAO.devOnlineTest(deviceId);
        LOGGER.warn("[QueryWanTypeJLLTService]cmdId[{}] userinfo[{}]get onlineStatus[{}]",
                checker.getCmdId(), checker.getUserInfo(), onlineStatus);
        if (onlineStatus == -3) {
            checker.setResult(1003);
            checker.setResultDesc("设备正在被操作，无法读取！");
            return false;
        }
        if (onlineStatus != 1) {
            checker.setResult(1004);
            checker.setResultDesc("设备不在线，无法读取！");
            return false;
        }
        return true;
    }
}
