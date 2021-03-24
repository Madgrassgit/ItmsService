package com.linkage.itms.itv.main;

import java.io.StringReader;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.DeviceGatherCAO;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.itv.bio.GetItvAccountBIO;
import com.linkage.itms.itv.bio.GetItvMacBIO;
import com.linkage.itms.itv.bio.GetItvMacHBBIO;
import com.linkage.itms.itv.inter.IService;
import com.linkage.itms.obj.DevStatusOBJ;
import com.linkage.itms.Global;

/**
 * @author Jason(3412)
 * @date 2009-12-15
 */
public class ItmsService implements IService {

	private static Logger logger = LoggerFactory.getLogger(ItmsService.class);
	//正则，字符加数字
	Pattern pattern = Pattern.compile("\\w{1,}+");

	// 用户设备信息数据库操作
	UserDeviceDAO devDao;
	// 采集设备信息CAO
	DeviceGatherCAO devCao;
	// 设备状态对象
	DevStatusOBJ devStatusObj;

	public String getUserModemInfo(String inXml) {
		logger.warn("getUserModemInfo({})", inXml);
		devStatusObj = new DevStatusOBJ();
		// 获取用户账号
		String username = readUsername(inXml);
		// 判断用户账号是否合法
		if (StringUtil.IsEmpty(username) || false == pattern.matcher(username).matches()) {
			logger.warn("非法用户账号: " + username);
			devStatusObj.setConnectState(UNKOWN);
		}else{
			// 获取用户设备信息
			devDao = new UserDeviceDAO();
			Map<String, String> userDevInfoMap = devDao.getUserDevInfo(username);
			if (null == userDevInfoMap || userDevInfoMap.isEmpty()) {
				logger.warn("无该用户信息: " + username);
				devStatusObj.setConnectState(NODEV);
			}else{
				// 用户是否绑定设备
				String devId = userDevInfoMap.get("device_id");
				devStatusObj.setDeviceId(devId);
				if (StringUtil.IsEmpty(devId)) {
					logger.warn("无用户对应设备信息: " + username);
					devStatusObj.setConnectState(NODEV);
				}else{
					//厂商，型号，序列号
					String devSn = userDevInfoMap.get("device_serialnumber");
					String devModelId = userDevInfoMap.get("device_model_id");
					String devStatus = userDevInfoMap.get("online_status");
					devStatusObj.setModemSn(devSn);
					//设备型号和厂商信息
					if(false == StringUtil.IsEmpty(devModelId)){
						Map<String,String> devModelMap = devDao.getDevVendorModel(devModelId);
						if(null != devModelMap && false == devModelMap.isEmpty()){
							devStatusObj.setModemVendor(devModelMap.get("vendor_name"));
							devStatusObj.setModemType(devModelMap.get("device_model"));
						}
					}
					//状态
					if ("1".equals(devStatus)) {	//1：采集成功
						devCao = new DeviceGatherCAO();
						int iret = devCao.gatherWan(devId);
						
						if(1==iret){
							iret = devCao.gatherQoS(devId);
						}
						
						if (iret == 1) {
							devStatusObj.setConnectState(ONLINE);
							devStatusObj.setWanState(UP);
							Map<String, String> wanMap = devDao.getDevWanInfo(devId);
							if (null != wanMap && false == wanMap.isEmpty()) {
								// conn_status, last_conn_error, access_type, vpi_id,
								// vci_id, vlan_id
								String pvcStatus = wanMap.get("conn_status");
								String accessType = getAccessType(wanMap.get("access_type"));
								String lastConnErr = wanMap.get("last_conn_error");
								String pvc = "PVC:" + wanMap.get("vpi_id") + "/"+ wanMap.get("vci_id");
								String vlanId = wanMap.get("vlan_id");

								devStatusObj.setPvcState(pvcStatus);
								devStatusObj.setAccessType(accessType);
								devStatusObj.setBohaoResult(lastConnErr);
								devStatusObj.setPvc(pvc);
								devStatusObj.setVlanid(vlanId);
							}
							Map<String, String> qosMap = devDao.getDevQosInfo(devId);
							
							if(null != qosMap && false == qosMap.isEmpty()){
									String qosMode = qosMap.get("qos_mode");
									devStatusObj.setQosMode(qosMode);
							}
						} else if(2 == iret){	// 2：设备不在线
							logger.warn("设备采集失败: " + devId + " 2");
							devStatusObj.setConnectState(OFFLINE);
							devStatusObj.setWanState(DOWN);
						}else{	//3：设备正被操作  4：采集返回9000后错误  5：采集失败
							logger.warn("设备采集失败: " + devId + " " + iret);
							devStatusObj.setConnectState(OFFLINE);
							devStatusObj.setWanState(DOWN);
						}
					} else {
						logger.warn("设备不在线: " + devId);
						devStatusObj.setConnectState(OFFLINE);
						devStatusObj.setWanState(DOWN);
					}
				}
			}
		}
		String retXML = devStatusObj.obj2Xml();
		
		//记录日志
		new RecordLogDAO().recordLog("itv", 6, "getUserModemInfo",
				username, null, null, 0,inXml, retXML, System.currentTimeMillis()/1000);
		
		return retXML;
	}

	/**
	 * 读取参数调用的用户账号
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return String
	 */
	String readUsername(String strXML) {
		logger.debug("read({})", strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		String username = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			username = root.elementText("username");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return username;
	}

	/**
	 * 接入方式转换
	 * 
	 * @param 设备采集到的接入方式
	 * @author Jason(3412)
	 * @date 2009-12-15
	 * @return String 1：ADSL上行 2：LAN 3：EPON上行
	 */
	String getAccessType(String strAccessType) {
		logger.debug("getAccessType({})", strAccessType);
		if ("DSL".equals(strAccessType)) {
			return "1";
		} else if ("Ethernet".equals(strAccessType)) {
			return "2";
		} else if ("POTS".equals(strAccessType)) {
			return "3";
		} else {
			return "-1";
		}
	}


	public String getItvAccount(String inXml) {
		logger.warn("getItvAccount({})", inXml);
		return new GetItvAccountBIO().getItvAccount(inXml);
	}
	
	
	public String getItvMac(String inXml) {
		logger.warn("getItvMac({})", inXml);
		if("hb_dx".equals(Global.G_instArea)){
			// HBDX-REQ-20170330-XuPan-001(湖北ITMS+机顶盒即插即用零配置接口)
			// 20170427
			return new GetItvMacHBBIO().getItvMac(inXml);
		}else{
			// AHDX_ITMS-REQ-20170227YQW-001(通过MCA地址，查询stbind事件中对应的LOID信息)
			// 20170323
			return new GetItvMacBIO().getItvMac(inXml);
		}
	}
	

}
