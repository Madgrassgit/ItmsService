package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;


public class VlanStatusChecker extends BaseChecker{

	
	private static Logger logger = LoggerFactory.getLogger(VlanStatusChecker.class);
	private VlanInfo v41;
	
	private VlanInfo v43;
	
	public VlanInfo getV41() {
		return v41;
	}
	public void setV41(VlanInfo v41) {
		this.v41 = v41;
	}
	public VlanInfo getV43() {
		return v43;
	}
	public void setV43(VlanInfo v43) {
		this.v43 = v43;
	}
	
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public VlanStatusChecker(String inXml) {
		this.callXml = inXml;
	}
	
	
	private boolean isbase=false;
	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public VlanStatusChecker(String inXml,boolean isbase) {
		this.callXml = inXml;
		this.isbase=isbase;
	}
	
	
	
	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			loid=param.elementTextTrim("Loid");
		} catch (Exception e) {
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck()) {
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText(""+result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		if(!isbase){
			if(v41!=null&&!StringUtil.IsEmpty(v41.getVlan())){
				root.addElement("Vlan").addText(v41.getVlan());
				root.addElement("Connect").addText(v41.getConnection());
				root.addElement("BindLan").addText(v41.getBindLan());
				root.addElement("wanType").addText(v41.getWanType());
			}else{
				if(result==0){
					root.addElement("RstMsg").setText("成功,没有vlan41");
				}
			}
			if(v43!=null&&!StringUtil.IsEmpty(v43.getVlan())){
				root.addElement("Vlan").addText(v43.getVlan());
				root.addElement("Connect").addText(v43.getConnection());
				root.addElement("BindLan").addText(v43.getBindLan());
				root.addElement("wanType").addText(v43.getWanType());
			}else{
				if(result==0){
					root.addElement("RstMsg").setText("成功,没有vlan43");
				}
			}
		}
		return document.asXML();
	}
	
	
	public static class VlanInfo{
		private String vlan;
		private String connection;
		private String bindLan;
		private String wanType;
		public String getWanType() {
			return wanType;
		}
		public void setWanType(String wanType) {
			this.wanType = wanType;
		}
		public String getVlan() {
			return vlan;
		}
		public void setVlan(String vlan) {
			this.vlan = vlan;
		}
		public String getConnection() {
			return connection;
		}
		public void setConnection(String connection) {
			this.connection = connection;
		}
		public String getBindLan() {
			return bindLan;
		}
		public void setBindLan(String bindLan) {
			this.bindLan = bindLan;
		}
	}
}
