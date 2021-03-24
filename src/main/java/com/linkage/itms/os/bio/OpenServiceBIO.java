package com.linkage.itms.os.bio;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.os.dao.UserBindDAO;
import com.linkage.itms.os.obj.RecieveSheetOBJ;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author zhangshimin(工号) Tel:
 * @version 1.0
 * @since 2011-6-24 上午08:25:52
 * @category com.linkage.itms.os.main
 * @copyright 南京联创科技 网管科技部
 *
 *modify by xiangzl 2012-07-11
 */
public class OpenServiceBIO
{
	private static Logger logger = LoggerFactory.getLogger(OpenServiceBIO.class);
	public String recieveSheet(String strXML)
	{
		
		//存放操作类型对应关系，开户对应销户
		//如果是反向工单，获取工单的操作类型，做反向。 
		Map<String,String> sheetDirection = new HashMap<String, String>();
		sheetDirection.put("1", "3"); //1:开户，3：销户
		sheetDirection.put("3", "1"); //如果是销户，则系统做开户
		sheetDirection.put("8", "1"); //如果是移机，做开户修改
		sheetDirection.put("6", "6"); //该账号的反向是修改会原来的，在生成工单是判断并传入新老账号
		UserBindDAO dao = new UserBindDAO();
		logger.warn("recieveSheet({})",strXML);
		String sheetCmd = "";
		String resltFromEserver = "";
		//1.将接收的XML格式的工单信息转化为OBJ
		RecieveSheetOBJ obj = new RecieveSheetOBJ(strXML);
		//判断是否反向工单，1：正向工单  2：反向工单
		if ("1".equals(obj.getSheetDirection()))
		{
			//2.生成EServer可接收的工单指令
			sheetCmd = this.toSheetCmd(obj);
			//如果是不送终端的工单ITMS不处理，直接入原始工单表
			if (obj.getIsGiveDev().equals("2"))
			{
				dao.saveOriginSheet(strXML, obj);
				return returnXML("0|||00", obj);
			}
			//生成工单指令失败，直接给北向接口回单
			if (sheetCmd.startsWith("000"))
			{
				return returnXML(sheetCmd, obj);
			}
			//3.发送工单指令
			resltFromEserver = this.sendSheetToEServer(sheetCmd);
			//4.将回单指令转化为北向可接受的XML格式
			String strReslt = returnXML(resltFromEserver, obj);
			dao.saveOriginSheet(strXML, obj);
			return strReslt;
		}
		else //反向工单，操作类型转换
		{
			if (null != sheetDirection.get(obj.getOperType()))
			{
				obj.setOperType(sheetDirection.get(obj.getOperType()));
				//2.生成EServer可接收的工单指令
				sheetCmd = this.toSheetCmd(obj);
				logger.warn("反向工单生成：（{}）", sheetCmd);
				//生成工单指令失败，直接给北向接口回单
				if (sheetCmd.startsWith("000"))
				{
					return returnXML(sheetCmd, obj);
				}
				//3.发送工单指令
				Map<String, String> map = dao.getOriginSheet(obj.getLoid(), obj
						.getServTpe(), obj.getOperType());
				if (map != null && map.size() > 0 )
				{
					if (!StringUtil.IsEmpty(map.get("sheet_para_desc"), true))
					{
						//将筛选出来的工单发给Eserver
						resltFromEserver = this.sendSheetToEServer(map.get("sheet_para_desc"));
					}
					else 
					{
						resltFromEserver = this.sendSheetToEServer(sheetCmd);
					}
				}
				else
				{
					resltFromEserver = this.sendSheetToEServer(sheetCmd);
				}
				//4.将回单指令转化为北向可接受的XML格式
				String strReslt = returnXML(resltFromEserver, obj);
				dao.saveOriginSheet(strXML, obj);
				return strReslt;
			}
			else 
			{
				sheetCmd = "000|||1|||无此业务类型";
				return returnXML(sheetCmd, obj);
			}
		}
	}
	public String sendSheetToEServer(String sheetCmd)
	{
		String sheetResult = "";
		if(StringUtil.IsEmpty(sheetCmd)){
			logger.warn("send Sheet is null");
			return null;
		}
		//e8c宽带开户工单发送
		if(sheetCmd.startsWith("20|||1"))
		{
			String[] e8cSheet = sheetCmd.split("####");
			//1、先发e8c资料工单
			logger.warn("e8c资料工单:"+e8cSheet[0]);
			sheetResult = sendSheetCmd(e8cSheet[0]);
			logger.warn("e8c资料工单:"+e8cSheet[0]+",执行结果"+sheetResult);
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				logger.error("InterruptedException error:",e);
				// Restore interrupted state...      
				Thread.currentThread().interrupt();
			}
			if(sheetResult.endsWith("0|||00") && e8cSheet.length>1)
			{
				//2、资料成功后向EServer发送开户工单
				logger.warn("e8c开户工单:"+e8cSheet[1]);
				sheetResult = sendSheetCmd(e8cSheet[1]);
				logger.warn("e8c开户工单:"+e8cSheet[1]+",执行结果"+sheetResult);
			}
			else
			{
				return sheetResult;
			}
		}
		else
		{
			sheetResult = sendSheetCmd(sheetCmd);
		}
		return sheetResult;
	}
	/**
	 * 向工单接口发送工单指令。正常返回工单接口的回单结果，如果过程中出现问题返回null
	 * 
	 * @param 工单数据
	 * @author zhangsm
	 * @date 2011-05-21
	 * @return String 回单信息
	 */
	public String sendSheetCmd(String sheetCmd){
		logger.debug("sendSheetCmd({})", sheetCmd);
		if(StringUtil.IsEmpty(sheetCmd)){
			logger.warn("sendSheet is null");
			return null;
		}
		String server = Global.G_ITMS_Sheet_Server;
		int port = Global.G_ITMS_Sheet_Port;
		System.out.println("工单："+sheetCmd);
		String retResult = SocketUtil.sendStrMesg(server, port, sheetCmd + "\n");
//		String retResult = "0|||00";
		return retResult;
	}
	
	/**
	 * 将接受的工单对象转化为EServer能接受的工单指令
	 * @param obj
	 * @return
	 */
	public String toSheetCmd(RecieveSheetOBJ obj)
	{
		logger.debug("toSheetCmd({})", obj);
		if(obj == null)
		{
			logger.warn("工单对象RecieveSheetOBJ为null");
			return null;
		}
		String servType = obj.getServTpe();
		String operType = obj.getOperType();
		String sheetCmd = "";
		if(StringUtil.IsEmpty(servType) || StringUtil.IsEmpty(operType))
		{
			sheetCmd = "000|||1|||无此业务类型";
		}
		if(servType.equals("40"))
		{
			//获取e8b资料接口指令
			sheetCmd = this.getE8bInfoSheetCmd(obj);
		}
		else if(servType.equals("10"))
		{
			if(operType.equals("1"))
			{
				//e8b宽带开户
				sheetCmd = this.getE8bNetOpenSheetCmd(obj);
			}
			else if(operType.equals("3"))
			{
				//e8b宽带销户
				sheetCmd = this.getE8bNetStopSheetCmd(obj);
			}
			else if(operType.equals("6"))
			{
				//e8b宽带更换账号
				sheetCmd = this.getE8bNetChgSheetCmd(obj);
			}
			else
			{
				sheetCmd = "000|||2|||无此操作类型" ;
			}
		}
		else if(servType.equals("20"))
		{
			
			if(operType.equals("1"))
			{
				//e8c资料工单
				sheetCmd = this.getE8cInfoSheetCmd(obj);
			}
			else if(operType.equals("3"))
			{
				//e8c全拆机工单
				sheetCmd = this.getE8cAllStopSheetCmd(obj);
			}
			else
			{
				sheetCmd = "000|||2|||无此操作类型";
			}
		}
		else if(servType.equals("22"))
		{
			if(operType.equals("1") || operType.equals("8") || operType.equals("12"))
			{
				//e8c宽带开户或移机，移机也走开户工单
				sheetCmd = this.getE8cNetOpenSheetCmd(obj);
			}
			else if(operType.equals("3"))
			{
				//e8c宽带销户
				sheetCmd = this.getE8cNetStopSheetCmd(obj);
			}
			else if(operType.equals("6"))
			{
				//e8c宽带更换账号
				sheetCmd = this.getE8cNetChgSheetCmd(obj);
			}
			else
			{
				sheetCmd = "000|||2|||无此操作类型" ;
			}
		}
		else if(servType.equals("15"))
		{
			if(operType.equals("1") || operType.equals("8"))
			{
				//e8c H248 VOIP开户
				sheetCmd = this.getE8cH248VoipOpenSheetCmd(obj);
			}
			else if(operType.equals("3"))
			{
				//e8c H248 VOIP销户
				sheetCmd = this.getE8cH248VoipStopSheetCmd(obj);
			}
			else if(operType.equals("6"))
			{
				//e8c H248 VOIP更换账号
				sheetCmd = getE8cVoipChgSheetCmd(obj);
			}
			else
			{
				sheetCmd = "000|||2|||无此操作类型" ;
			}
		}
		else if(servType.equals("21"))
		{
			if(operType.equals("1") || operType.equals("8"))
			{
				//iptv开户
				sheetCmd = this.getIPTVOpenSheetCmd(obj);
			}
			else if(operType.equals("3"))
			{
				//iptv销户
				sheetCmd = this.getIPTVStopSheetCmd(obj);
			}
//			else if(operType.equals("6"))
//			{
//				//e8c H248 VOIP更换账号
//				sheetCmd = getE8cVoipChgSheetCmd(obj);
//			}
			else
			{
				sheetCmd = "000|||2|||无此操作类型" ;
			}
		}
		else
		{
			sheetCmd = "000|||1|||无此业务类型" ;
		}
		logger.warn("工单指令："  + sheetCmd);
		return sheetCmd;
	}
	/**
	 * 获取e8c宽带销户工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cNetStopSheetCmd(RecieveSheetOBJ obj)
	{
		int servNum = new UserBindDAO().hasElseService(obj.getLoid(), 10, obj.getDevType());
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		
		sheetCmd.append(obj.getCityId()).append("LINKAGE");
		if(servNum == 0)
		{
			return this.getE8cAllStopSheetCmd(obj);
		}
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c H248 Voip销户工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cH248VoipStopSheetCmd(RecieveSheetOBJ obj)
	{
		int servNum = new UserBindDAO().hasElseService(obj.getLoid(), 14, obj.getDevType());
		int voipCount = new UserBindDAO().hasElseService(obj.getLoid(), 14, obj.getVoipTelepone(), obj.getDevType());
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getVoipTelepone()).append("LINKAGE");
		if(servNum == 0 && voipCount == 0)
		{
			return this.getE8cAllStopSheetCmd(obj);
		}
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c H248 Voip开户工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cH248VoipOpenSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		
		//拼装e8c宽带开户工单工单
		sheetCmd.append("20").append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getOfficeId()).append("|||");
		sheetCmd.append(obj.getCellId()).append("|||");
		sheetCmd.append(obj.getAccessType()).append("|||");
		sheetCmd.append(obj.getLinkPeople()).append("|||");
		sheetCmd.append(obj.getTelPhone()).append("|||");
		sheetCmd.append(obj.getEmail()).append("|||");
		sheetCmd.append(obj.getMobile()).append("|||");
		sheetCmd.append(obj.getAddress()).append("|||");
		sheetCmd.append(obj.getCardNo()).append("|||");
		sheetCmd.append(obj.getCustomerId()).append("|||");
		if("2".equals(obj.getDevType()) && "".equals(obj.getCustomerAccount())){
			sheetCmd.append(obj.getCustomerId()).append("|||");
		}else{
			sheetCmd.append(obj.getCustomerAccount()).append("|||");
		}
		sheetCmd.append(obj.getCustomerPwd()).append("LINKAGE");

		//资料工单和开户工单用'####'分开
		sheetCmd.append("####");
		//拼装e8c H248 Voip开户工单工单
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getVoipTelepone()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getRegId()).append("|||");
		sheetCmd.append(obj.getRegIdType()).append("|||");
		sheetCmd.append(obj.getMgcIp()).append("|||");
		sheetCmd.append(obj.getMgcPort()).append("|||");
		sheetCmd.append(obj.getStandMgcIp()).append("|||");
		sheetCmd.append(obj.getStandMgcPort()).append("|||");
		// 黑龙江需求：1表示语音1口即A0，2表示语音2口即A1。正常情况只有1和2两个值，当异常时默认为1口即A0.
		if ("1".equals(obj.getVoipPort()))
		{
			obj.setVoipPort("A1");
		}
		else
		{
			obj.setVoipPort("A0");
		}
		sheetCmd.append(obj.getVoipPort());
		sheetCmd.append("|||45|||4|||||||||||||||");
		sheetCmd.append(2).append("LINKAGE");
		
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c VOIP更换账号指令
	 * @param obj
	 * @return
	 */
	private String getE8cVoipChgSheetCmd(RecieveSheetOBJ obj)
	{
		//根据老号码获取LOID和语音端口
		Map<String,String> voipParamMap = new UserBindDAO().queryLoidByVoip(obj.getOldNetUsername(), obj.getCityId(), obj.getDevType());
		if(voipParamMap == null || voipParamMap.isEmpty())
		{
			logger.warn("ddddddddddddddddd");
		}
		
		if(voipParamMap != null && !voipParamMap.isEmpty())
		{
			String loid = voipParamMap.get("username");
			obj.setLoid(loid);
			obj.setVoipPort(voipParamMap.get("voip_port"));
			
		}
		else
		{
//			return "000|||6|||无此用户";
			return "0|||00";
		}
		
		obj.setRegId(obj.getLoid() + ".voip");
		
		StringBuffer sheetCmd = new StringBuffer();
		//拼装开户工单
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		
		sheetCmd.append(obj.getVoipTelepone()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getRegId()).append("|||");
		sheetCmd.append(obj.getRegIdType()).append("|||");
		sheetCmd.append(obj.getMgcIp()).append("|||");
		sheetCmd.append(obj.getMgcPort()).append("|||");
		sheetCmd.append(obj.getStandMgcIp()).append("|||");
		sheetCmd.append(obj.getStandMgcPort()).append("|||");
		sheetCmd.append(obj.getVoipPort()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c宽带更换账号指令
	 * @param obj
	 * @return
	 */
	private String getE8cNetChgSheetCmd(RecieveSheetOBJ obj)
	{
		//如果LOID为空，就根据老宽带账号查询LOID
		if(StringUtil.IsEmpty(obj.getLoid(), true))
		{
			String loid = new UserBindDAO().queryLoidBynet(obj.getOldNetUsername(), obj.getCityId(), obj.getDevType());
			if(StringUtil.IsEmpty(loid, true))
			{
//				return "000|||6|||无此用户";
//				TODO  黑龙江老账号不存在，先返回成功，后续等老用户同步过来在处理。xiangzl
				return "0|||00";
			}
			else
			{
				obj.setLoid(loid);
			}
			
		}
		StringBuffer sheetCmd = new StringBuffer();
		//拼装e8c宽带开户工单工单
		sheetCmd.append("20").append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getOfficeId()).append("|||");
		sheetCmd.append(obj.getCellId()).append("|||");
		sheetCmd.append(obj.getAccessType()).append("|||");
		sheetCmd.append(obj.getLinkPeople()).append("|||");
		sheetCmd.append(obj.getTelPhone()).append("|||");
		sheetCmd.append(obj.getEmail()).append("|||");
		sheetCmd.append(obj.getMobile()).append("|||");
		sheetCmd.append(obj.getAddress()).append("|||");
		sheetCmd.append(obj.getCardNo()).append("|||");
		sheetCmd.append(obj.getCustomerId()).append("|||");
		sheetCmd.append(obj.getCustomerAccount()).append("|||");
		sheetCmd.append(obj.getCustomerPwd()).append("LINKAGE");

		sheetCmd.append("####");
		
		
		//拼装改账号工单
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append("6").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getOldNetUsername()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c宽带开户工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cNetOpenSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		if(!"12".equals(obj.getOperType())){
			//拼装e8c宽带开户工单工单
			sheetCmd.append("20").append("|||");
			sheetCmd.append("1").append("|||");
			sheetCmd.append(obj.getDealDate()).append("|||");
			sheetCmd.append(obj.getDevType()).append("|||");
			sheetCmd.append(obj.getLoid()).append("|||");
			sheetCmd.append(obj.getCityId()).append("|||");
			sheetCmd.append(obj.getOfficeId()).append("|||");
			sheetCmd.append(obj.getCellId()).append("|||");
			sheetCmd.append(obj.getAccessType()).append("|||");
			sheetCmd.append(obj.getLinkPeople()).append("|||");
			sheetCmd.append(obj.getTelPhone()).append("|||");
			sheetCmd.append(obj.getEmail()).append("|||");
			sheetCmd.append(obj.getMobile()).append("|||");
			sheetCmd.append(obj.getAddress()).append("|||");
			sheetCmd.append(obj.getCardNo()).append("|||");
			sheetCmd.append(obj.getCustomerId()).append("|||");
			if("2".equals(obj.getDevType()) && "".equals(obj.getCustomerAccount())){
				sheetCmd.append(obj.getCustomerId()).append("|||");
			}else{
				sheetCmd.append(obj.getCustomerAccount()).append("|||");
			}
			sheetCmd.append(obj.getCustomerPwd()).append("LINKAGE");
			//sheetCmd.append(obj.getBandRate()).append("|||");
			//sheetCmd.append(obj.getCustGrade()).append("LINKAGE");

			//资料工单和开户工单用'####'分开
			sheetCmd.append("####");
		}
		//拼装开户工单
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("|||");
		sheetCmd.append(obj.getNetPassword()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getVlanId()).append("|||");
		convertWanType(sheetCmd, obj);
		sheetCmd.append(obj.getIpAddress()).append("|||");
		sheetCmd.append(obj.getIpMask()).append("|||");
		sheetCmd.append(obj.getGateWay()).append("|||");
//		sheetCmd.append(obj.getIpType()).append("|||");
//		
//		sheetCmd.append(obj.getDns()).append("LINKAGE");
		sheetCmd.append(obj.getDns()).append("|||");
		sheetCmd.append(obj.getIpType()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8c全拆机工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cAllStopSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append("20").append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("LINKAGE");

		return sheetCmd.toString();
	}
	/**
	 * 获取e8c资料工单指令
	 * @param obj
	 * @return
	 */
	private String getE8cInfoSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append("20").append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getOfficeId()).append("|||");
		sheetCmd.append(obj.getCellId()).append("|||");
		sheetCmd.append(obj.getAccessType()).append("|||");
		sheetCmd.append(obj.getLinkPeople()).append("|||");
		sheetCmd.append(obj.getTelPhone()).append("|||");
		sheetCmd.append(obj.getEmail()).append("|||");
		sheetCmd.append(obj.getMobile()).append("|||");
		sheetCmd.append(obj.getAddress()).append("|||");
		sheetCmd.append(obj.getCardNo()).append("|||");
		sheetCmd.append(obj.getCustomerId()).append("|||");
		if("2".equals(obj.getDevType()) && "".equals(obj.getCustomerAccount())){
			sheetCmd.append(obj.getCustomerId()).append("|||");
		}else{
			sheetCmd.append(obj.getCustomerAccount()).append("|||");
		}
		sheetCmd.append(obj.getCustomerPwd()).append("LINKAGE");

		return sheetCmd.toString();
	}
	/**
	 * 获取e8b宽带暂停指令
	 * @param obj
	 * @return
	 */
	private String getE8bNetPauseSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		
		sheetCmd.append(obj.getOui()).append("|||");
		sheetCmd.append(obj.getDevSn()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("|||");
		sheetCmd.append(obj.getCityId()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8b宽带复机指令
	 * @param obj
	 * @return
	 */
	private String getE8bNetReStartSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		
		sheetCmd.append(obj.getOui()).append("|||");
		sheetCmd.append(obj.getDevSn()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("|||");
		sheetCmd.append(obj.getCityId()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8b宽带更换账号指令
	 * @param obj
	 * @return
	 */
	private String getE8bNetChgSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append("10").append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		
		sheetCmd.append(obj.getOui()).append("|||");
		sheetCmd.append(obj.getDevSn()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("|||");
		sheetCmd.append(obj.getNetPassword()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");

		sheetCmd.append(obj.getOldNetUsername()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8b宽带销户指令
	 * @param obj
	 * @return
	 */
	private String getE8bNetStopSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		
		sheetCmd.append(obj.getOui()).append("|||");
		sheetCmd.append(obj.getDevSn()).append("|||");
		sheetCmd.append(obj.getNetUsername()).append("|||");
		
		sheetCmd.append(obj.getCityId()).append("LINKAGE");

		return sheetCmd.toString();
	}
	/**
	 * 获取e8b宽带开户指令
	 * @param obj
	 * @return
	 */
	private String getE8bNetOpenSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		
		sheetCmd.append(obj.getOui()).append("|||");
		sheetCmd.append(obj.getDevSn()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		
		sheetCmd.append(obj.getNetUsername()).append("|||");
		sheetCmd.append(obj.getNetPassword()).append("|||");
		sheetCmd.append(obj.getPhoneNumber()).append("|||");
		
		sheetCmd.append(obj.getMaxupRate()).append("|||");
		sheetCmd.append(obj.getMaxdownRate()).append("|||");
		sheetCmd.append(obj.getMaxUserNum()).append("|||");
		
		sheetCmd.append(obj.getVlanId()).append("|||");
		sheetCmd.append(obj.getVpi()).append("|||");
		sheetCmd.append(obj.getVci()).append("|||");
		
		sheetCmd.append(obj.getDslamDevNo()).append("|||");
		sheetCmd.append(obj.getDslamIp()).append("|||");
		sheetCmd.append(obj.getDslamDevFrameNo()).append("|||");
		
		sheetCmd.append(obj.getDslamDevBoxNo()).append("|||");
		sheetCmd.append(obj.getDslamDevSlotNo()).append("|||");
		sheetCmd.append(obj.getDslamDevPortNo()).append("|||");
		
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getOfficeId()).append("|||");
		sheetCmd.append(obj.getCellId()).append("|||");
		sheetCmd.append(obj.getLinkPeople()).append("|||");
		sheetCmd.append(obj.getTelPhone()).append("|||");
		sheetCmd.append(obj.getEmail()).append("|||");
		sheetCmd.append(obj.getMobile()).append("|||");
		sheetCmd.append(obj.getAddress()).append("|||");
		sheetCmd.append(obj.getWanType()).append("|||");
		sheetCmd.append(obj.getAccessType()).append("|||");
		sheetCmd.append(obj.getPackageType()).append("|||");
		sheetCmd.append(obj.getCardNo()).append("|||");

		sheetCmd.append(obj.getOrderId()).append("LINKAGE");
		return sheetCmd.toString();
	}
	/**
	 * 获取e8b资料接口指令
	 * @param obj
	 * @return
	 */
	private String getE8bInfoSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getOfficeId()).append("|||");
		sheetCmd.append(obj.getCellId()).append("|||");
		sheetCmd.append(obj.getAccessType()).append("|||");
		sheetCmd.append(obj.getLinkPeople()).append("|||");
		sheetCmd.append(obj.getTelPhone()).append("|||");
		sheetCmd.append(obj.getEmail()).append("|||");
		sheetCmd.append(obj.getMobile()).append("|||");
		sheetCmd.append(obj.getAddress()).append("|||");
		sheetCmd.append(obj.getCardNo()).append("LINKAGE");
//		sheetCmd.append(obj.getBandRate()).append("|||");
//		sheetCmd.append(obj.getCustGrade()).append("LINKAGE");

		return sheetCmd.toString();
	}
	
	/**
	 * 获取iptv开户工单指令
	 * @param obj
	 * @return
	 */
	private String getIPTVOpenSheetCmd(RecieveSheetOBJ obj)
	{
		StringBuffer sheetCmd = new StringBuffer();
		//拼装e8c资料开户工单
		sheetCmd.append(getE8cInfoSheetCmd(obj));
		
		//资料工单和开户工单用'####'分开
		sheetCmd.append("####");
		
	
		//拼装e8c iptv 开户工单工单
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append("1").append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getIptvAccount()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getIptvNum()).append("|||");
		sheetCmd.append(obj.getIptvPort()).append("|||");
		sheetCmd.append(obj.getVlanId()).append("LINKAGE");
		
		return sheetCmd.toString();
	}
	
	/**
	 * iptv销户工单
	 * @param obj
	 * @return
	 */
	private String getIPTVStopSheetCmd(RecieveSheetOBJ obj)
	{
		//iptv的业务类型为11
		int serviceTypeIPTV =11;
		int servNum = new UserBindDAO().hasElseService(obj.getLoid(), serviceTypeIPTV, obj.getDevType());
		StringBuffer sheetCmd = new StringBuffer();
		sheetCmd.append(obj.getServTpe()).append("|||");
		sheetCmd.append(obj.getOperType()).append("|||");
		sheetCmd.append(obj.getDealDate()).append("|||");
		sheetCmd.append(obj.getDevType()).append("|||");
		sheetCmd.append(obj.getLoid()).append("|||");
		sheetCmd.append(obj.getCityId()).append("|||");
		sheetCmd.append(obj.getIptvAccount()).append("LINKAGE");
		if(servNum == 0)
		{
			return this.getE8cAllStopSheetCmd(obj);
		}
		return sheetCmd.toString();
	}
	
	
	public String returnXML(String strXML,RecieveSheetOBJ obj)
	{
		logger.warn("工单服务器回单："+strXML);
		String [] arrStrings = strXML.split("\\|\\|\\|");
		String resultCode = arrStrings[1];
		String resultMsg = "成功";
		if(!resultCode.equals("00"))
		{
			resultMsg = arrStrings[2];
		}
		logger.warn("resultCode:"+resultCode + ",resultMsg:" +resultMsg);
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		root.addElement("CmdID").addText(obj.getCmdId());
		root.addElement("RstCode").addText(resultCode);
		root.addElement("RstMsg").addText(resultMsg);
		String strReslt = document.asXML();
		logger.warn("业务开通接口回参："+strReslt);
		return strReslt;
	}
	
	private void convertWanType(StringBuffer sheetCmd, RecieveSheetOBJ obj) {

		// 黑龙江电信企业网关做特殊处理
		if ("hlj_dx".equals(Global.G_instArea) && "2".equals(obj.getDevType())) {
			sheetCmd.append(obj.getWanType()).append("|||");
			//sheetCmd.append(StringUtil.IsEmpty(obj.getWanType()) ? "1" : obj.getWanType()).append("|||");
			
		} else {
			if ("7".equals(obj.getNetType())) {
				sheetCmd.append(1).append("|||");
			} else {
				sheetCmd.append(2).append("|||");
			}
		}
	}
}
