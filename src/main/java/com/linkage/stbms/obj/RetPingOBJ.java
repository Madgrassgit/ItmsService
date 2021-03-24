package com.linkage.stbms.obj;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.stbms.itv.main.Global;

/**
 * @author Jason(3412)
 * @date 2009-12-22
 */
public class RetPingOBJ {

	private static Logger logger = LoggerFactory.getLogger(RetPingOBJ.class);
	// 设备ID
	private String deviceId;
	//用户信息
	private String searchInfo;
	// ping的操作对象
	private PingOBJ[] pingObj;
	// 结果标识，未找到用户，无对应设备，设备不在线等
	private int flag;

	/**
	 * 接口对象返回XML字符串
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-12-17
	 * @return String
	 */
	public String obj2xml() {
		logger.debug("RetPingOBJ.obj2Xml()");

		Document document = DocumentHelper.createDocument();
		// root
		Element root = document.addElement("root");
		// ping操作的标识位
		root.addElement("result_flag").setText(StringUtil.getStringValue(flag));
		if (1 == flag && null != pingObj) {
			int length = pingObj.length;
			for (int i = 0; i < length; i++) {
				Element devicePing = root.addElement("device");

				devicePing.addElement("ping_ip").addText(
						StringUtil.getStringValue(pingObj[i].getPingAddr()));
				devicePing.addElement("avg_deley").addText(
						StringUtil.getStringValue(pingObj[i].getDelayAvg()));
				devicePing.addElement("max_deley").addText(
						StringUtil.getStringValue(pingObj[i].getDelayMax()));
				devicePing.addElement("min_deley").addText(
						StringUtil.getStringValue(pingObj[i].getDelayMin()));
				devicePing.addElement("success_packs").addText(
						StringUtil.getStringValue(pingObj[i].getSuccNum()));
				devicePing.addElement("fail_packs").addText(
						StringUtil.getStringValue(pingObj[i].getFailNum()));
				if("jx_dx".equals(Global.G_instArea)){
					devicePing.addElement("SearchInfo").addText(StringUtil.getStringValue(searchInfo));

					int avg_deley = pingObj[i].getDelayAvg();
					String pingRet = "0";
					if(avg_deley > 0){
						pingRet = "1";
					}
					devicePing.addElement("Result").addText(StringUtil.getStringValue(pingRet));
				}
			}
		}
		return document.asXML();
	}

	/** getter, setter */
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public PingOBJ[] getPingObj() {
		return pingObj;
	}

	public void setPingObj(PingOBJ[] pingObj) {
		this.pingObj = pingObj;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getSearchInfo()
	{
		return searchInfo;
	}

	public void setSearchInfo(String searchInfo)
	{
		this.searchInfo = searchInfo;
	}

}
