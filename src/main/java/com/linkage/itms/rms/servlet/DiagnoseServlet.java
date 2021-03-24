package com.linkage.itms.rms.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.rms.obj.DiagnoseResult;
import com.linkage.itms.rms.obj.DiagnoseService;
import com.linkage.itms.rms.obj.JsonEntity;
import com.linkage.itms.rms.obj.WanConnSessObj;
import com.linkage.itms.rms.obj.domain.DeviceWireInfoObj;
import com.linkage.itms.rms.obj.domain.PONInfoOBJ;
import com.linkage.itms.rms.util.RmsUtil;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2015年3月24日
 * @category com.linkage.itms.rms.servlet
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class DiagnoseServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(DiagnoseServlet.class);
	private static HashMap<String,String> bindPortMap=new HashMap<String,String>();
	//构造
	public DiagnoseServlet(){
		bindPortMap.put("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1", "LAN1");
		bindPortMap.put("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.2", "LAN2");
		bindPortMap.put("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3", "LAN3");
		bindPortMap.put("InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.4", "LAN4");
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setCharacterEncoding("UTF-8");
		String userTypeString = req.getParameter("userInfoType");
		String username = req.getParameter("userInfo");
		Integer userType = null;
		if(StringUtil.IsEmpty(username) || StringUtil.IsEmpty(userTypeString))
		{
			logger.error("参数不对, userInfoType[{}],username[{}]",userTypeString,username);
			// 书写json对象实体
			JsonEntity entity = new JsonEntity();
			entity.setCode("1");
			entity.setDetail("参数不对,空值");
			// 写出json
			RmsUtil.writeJson(entity, resp);
			return;
		}
		try
		{
			userType = Integer.parseInt(userTypeString);
		}
		catch (Exception e)
		{
			logger.error("参数不对, 发生异常{}", e);
			// 书写json对象实体
			JsonEntity entity = new JsonEntity();
			entity.setCode("1");
			entity.setDetail("参数非法, userType必须是数字");
			// 写出json
			RmsUtil.writeJson(entity, resp);
			return;
		}
		DiagnoseService service = new DiagnoseService();
		List<HashMap<String, String>> userInfo = service.queryUserInfo(userType, username);
		if(userInfo==null||userInfo.size()==0){
			JsonEntity entity = new JsonEntity();
			entity.setCode("2");
			entity.setDetail("结果为空");
			// 写出json
			RmsUtil.writeJson(entity, resp);
			return;
		}
		else
		{
			Map<String,DiagnoseResult> map=new HashMap<String,DiagnoseResult>();
			String deviceId=null;
			for(HashMap<String, String> columnMapTemp:userInfo){
				String userId=StringUtil.getStringValue(columnMapTemp,"user_id");
				deviceId=StringUtil.getStringValue(columnMapTemp,"device_id");
				if(!map.containsKey(userId)){
					DiagnoseResult diagnoseResult=new DiagnoseResult();
					diagnoseResult.setLoid(StringUtil.getStringValue(columnMapTemp,"loid",""));
					diagnoseResult.setNetAccount(StringUtil.getStringValue(columnMapTemp,"username",""));
					//宽带
					if("10".equals(StringUtil.getStringValue(columnMapTemp,"serv_type_id"))){
						diagnoseResult.setNetAccount(StringUtil.getStringValue(columnMapTemp,"username",""));
					}
					map.put(userId, diagnoseResult);
				}
				else{
					//宽带
					if("10".equals(StringUtil.getStringValue(columnMapTemp,"serv_type_id"))){
						map.get(userId).setNetAccount(StringUtil.getStringValue(columnMapTemp,"username",""));
					}
				}
			}
			List<DiagnoseResult> resultList=new ArrayList(map.values());
			if(map.keySet().size()>1){
				JsonEntity entity = new JsonEntity();
				entity.setCode("3");
				entity.setDetail("查询到多个设备");
				entity.setValue(resultList);
				// 写出json
				RmsUtil.writeJson(entity, resp);
				return;
			}
			else if(map.keySet().size()==1){
			   //确定为一个设备后
				DiagnoseResult data=resultList.get(0);
				Map<String, String> deviceInfo = service.queryDeviceInfoByDeviceId(deviceId);
				if(deviceInfo!=null){
					data.setSofwareversion(StringUtil.getStringValue(deviceInfo,"softwareversion",""));
					data.setVendor_name(StringUtil.getStringValue(deviceInfo,"vendor_name",""));
					data.setHardwareversion(StringUtil.getStringValue(deviceInfo,"hardwareversion",""));
				}
				Map<String, Object> dataGatherByDeviceId = service.gatherByDeviceId(deviceId);
				if(null == dataGatherByDeviceId)
				{
					JsonEntity entity = new JsonEntity();
					entity.setCode("1005");
					entity.setDetail("采集失败");
					entity.setValue(data);
					// 写出json
					RmsUtil.writeJson(entity, resp);
					return;
				}
				//宽带采集信息
				List<WanConnSessObj> netList=(List<WanConnSessObj>) dataGatherByDeviceId.get("wideNetInfoList");
				logger.warn("netList is "+netList);
				//连接状态
				String netStatus="";
				//绑定端口
				String bindPort="";
				//pvc_vlan配置
				String netPvcVlan="";
				if(null!=netList&&netList.size()>0){
					netStatus=StringUtil.getStringValue(netList.get(0).getStatus());
				    for(int i=0;i<netList.size();i++){
				    	bindPort=bindPort+StringUtil.getStringValue(bindPortMap,netList.get(i).getBindPort(),"");
				    	if(i<netList.size()-1&&null!=StringUtil.getStringValue(bindPortMap,netList.get(i).getBindPort())){
				    		bindPort=bindPort+",";
				    	}
				    }
				    netPvcVlan=StringUtil.getStringValue(netList.get(0).getPvc())+StringUtil.getStringValue(netList.get(0).getVlanid());
				}
		    //语音采集信息
				List<Map> voipList=(List<Map>) dataGatherByDeviceId.get("voipInfoList");
				logger.warn("voipList is "+voipList);
				//连接状态
				String voipStatus="";
				//注册状态
				String registerStatus="";
				//语音端口
				String line="";
				//pvc_vlan配置
				String voipPvcVlan="";
				if(null!=voipList&&voipList.size()>0){
					voipStatus=StringUtil.getStringValue(voipList.get(0),"status","");
					 registerStatus=StringUtil.getStringValue( voipList.get(0),"regist_status","");
					 line=StringUtil.getStringValue( voipList.get(0),"line","");
					 voipPvcVlan=StringUtil.getStringValue(voipList.get(0),"pvc","")+StringUtil.getStringValue( voipList.get(0),"vlanid","");
				}
				 
			//线路信息
				DeviceWireInfoObj[] wireInfoObjArr=(DeviceWireInfoObj[]) dataGatherByDeviceId.get("wireInfoObjArr");
				PONInfoOBJ[] ponInfoOBJArr=(PONInfoOBJ[]) dataGatherByDeviceId.get("ponInfoOBJArr");
				logger.warn("wireInfoObjArr is "+wireInfoObjArr);
				logger.warn("ponInfoOBJArr is "+ponInfoOBJArr);
				//线路状态,DSL物理链路的状态
				String wireStatus="";
				//上行线路衰减表示为0.1d
				String upstreamAttenuation="";
				//下行线路衰减表示为0.1d
				String downstreamAttenuation="";
				//上行速率Kbps
				String upstreamMaxRate="";
				//下行速率Kbps
				String downstreamMaxRate="";
				if(null!=ponInfoOBJArr){
					
				}
				if(null!=wireInfoObjArr&&wireInfoObjArr.length>0){
				 wireStatus=StringUtil.getStringValue(wireInfoObjArr[0].getWireStatus());
				 upstreamAttenuation=StringUtil.getStringValue(wireInfoObjArr[0].getUpstreamAttenuation());
				 downstreamAttenuation=StringUtil.getStringValue(wireInfoObjArr[0].getDownstreamAttenuation());
				 upstreamMaxRate=StringUtil.getStringValue(wireInfoObjArr[0].getUpstreamMaxRate());
				 downstreamMaxRate=StringUtil.getStringValue(wireInfoObjArr[0].getDownstreamAttenuation());
			    }
			//lan信息
				List lanInfoList=(List) dataGatherByDeviceId.get("lanInfoList");
				logger.warn("lanInfoList is "+lanInfoList);
				//连接状态
				String lanStatus="";
				//连接名称
				String connName="";
				//连接速率
				String connRate="";
				if(null!=lanInfoList&&lanInfoList.size()>0){
					HashMap<String,String> lanMap=(HashMap<String, String>) lanInfoList.get(0);
					 lanStatus=StringUtil.getStringValue(lanMap, "status","");
					 if(null!=StringUtil.getStringValue(lanMap, "lan_eth_id")){
						 connName="LAN"+StringUtil.getStringValue(lanMap, "lan_eth_id");
					 }
					 connRate=StringUtil.getStringValue(lanMap, "max_bit_rate","");;
				}
		    //wlan信息
				List wlanInfoList=(List) dataGatherByDeviceId.get("wlanInfoList");
				logger.warn("wlanInfoList is "+wlanInfoList);
				//ssid名称
				String ssid="";
				//连接状态
				String wlanStatus="";
				//连接设备数
				String associatedNum="";
				//是否启用
				String enable="";
				if(null!=wlanInfoList&&wlanInfoList.size()>0){
				  HashMap<String,String> wlanMap=(HashMap<String, String>) wlanInfoList.get(0);
				  ssid=StringUtil.getStringValue(wlanMap, "ssid","");
				  wlanStatus=StringUtil.getStringValue(wlanMap, "status","");
				  associatedNum=StringUtil.getStringValue(wlanMap, "associated_num","");
				  enable="1".equals(StringUtil.getStringValue(wlanMap, "enable"))?"是":"否";
				}
				data.setNetStatus(netStatus);
				data.setBind_port(bindPort);
				data.setNetPvcVlan(netPvcVlan);
				data.setVoipStatus(voipStatus);
				data.setLine(line);
				data.setVoipPvcVlan(voipPvcVlan);
				data.setRegisterStatus(registerStatus);
				data.setWireStatus(wireStatus);
				data.setUpstreamAttenuation(upstreamAttenuation);
				data.setDownstreamAttenuation(downstreamAttenuation);
				data.setUpstreamMaxRate(upstreamMaxRate);
				data.setDownstreamMaxRate(downstreamMaxRate);
				data.setLanStatus(lanStatus);
				data.setConnName(connName);
				data.setConnRate(connRate);
				data.setSsid(ssid);
				data.setWlanStatus(wlanStatus);
				data.setAssociatedNum(associatedNum);
				data.setEnable(enable);
				
				JsonEntity entity = new JsonEntity();
				entity.setCode("0");
				entity.setDetail("查询成功");
				entity.setValue(data);
				// 写出json
				RmsUtil.writeJson(entity, resp);
				return;
			}
			else
			{
				JsonEntity entity = new JsonEntity();
				entity.setCode("3");
				entity.setDetail("查询到多个设备");
				entity.setValue(resultList);
				// 写出json
				RmsUtil.writeJson(entity, resp);
				return;
			}
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		this.doGet(req, resp);
	}
}
