package com.linkage.itms.dispatch.gsdx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.dao.ServUserDAO;
import com.linkage.itms.dao.UserDeviceDAO;
import com.linkage.itms.dispatch.gsdx.obj.ServiceChangeDealXML;

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
	
	//用户宽带帐号
	private final int USERINFOTYPE_1 =1;
	
	private ServiceChangeDealXML dealXML;


	public String work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);

		// 解析获得入参
		dealXML = new ServiceChangeDealXML(methodName);

		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			return dealXML.returnXML();
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");

		UserDeviceDAO userDevDao = new UserDeviceDAO();
		Map<String, String> userDevInfo  = null;
		
		userDevInfo = userDevDao.queryUserInfo(USERINFOTYPE_1, dealXML.getPppUsename(), null);
		

		if (null == userDevInfo || userDevInfo.isEmpty()) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]logic_Id=[{}],ppp_username=[{}]查无此用户",
					new Object[] { dealXML.getOpId(), dealXML.getLogicId(), dealXML.getPppUsename()});
			dealXML.setResulltCode(-1);
			dealXML.setResultDesc("用户不存在");
			return dealXML.returnXML();
		}else{
			String deviceId = userDevInfo.get("device_id");
			String user_id = userDevInfo.get("user_id");
			String oldpasswd = userDevInfo.get("passwd");
			String city_id = userDevInfo.get("city_id");
			String loid = userDevInfo.get("username");
			
			if (StringUtil.IsEmpty(deviceId)) {
				// 未绑定设备
				logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]此客户未绑定",
						new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
				dealXML.setResulltCode(-99);
				dealXML.setResultDesc("此用户未绑定设备");
				userDevDao.prossBssSheet(loid,city_id,inXml,"-8","此用户未绑定设备");
				return dealXML.returnXML();
			}
			else if (dealXML.getNewPassWord().equals(oldpasswd)) {
				// 未绑定设备
				logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}],ppp_username[{}],newPassWord[{}]新密码与旧密码相同",
						new Object[] { dealXML.getOpId(), dealXML.getPppUsename(),dealXML.getNewPassWord(), });
				dealXML.setResulltCode(-99);
				dealXML.setResultDesc("新密码与旧密码相同");
				userDevDao.prossBssSheet(loid,city_id,inXml,"-8","新密码与旧密码相同");
				return dealXML.returnXML();
			}
			else{
				
				ArrayList<HashMap<String, String>> devList = userDevDao.qryDevId(deviceId);
				if (null == devList || devList.size()==0) {
					// 未绑定设备
					logger.warn(methodName+"["+dealXML.getOpId()+"]查询不到设备信息,device_id="+deviceId);
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("此用户未绑定设备");
					userDevDao.prossBssSheet(loid,city_id,inXml,"-8","此用户未绑定设备");
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
					userDevDao.prossBssSheet(loid,city_id,inXml,"-8","此用户没有开通任何宽带业务");
					return dealXML.returnXML();
				}
				//更改密码
				userDevDao.modCustomerPwd(user_id, dealXML.getPppUsename(), dealXML.getNewPassWord());
				userDevDao.prossBssSheet(loid,city_id,inXml,"1","");
				//业务下发
				boolean res = serviceDoner(deviceId, user_id, oui, devSN);
				if(!res){
					logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]netUserName[{}]newPassWord[{}]下发特定业务，调用后台预读模块失败，业务类型为：[{}]",
							new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
					dealXML.setResulltCode(-99);
					dealXML.setResultDesc("下发业务失败，请稍后重试");
					userDevDao.prossBssSheet(loid,city_id,inXml,"-8","下发业务失败，请稍后重试");
					return dealXML.returnXML();
				}
			}
		}
		dealXML.setResulltCode(1);
		dealXML.setResultDesc("执行成功");
		logger.warn(methodName+"["+dealXML.getOpId()+"]cmdId[{}]ppp_username[{}]newPassWord[{}]执行成功:{}",
				new Object[] { dealXML.getOpId(), dealXML.getPppUsename(), dealXML.getNewPassWord() });
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
