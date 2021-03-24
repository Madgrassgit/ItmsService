package com.linkage.stbms.pic.service;

import com.linkage.WSClient.WSClientProcess;
import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.cao.ACSCorba;
import com.linkage.stbms.cao.ParameValueOBJ;
import com.linkage.stbms.itv.main.Global;
import com.linkage.stbms.pic.bio.ApkSetDownBio;
import com.linkage.stbms.pic.object.StbDeviceOBJ;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 江西 APK 系统调用ITV终端网管平台下发业务接口处理类
 * 
 * @param xmlData
 * @return
 */
public class ApkSetDownService {

	private final Logger logger = LoggerFactory.getLogger(ApkSetDownService.class);

	public String work(String inXml) {
		// 检查合法性
		ApkSetDownChecker checker = new ApkSetDownChecker(inXml);
		if (false == checker.check()) 
		{
			logger.error("servicename[ApkSetDownService]cmdId[{}]验证未通过，返回：[{}]",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn("servicename[ApkSetDownService]cmdId[{}]参数校验通过，入参为：[{}]",
				new Object[] { checker.getCmdId(), inXml });

		ApkSetDownBio apkBio = new ApkSetDownBio();
		Map<String, String> servInfoMap = apkBio.getUserInfoyByServAccount(checker.getUsername());
		if (null == servInfoMap || servInfoMap.isEmpty()) 
		{
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]业务账号不存在",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("业务账号不存在");
			apkBio.saveApkRes(checker);
			return checker.getReturnXml();
		}

		Map<String, String> devInfoMap = apkBio.getdevInfoByMac(checker.getMac());
		if (null == devInfoMap || devInfoMap.isEmpty()) {
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]设备不存在",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("设备不存在");
			apkBio.saveApkRes(checker);
			return checker.getReturnXml();
		}

		String cpeMacUser = StringUtil.getStringValue(servInfoMap, "cpe_mac", "");
		if (!cpeMacUser.equalsIgnoreCase(checker.getMac())) 
		{
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]账号和设备不存在绑定关系",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("账号和设备不存在绑定关系");
			apkBio.saveApkRes(checker);
			return checker.getReturnXml();
		}
		StbDeviceOBJ stbDeviceOBJ = new StbDeviceOBJ();
		stbDeviceOBJ.setStbMac(checker.getMac());
		stbDeviceOBJ.setDeviceId(StringUtil.getStringValue(servInfoMap, "device_id", ""));
		String pppoeUser = getItvAccountsFrItms(stbDeviceOBJ);
		if (pppoeUser == null || !pppoeUser.equals(checker.getPppoeuser()))
		{
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]接入账号不一致",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("接入账号不一致");
			apkBio.saveApkRes(checker);
			return checker.getReturnXml();
		}

		String newPwd = StringUtil.getStringValue(servInfoMap, "serv_pwd", "");
		if (StringUtil.IsEmpty(newPwd)) 
		{
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]用户业务密码为空",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("用户业务密码为空");
			apkBio.saveApkRes(checker);
			return checker.getReturnXml();
		}

		String inParm = apkBio.getInParam(checker.getCmdId(), checker.getUsername(), newPwd);
		logger.warn("调用修改密码接口参数：" + inParm);
		// 调用接口方法
		String resltFromItms = WSClientProcess.accessService(Global.JX_MODIFY_PWD_URL, checker.getUsername(),
				newPwd, checker.getCmdId(), Global.JX_MODIFY_PWD_USERNAME, Global.JX_MODIFY_PWD_PASSWORD);
		
		logger.warn("调用修改密码接口返回结果：" + resltFromItms);
		
		if ("0".equals(resltFromItms)) 
		{
			logger.warn("调用Corba，设置节点值,[{}]",StringUtil.getStringValue(devInfoMap, "device_id", ""));
			ACSCorba corba = new ACSCorba();
			ArrayList<ParameValueOBJ> objList = apkBio.getObjList(newPwd);
			int flag = corba.setValue(StringUtil.getStringValue(devInfoMap, "device_id", ""), objList);
			
			if (1 == flag) 
			{
				logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]下发业务密码成功",
						new Object[] { checker.getUsername(), checker.getCmdId() });
				checker.setResult(0);
				checker.setResultDesc("下发业务密码成功");
				corba.reboot(StringUtil.getStringValue(devInfoMap, "device_id", ""));
			} else {
				logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]下发业务密码失败",
						new Object[] { checker.getUsername(), checker.getCmdId() });
				checker.setResult(1005);
				checker.setResultDesc("获取RPC命令失败");
				apkBio.saveApkRes(checker);
				
				return checker.getReturnXml();
			}

		} else {
			logger.warn("servicename[ApkSetDownService]searchInfo[{}]cmdId[{}]调用修改密码接口失败",
					new Object[] { checker.getUsername(), checker.getCmdId() });
			checker.setResult(1001);
			checker.setResultDesc("调用修改密码接口失败");
			apkBio.saveApkRes(checker);
			
			return checker.getReturnXml();
		}

		apkBio.saveApkRes(checker);
		logger.warn("servicename[ApkSetDownService]cmdId[{}]searchInfo[{}]处理结束，回参为：[{}]",
				new Object[] { checker.getCmdId(), checker.getUsername(), checker.getReturnXml() });
		
		return checker.getReturnXml();
	}
	
	
	/**
	 * 调用ITMS WebService接口获取itv接入账号<br>
	 * 设备mac地址不能为空，为空直接返回null
	 * 
	 * @param url
	 * @param inParam
	 * @param method
	 * @return
	 */
	private String getItvAccountsFrItms(StbDeviceOBJ stbDeviceOBJ)
	{
		if (StringUtil.IsEmpty(stbDeviceOBJ.getStbMac()))
		{
			logger.warn("[zerocfg-{}]机顶盒mac[{}]地址为空，不能ITMS获取接入账号",new Object[]{
					stbDeviceOBJ.getDeviceId(), stbDeviceOBJ.getStbMac()});
			return null;
		}
		// 瓶装入参
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("root");
		root.addElement("CmdID").addText("123456789012345");
		root.addElement("CmdType").addText("CX_01");
		root.addElement("ClientType").addText("3");
		Element param = root.addElement("Param");
		param.addElement("mac").addText(stbDeviceOBJ.getStbMac());
		logger.warn("[zerocfg-{}]调用ITMS接口参数：" + doc.asXML(),
				new Object[] { stbDeviceOBJ.getDeviceId()});
		// 调用接口方法，查看ailk-itms-ItmsService项目GetItvAccountBIO.getItvAccount方法
		logger.warn("url :"+Global.STBSERVICE_URL);
		String resltFromItms = WSClientProcess.callItmsService(Global.STBSERVICE_URL,
				doc.asXML(), "getStbPppoeAccount");
		/*String resltFromItms = WSClientProcess.callItmsService(Global.ITMS_URL,doc.asXML(),"getItvAccount");*/
		logger.warn("[zerocfg-{}]调用ITMS接口获取itv接入账号返回结果：" + resltFromItms,
				new Object[] { stbDeviceOBJ.getDeviceId()});
		//String username = "";
		String usernameListStr = "";
		//String customerId = "";
		//List<HashMap<String, String>> userList = new ArrayList<HashMap<String, String>>();		
		//List<HashMap<String, String>> userListTemp = new ArrayList<HashMap<String, String>>();
		
		// 解析回参
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(resltFromItms));
			Element root2 = document.getRootElement();
			Iterator accountIter = root2.element("accounts").elementIterator("account");
			while (accountIter.hasNext()) {
				Element accElement = (Element) accountIter.next();
				String usernameTemp = accElement.elementTextTrim("username");
				if (!StringUtil.IsEmpty(usernameTemp)) {
					// 江西逻辑：从itms查询的账号如果不为空，则增加@itv后进行查询
					usernameListStr +=  usernameTemp + "@itv" + ",";
				}
			}
			
			if(!StringUtil.IsEmpty(usernameListStr) && usernameListStr.endsWith(",")){
				usernameListStr = usernameListStr.substring(0, usernameListStr.length()-1);
				//stbDeviceOBJ.setUsernameListStr(usernameListStr);
			}
			
			logger.warn("[zerocfg-{}]-ITMS返回接入账号为-[usernameListStr-{}]", stbDeviceOBJ.getDeviceId(), usernameListStr);
			
			/*userListTemp = bind4sdao.getUserByPppoeList(usernameListStr);					
			if (null != userListTemp && !userListTemp.isEmpty())
			{				
				for(HashMap<String,String> map : userListTemp){
					// 根据用户ID判断用户是否绑定
					customerId = StringUtil.getStringValue(map, "customer_id");
					if(!bind4sdao.isUserUnbindDevice(customerId)){
						logger.warn("[zerocfg-{}]-[customerId-{}] 根据接入账号ITMS查询机顶盒用户-[pppoe_user-{}]-已经绑定",
								stbDeviceOBJ.getDeviceId(), customerId, StringUtil.getStringValue(map, "pppoe_user"));
					}else{
						username = StringUtil.getStringValue(map, "pppoe_user");
						userList.add(map);
						stbDeviceOBJ.setUserList(userList);
						break;
					}
				}
			}
			else{
				logger.warn("[zerocfg-{}]-根据接入账号ITMS查询机顶盒用户-[usernameListStr-{}]-查询机顶盒用户为空",
						stbDeviceOBJ.getDeviceId(), usernameListStr);
			}
			
			String rstCode = root2.elementTextTrim("RstCode");
			stbDeviceOBJ.setRstCode(rstCode);
			logItmsResult(rstCode, reqUserId, stbDeviceOBJ);*/
		}
		catch (DocumentException e)
		{
			logger.error("[zerocfg-{}]解析xml[{}]failed",new Object[]{stbDeviceOBJ.getDeviceId(),resltFromItms});
			logger.error("[{}]-e.getMessage[{}]-e[{}]", new Object[]{stbDeviceOBJ.getDeviceId(),e.getMessage(),e});
		}
		
		/*if(StringUtil.IsEmpty(username)){
			logger.warn("[zerocfg-{}]-没有查询到未绑定用户", stbDeviceOBJ.getDeviceId());
		}else{
			logger.warn("[zerocfg-{}]-查询到的未绑定最新用户为-[{}]-customerId-[{}]", 
					stbDeviceOBJ.getDeviceId(), username, customerId);
		}*/
		return usernameListStr;
	}
	
}
