package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.util.GetDeviceOnLineStatus;
import com.linkage.itms.dao.DeviceInfoDAO;
import com.linkage.itms.obj.ParameValueOBJ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年9月24日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class SetWIFIPasswdService implements IService
{
	private static Logger logger = LoggerFactory.getLogger(SetWIFIPasswdService.class); 
	@Override
	public String work(String inXml)
	{
		logger.warn("SetWIFIPasswdService => param:{}",inXml);
		SetWIFIPasswdServiceChecker checker = new SetWIFIPasswdServiceChecker(inXml);
		if(false == checker.check()){
			logger.warn(
					"servicename[SetWIFIPasswdService]cmdId[{}]userInfo[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getUserInfo(),
							checker.getReturnXml() });
			return checker.getReturnXml();
		}
		
		DeviceInfoDAO dao = new DeviceInfoDAO();
		List<HashMap<String,String>> devMaps = dao.qryDevInfo(checker.getUserInfoType(), checker.getUserInfo());
		if(null == devMaps || devMaps.isEmpty() || devMaps.size() < 1){
			logger.warn("SetWIFIPasswdService =>userinfo[{}] 此用户未绑定 ",checker.getUserInfo());
			checker.setResult(1004);
			checker.setResultDesc("此用户或设备未绑定");
			return checker.getReturnXml();
		}
		//查看查询信息是否多条
		if(devMaps.size() > 1){
			logger.warn("SetWIFIPasswdService =>userinfo[{}] 查询出多个设备 ",checker.getUserInfo());
			checker.setResult(1014);
			checker.setResultDesc("查询出多个设备");
			return checker.getReturnXml();
		}
		
		//查询设备是否正在做业务下发
		String devId = devMaps.get(0).get("device_id");
		if(dao.isDevDoing(devId)){
			logger.warn("SetWIFIPasswdService =>userinfo[{}],devId[{}]设备正在被操作，不能正常交互 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1013);
			checker.setResultDesc("设备正在被操作，不能正常交互");
			return checker.getReturnXml();
		}
		
		//下发节点
		int code = preNode(devId,checker.getSsid(),checker.getWifiPasswd());
		if(code == 0 || code == 1){
			logger.warn("SetWIFIPasswdService =>userinfo[{}],devId[{}]节点下发成功 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(0);
			checker.setResultDesc("成功");
			return checker.getReturnXml();
		}else if(code == -1){
			logger.warn("SetWIFIPasswdService =>userinfo[{}],devId[{}]设备不在线",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1012);
			checker.setResultDesc("设备不在线");
			return checker.getReturnXml();
		}else if(code == -2){
			checker.setResult(1002);
			checker.setResultDesc("或者设备实例失败");
			return checker.getReturnXml();
		}else if( code == -6){
			logger.warn("SetWIFIPasswdService =>userinfo[{}],devId[{}]设备正在被操作，不能正常交互 ",new Object[]{checker.getUserInfo(),devId});
			checker.setResult(1013);
			checker.setResultDesc("设备正在被操作，不能正常交互");
			return checker.getReturnXml();
		}else{
			logger.warn("SetWIFIPasswdService =>userinfo[{}],devId[{}],code[{}]系统内部错误 ",new Object[]{checker.getUserInfo(),devId,code});
			checker.setResult(1000);
			checker.setResultDesc("系统内部错误");
			return checker.getReturnXml();
		}
		
	}
	
	public int preNode(String devId,String preSsid,String pwd){
		//SSID
		String lanDevice = "InternetGatewayDevice.LANDevice.";
		String wanConfig = "WLANConfiguration.";
		String ssid = "SSID";
		String wifiNode = "PreSharedKey";
		
		ACSCorba acsCorba = new ACSCorba();
		List<String> ssidPathList = new ArrayList<String>(); //保存ssid
		List<String> PreSharedKeyList = new ArrayList<String>(); //保存PreSharedKey节点
		List<String> wifyPathNodeList = new ArrayList<String>(); //下发的wifi密码
		ArrayList<ParameValueOBJ> preNode = new ArrayList<ParameValueOBJ>();//节点下发
		
		//检测在线状态
		GetDeviceOnLineStatus getStatus = new GetDeviceOnLineStatus();
		int flag = getStatus.testDeviceOnLineStatus(devId, acsCorba);
		if(flag != 1){
			return -1; 
		}
		
		//预读获取lanDevice下所有的节点
		ArrayList<String> lanDevicePaths = acsCorba.getParamNamesPath(devId, lanDevice, 0);
		if(null == lanDevicePaths || lanDevicePaths.isEmpty() || lanDevicePaths.size() < 1 ){
			logger.warn("preNode == > 获取{}实例失败",lanDevice);
			return -2;
		}
		
		//循环获取需下发的节点路径
		for(String lanPath : lanDevicePaths){
			if(lanPath.contains(wanConfig) && lanPath.contains("."+ssid)){
				ssidPathList.add(lanPath);
			}
			
			if(lanPath.contains(wanConfig) && lanPath.contains(wifiNode) && (search(wifiNode, lanPath) == 2)){
				PreSharedKeyList.add(lanPath);
			}
		}
		logger.warn("ssidPathList：{}",ssidPathList); 
		logger.warn("PreSharedKeyList：{}",PreSharedKeyList); 
		
		//获取ssid组装需要下发的ssid
		ArrayList<ParameValueOBJ> ssidVals = acsCorba.getValue(devId, ssidPathList.toArray(new String[ssidPathList.size()]));
		if(null != ssidVals && !ssidVals.isEmpty()){
			for(ParameValueOBJ obj : ssidVals){
				String head = "";
				String end = "";
				String mid = "";
				String ssidNodeVal = obj.getValue();
			    logger.warn("ssidNodeVal => {}",ssidNodeVal);
				if(ssidNodeVal.contains("ChinaNet-")){
					if(!StringUtil.IsEmpty(preSsid)){
						head = "ChinaNet-";
						mid = ssidNodeVal.replace(head, "");
						if(mid.contains("-5G")){
							end = "-5G";
							mid = mid.replace(end, "");
						}
						mid = preSsid;
						obj.setValue(head + mid + end);
						preNode.add(obj);
					    logger.warn("ssidNodeVal get => {}",obj.getValue());
					}
					
					//获取对应节点下面的wifi密码的节点
					if(!StringUtil.IsEmpty(pwd)){
						String PreSharedKeyNode = obj.getName().replace(ssid, "") + wifiNode + ".";
						for(String node : PreSharedKeyList){
							if(node.contains(PreSharedKeyNode)){
								wifyPathNodeList.add(node);
							}
						}
					}
					
				} 
			}
		}
		logger.warn("preNode => {}",preNode);
		logger.warn("wifyPathNodeList => {}",wifyPathNodeList);
		//组装wifi密码
		if(!StringUtil.IsEmpty(pwd) && null != wifyPathNodeList && !wifyPathNodeList.isEmpty()){
			for(String wifiPwdPath : wifyPathNodeList){
				ParameValueOBJ obj = new ParameValueOBJ();
				obj.setName(wifiPwdPath);
				obj.setValue(pwd);
				preNode.add(obj);
			}
		}
		
		logger.warn("下发节点：{}",preNode);
		if(null != preNode && !preNode.isEmpty() && preNode.size() > 0){
			logger.warn("[{}]开始下发......",devId);
			return acsCorba.setValue(devId, preNode);
		}
		logger.warn("无参数节点需要下发");
		return 1000; 
	}
	
	/**
	 * 查找字符串里与指定字符串相同的个数
	 * @param desNode
	 * @param path
	 * @return
	 */
    public static int search(String desNode,String path) {
        int n=0;//计数器
        while(path.indexOf(desNode)!=-1) {
            int i = path.indexOf(desNode);
            n++;
            path = path.substring(i+1);
        }
        return n;
    }
	
	public static void main(String args[]){
		/*String lanPath = "InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey";
		String wanConfig = "WLANConfiguration.";
		if(lanPath.contains(wanConfig) && lanPath.contains(wanConfig) && search("PreSharedKey", lanPath) == 2){
			System.out.println(lanPath); 
		}*/
		
		String path = "ChinaNet-kXaH-5G";
		System.out.println(path.replace("ChinaNet-", "")); 
	}
	
}
