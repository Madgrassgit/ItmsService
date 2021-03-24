package com.linkage.itms.mq.servinfo.thread;

import java.io.StringReader;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.linkage.commom.util.HttpClientCallSoapUtil;
import com.linkage.commom.util.HttpUtil;
import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.dao.DeviceInfoDAO;

/**
 * @author zhaixx
 *
 */
public class DevServinfoDealThread implements Runnable {

	/** log */
	private static final Logger log = LoggerFactory.getLogger(DevServinfoDealThread.class);
	private static final String QOS_SERVTYPEID = "<servTypeId>46</servTypeId>";
	/**
	 * // 针对mq过来的消息
	 */
	private String message = null;
	private String topic = null;
	private String deviceId = "";
	private String loid = "";
	private static String resultDesc = "";
	private static String resultCode = "";
	private DeviceInfoDAO dao = new DeviceInfoDAO();
	public void setMessage(String message) {
		log.debug("setMessage({})", message);
		this.message = message;
	}
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public void run() {
		if (!mqMessageHandle()) {
			log.error("[DevServinfoDealThread]消息解析失败或者不包含servtypeid = 46，[{}]", message);
			return;
		}
		//根据device_id查询loid,返回结果
		Map<String, String> queryLoidInfoByDeviceId = dao.queryLoidInfoByDeviceId(deviceId);
		if(null != queryLoidInfoByDeviceId && !queryLoidInfoByDeviceId.isEmpty()){
			//调用服开接口返回结果
			if (Global.AHLT.equals(Global.G_instArea))
			{
				String callRemoteService = callRemoteService(queryLoidInfoByDeviceId.get("username"),Global.BACK_FUKAI_URL);
				log.warn("callRemoteService：{}",callRemoteService);
			}else{
				
				JSONObject paramMap = new JSONObject();
				paramMap.put("loid",queryLoidInfoByDeviceId.get("username"));
				paramMap.put("result_code",resultCode);
				paramMap.put("err_type",30);
				String result="";
				try {
					result = HttpUtil.doPost(Global.BACK_FUKAI_URL, paramMap,null);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				JSONObject jsonObject = JSONObject.parseObject(result);
				String rstCode = jsonObject.getString("result_code");
				log.warn("保障配置结果回调返回：{}",result);
				recordLog("cfgresult", queryLoidInfoByDeviceId.get("username"), queryLoidInfoByDeviceId.get("device_serialnumber"), "", 
						StringUtil.getIntegerValue(rstCode), paramMap.toString(), result);
				
			}
		}
	}
	
	public void recordLog(String modthName,
			String userName, String devSn, String cityId, int respCode,
			String reqInfo, String respInfo)
	{
		String strSQL = "insert into log_gtms_service ("
				+ " serv_id,itfs_id,client_type_id,cmd_name,username,"
				+ " device_sn,city_id,resp_code,req_info,resp_info,"
				+ " itfs_time) values " + " (?,?,?,?,?,   ?,?,?,?,?,   ?)";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.setLong(1, getRandomId());
		psql.setString(2, System.currentTimeMillis()/1000+"" );
		psql.setInt(3, 0);
		psql.setString(4, modthName);
		psql.setString(5, userName);
		psql.setString(6, devSn);
		psql.setString(7, cityId);
		psql.setInt(8, respCode);
		psql.setString(9, reqInfo);
		psql.setString(10, respInfo);
		psql.setLong(11, System.currentTimeMillis() / 1000);
		DBOperation.executeUpdate(psql.getSQL());
	}
	
	
	public static long getRandomId()
	{
		return Math.round(Math.random() * 1000000000);
	}
	
	/**
	 * 发送webService
	 */
	public static String callRemoteService(String loid, String url)
	{
		StringBuffer inParam = new StringBuffer();
		inParam.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.interfaces.iom.dbdp.usi/\">\n");
		inParam.append("    <soapenv:Header/>\n");
		inParam.append(" <soapenv:Body>\n");
		inParam.append("   <ser:TMSOrderResult>\n");
		inParam.append("        <!--Optional:-->\n");
		inParam.append("         <TMSOrderResult>\n");
		inParam.append("          <![CDATA[ <?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		inParam.append("         <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:os=\"os.xsd.v1\">\n");
		inParam.append("           <soapenv:Body>\n");
		inParam.append("             <os:Notify-TMSOrderResult>\n");
		inParam.append("     			 <os:MODIFIER>TMSOrderResult</os:MODIFIER>\n");
		inParam.append("    			 <os:Session>aasfsa</os:Session>\n");
		inParam.append("    		 	<os:objectParas>\n");
		inParam.append("			 		<os:OrderID>").append(loid).append("</os:OrderID>				\n");
		inParam.append("					 <os:ErrCode>").append(resultCode).append("</os:ErrCode>				\n");
		inParam.append("		 			<os:ErrDesc>").append(resultDesc).append("</os:ErrDesc>				\n");
		inParam.append(" 			 	</os:objectParas>\n");
		inParam.append("			</os:Notify-TMSOrderResult>\n");
		inParam.append(" 		  </soapenv:Body>\n");
		inParam.append("		 </soapenv:Envelope> ]]>\n");
		inParam.append(" 		</TMSOrderResult>\n");
		inParam.append(" 	</ser:TMSOrderResult>\n");
		inParam.append(" </soapenv:Body>\n");
		inParam.append("</soapenv:Envelope>\n");
		log.warn("url:{},inParam:{}",url,inParam.toString());
		return HttpClientCallSoapUtil.doPostSoap(url, inParam.toString(), "");
	}
	
	public boolean mqMessageHandle() {
		log.warn("TOPIC:{},message:{}",topic,message);
		//<ServInfo><devId>11103037</devId><servTypeId>46</servTypeId><servName>
		//</servName><servStatus>1</servStatus><openStatus>1</openStatus><gwType>1</gwType></ServInfo>]
		SAXReader reader = new SAXReader();
		Document document = null;
		if (StringUtil.IsEmpty(message)) {
			return false;
		}
		if(message.contains(QOS_SERVTYPEID)){
			try
			{
				document = reader.read(new StringReader(message));
				Element root = document.getRootElement();
				deviceId = root.elementTextTrim("devId");
				//loid = root.elementTextTrim("Loid");
				resultCode = root.elementTextTrim("OpenStatus");
				if(Global.DO == StringUtil.getIntegerValue(resultCode)){
					resultDesc = "成功";
				}else{
					resultDesc = "失败";
				}
			}catch(Exception e){
				log.error("解析出错:{}",ExceptionUtils.getStackTrace(e));
				return false;
			}
			return true;
		}else{
			return false;
		}
	}
}
