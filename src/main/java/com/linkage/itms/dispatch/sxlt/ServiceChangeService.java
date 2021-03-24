package com.linkage.itms.dispatch.sxlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.sxlt.obj.ServiceChangeDealXML;
import com.linkage.itms.dispatch.sxlt.service.ServiceFather;

/**
 * 重庆电信修改宽带密码接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年06月03日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class ServiceChangeService extends ServiceFather{

	public ServiceChangeService(String methodName){
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(ServiceChangeService.class);

	//业务类型（宽带）
	private final String ServiceType = "10";
	private long id = RecordLogDAO.getRandomId();
	
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	
	private ServiceChangeDealXML dealXML;


	public String work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		new RecordLogDAO().recordLog(id, inXml, "ServiceChange");
		// 解析获得入参
		dealXML = new ServiceChangeDealXML(methodName);

		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			new RecordLogDAO().recordDispatchLog(dealXML,id,"");
			return dealXML.returnXML();
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");

		if("wband-X".equals(dealXML.getOrderType())){
			UserDeviceDAO userDevDao = new UserDeviceDAO();
			Map<String, String> userDevInfo  = null;
			
			userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_1, dealXML.getPppUsename(), null);
			
	
			if (null == userDevInfo || userDevInfo.isEmpty()) {
				logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]logic_Id=[{}],ppp_username=[{}]查无此用户",
						new Object[] { dealXML.getOpId(), dealXML.getLogicId(), dealXML.getPppUsename()});
				dealXML.setResulltCode(-1);
				dealXML.setResultDesc("用户不存在");
				new RecordLogDAO().recordDispatchLog(dealXML,id,"");
				return dealXML.returnXML();
			}else{
				String deviceId = userDevInfo.get("device_id");
				String user_id = userDevInfo.get("user_id");
				String oldpasswd = userDevInfo.get("passwd");
	
				if (StringUtil.IsEmpty(deviceId)) {
					// 未绑定设备
					logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]此客户未绑定",
							new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("此用户未绑定设备");
					new RecordLogDAO().recordDispatchLog(dealXML,id,"");
					return dealXML.returnXML();
				}
				else if (dealXML.getNewPassWord().equals(oldpasswd)) {
					// 未绑定设备
					logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]新密码与旧密码相同",
							new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("新密码与旧密码相同");
					new RecordLogDAO().recordDispatchLog(dealXML,id,"");
					return dealXML.returnXML();
				}
				else{
					
					ArrayList<HashMap<String, String>> devList = userDevDao.qryDevId(deviceId);
					if (null == devList || devList.size()==0) {
						// 未绑定设备
						logger.warn(methodName+"["+dealXML.getOpId()+"]查询不到设备信息,device_id="+deviceId);
						dealXML.setResulltCode(-99);
						dealXML.setResultDesc("此用户未绑定设备");
						new RecordLogDAO().recordDispatchLog(dealXML,id,"");
						return dealXML.returnXML();
					}
					
					String oui = devList.get(0).get("oui");
					String devSN = devList.get(0).get("device_serialnumber");
					
					// 1.查询此用户开通的业务信息
					Map<String, String> userServMap = userDevDao.queryServForNet(user_id);
					if (null == userServMap || userServMap.isEmpty())
					{
						// 没有开通业务
						logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]ppp_username[{}]此用户没有开通任何宽带业务",
								new Object[] { dealXML.getOpId(), dealXML.getPppUsename() });
						dealXML.setResulltCode(-99);
						dealXML.setResultDesc("此用户没有开通任何宽带业务");
						new RecordLogDAO().recordDispatchLog(dealXML,id,"");
						return dealXML.returnXML();
					}
					//更改密码
					userDevDao.modCustomerPwd(user_id, dealXML.getPppUsename(), dealXML.getNewPassWord());
	
					//业务下发
					boolean res = serviceDoner(deviceId, user_id, oui, devSN);
					if(!res){
						logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]netUserName[{}]newPassWord[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
								new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
						dealXML.setResulltCode(-99);
						dealXML.setResultDesc("下发业务失败，请稍后重试");
						new RecordLogDAO().recordDispatchLog(dealXML,id,"");
						return dealXML.returnXML();
					}
				}
			}
			dealXML.setResulltCode(1);
			dealXML.setResultDesc("执行成功");
			logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]ppp_username[{}]newPassWord[{}]执行成功:{}",
					new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
		}
		else if("iptv-X".equals(dealXML.getOrderType())){
				/*UserStbInfoDAO userDevDao = new UserStbInfoDAO();
				Map<String, String> userDevInfo  = null;
				
				userDevInfo = userDevDao.qryStbServInfo(dealXML.getPppUsename());
				
		
				if (null == userDevInfo || userDevInfo.isEmpty()) {
					logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]logic_Id=[{}],ppp_username=[{}]查无此用户",
							new Object[] { dealXML.getOpId(), dealXML.getLogicId(), dealXML.getPppUsename()});
					dealXML.setResulltCode(-1);
					dealXML.setResultDesc("用户不存在");
					new RecordLogDAO().recordDispatchLog(dealXML,id,"");
					return dealXML.returnXML();
				}else{
					String deviceId = userDevInfo.get("device_id");
					String user_id = userDevInfo.get("customer_id");
					String oldpasswd = userDevInfo.get("serv_pwd");
					String oui = userDevInfo.get("oui");
					String device_serialnumber = userDevInfo.get("device_serialnumber");
		
					if (StringUtil.IsEmpty(deviceId)) {
						// 未绑定设备
						logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]此客户未绑定",
								new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
						dealXML.setResulltCode(-99);
						dealXML.setResultDesc("此用户未绑定设备");
						new RecordLogDAO().recordDispatchLog(dealXML,id,"");
						return dealXML.returnXML();
					}
					else if (dealXML.getNewPassWord().equals(oldpasswd)) {
						// 未绑定设备
						logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]新密码与旧密码相同",
								new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
						dealXML.setResulltCode(-99);
						dealXML.setResultDesc("新密码与旧密码相同");
						new RecordLogDAO().recordDispatchLog(dealXML,id,"");
						return dealXML.returnXML();
					}
					else{
						//更改密码
						userDevDao.updateStbCustomerPswd(dealXML.getPppUsename(), dealXML.getNewPassWord(), null, null);
		
						//业务下发
						StringBuffer xmlSB = new StringBuffer();
						xmlSB.append("<ServXml><servList><serv>");
						xmlSB.append("<userId>").append(user_id).append("</userId>");
						xmlSB.append("<deviceId>").append(deviceId).append("</deviceId>");
						xmlSB.append("<serviceId>").append("120").append("</serviceId>");
						xmlSB.append("<oui>").append(oui).append("</oui>");
						xmlSB.append("<deviceSn>").append(device_serialnumber).append("</deviceSn>");
						xmlSB.append("</serv></servList></ServXml>");
						boolean res = CreateObjectFactory.createPreProcess(Global.GW_TYPE_STB).processSTBServiceInterface(xmlSB.toString());
						
						if(!res){
							logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]netUserName[{}]newPassWord[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
									new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
							dealXML.setResulltCode(-99);
							dealXML.setResultDesc("下发业务失败，请稍后重试");
							new RecordLogDAO().recordDispatchLog(dealXML,id,"");
							return dealXML.returnXML();
						}
					}
				}
				dealXML.setResulltCode(1);
				dealXML.setResultDesc("执行成功");
				logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]ppp_username[{}]newPassWord[{}]执行成功:{}",
						new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });*/
			
			dealXML.setResulltCode(1);
			dealXML.setResultDesc("执行成功");
			logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]ppp_username[{}]newPassWord[{}]非wband-x不做处理:{}",
					new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
			
		}
		
		new RecordLogDAO().recordDispatchLog(dealXML,id,"");
		return dealXML.returnXML();
		

	}

	/**
	 * 业务下发
	 * @param deviceId 设备编码
	 * @param user_id 用户ID
	 * @param oui 设备OUI
	 * @param devSN 设备SN
	 * @return 下发结果
	 */
	private boolean serviceDoner(String deviceId, String user_id, String oui,
			String devSN) {
		logger.warn(methodName+"["+dealXML.getOpId()+"]serviceDoner({})",new Object[]{deviceId,user_id,oui,devSN});
		boolean res = false;

		ServUserDAO servUserDao = new ServUserDAO();

		// 更新业务用户表的业务开通状态
		servUserDao.updateServOpenStatus(StringUtil.getLongValue(user_id),StringUtil.getIntegerValue(ServiceType));
		// 预读调用对象
		PreServInfoOBJ preInfoObj = new PreServInfoOBJ(user_id, deviceId, oui, devSN, ServiceType, "1");
		if (1 == CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
				.GetPPBindUserList(preInfoObj)))
		{
			res = true;
		}

		return res;
	}




}
