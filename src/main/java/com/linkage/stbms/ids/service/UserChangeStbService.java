
package com.linkage.stbms.ids.service;

import java.net.URL;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.stbms.ids.dao.UserChangeStbDAO;
import com.linkage.stbms.ids.obj.ResultBean;
import com.linkage.stbms.ids.util.UserChangeStbChecker;
import com.linkage.stbms.itv.main.StbServGlobals;

/**
 * 用户设备解绑接口(江西电信ITV终端管理平台与行业应用APP接口)
 * 
 * @author yinlei3 (73167)
 * @version 1.0
 * @since 2015年9月7日
 * @category com.linkage.stbms.ids.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UserChangeStbService
{

	private static Logger logger = LoggerFactory.getLogger(UserChangeStbService.class);

	public String work(String inParam)
	{
		logger.warn("UserChangeStbService==>inParam:" + inParam);
		UserChangeStbChecker check = new UserChangeStbChecker(inParam);
		// 入参检证
		if (false == check.check())
		{
			logger.warn(
					"[{}]入参验证没通过，返回：{}",
					new Object[] {
							"SelectType=" + check.getSelectType() + "；UserInfo="
									+ check.getUserInfo(), inParam });
			return check.getReturnXml();
		}
		UserChangeStbDAO dao = new UserChangeStbDAO();
		
		// 非爱运维用户校验IMEI
		if(check.getClientType() != 2){
			// 查询imei码是否合法
			Map<String, String> imeiMap = dao.getImei(check.getImei());
			if (imeiMap == null || imeiMap.isEmpty())
			{
				// 插入状态为未确认的imei信息 status:1 确认 0:未确认
				dao.insertImei(check.getImei());
				check.setRstCode("1006");
				check.setRstMsg("用户IMEI信息不合法");
				return check.getReturnXml();
			}
			if (!"1".equals(StringUtil.getStringValue(imeiMap, "status")))
			{
				check.setRstCode("1006");
				check.setRstMsg("用户IMEI信息不合法");
				return check.getReturnXml();
			}
		}
		else{
			if(StringUtil.IsEmpty(check.getUserName())){
				check.setRstCode("1008");
				check.setRstMsg("爱运维调用人员不能为空");
				return check.getReturnXml();
			}
		}
		
		// 随机获取 操作流水号
		String sequenceID = new DateTimeUtil().getLongDateChar()
				+ (new Random().nextInt(900) + 100);
		Map<String, String> customerMap = null;
		String oldMac = "";
		// 1：根据itv业务帐号查询
		if ("1".equals(check.getSelectType()))
		{
			// 调用解绑机顶盒MAC地址接口(解绑旧设备)(忽视返回结果)
			excuteBusinessService(check.getUserInfo(), null, sequenceID);
			// 调用解绑机顶盒MAC地址接口(解绑更换后的机顶盒设备)(忽视返回结果)
			excuteBusinessService(null, check.getNewStbMac(),
					new DateTimeUtil().getLongDateChar()
							+ (new Random().nextInt(900) + 100));
			// 查询对应itv客户
			customerMap = dao.getCustomerByAcc(check.getUserInfo());
			if ((customerMap != null) && (!customerMap.isEmpty()))
			{
				/** 旧的设备号 **/
				oldMac = StringUtil.getStringValue(customerMap, "cpe_mac");
				check.setOldStdMac(oldMac);
			}
			
		}
		// 2：根据MAC地址查询
		else
		{
			// 调用解绑机顶盒MAC地址接口(忽视返回结果)
			excuteBusinessService(null, check.getUserInfo(), sequenceID);
			// 调用解绑机顶盒MAC地址接口(解绑更换后的机顶盒设备)(忽视返回结果)
			excuteBusinessService(null, check.getNewStbMac(),
					new DateTimeUtil().getLongDateChar()
							+ (new Random().nextInt(900) + 100));
			// 查询对应itv客户
			customerMap = dao.getCustomerByMac(check.getUserInfo());
			
			oldMac = check.getUserInfo();
			check.setOldStdMac(oldMac);
		}
		if (customerMap == null || customerMap.isEmpty())
		{
			check.setRstCode("1005");
			check.setRstMsg("查找不到用户信息");
			return check.getReturnXml();
		}
		
		// 根据老mac地址/业务账号，解绑机顶盒
		dao.unbindDevice(StringUtil.getStringValue(customerMap, "city_id"), check.getUserInfo(),check.getSelectType());
				
		// 根据新mac 解除新mac与之前用户的对应关系
		dao.updateCustomerByNewMac(check.getNewStbMac());
		
		// 根据mac 解除mac对应设备的绑定情况 
		dao.unbindDevice(StringUtil.getStringValue(customerMap, "city_id"), check.getNewStbMac(),"2");
				
		// 根据itv业务账号更改mac
		dao.updateCustomerMac(StringUtil.getStringValue(customerMap, "customer_id"),
				check.getNewStbMac());
		return check.getReturnXml();
	}

	/**
	 * 调用解绑机顶盒MAC地址接口
	 * 
	 * @param userID
	 *            ITV帐号ID
	 * @param mac
	 *            MAC地址
	 * @param sequenceID
	 *            操作流水号
	 */
	private void excuteBusinessService(String userID, String mac, String sequenceID)
	{
		// 接口方法
		String method = "unbind";
		// 接口url
		String url = StringUtil.getStringValue(StbServGlobals
				.getLipossProperty("BusinessService.url"));
		logger.warn("开始调用解绑机顶盒MAC地址接口");
		logger.warn("url=" + url);
		logger.warn("method=" + method);
		logger.warn("入参   userID= " + userID + "  mac= " + mac + "  sequenceID= "
				+ sequenceID);
		try
		{
			Service service = new Service();
			Call call = null;
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(new URL(url));
			// 注册对象
			QName qn = new QName(url, method);
			call.registerTypeMapping(ResultBean.class, qn, new BeanSerializerFactory(
					ResultBean.class, qn), new BeanDeserializerFactory(ResultBean.class,
					qn));
			call.setOperationName(qn);
			// 添加参数
			call.addParameter("userID", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.addParameter("mac", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.addParameter("sequenceID", org.apache.axis.Constants.XSD_STRING,
					ParameterMode.IN);
			call.setReturnType(qn, ResultBean.class);
			ResultBean unbindReturn = (ResultBean) call.invoke(new Object[] { userID,
					mac, sequenceID });
			logger.warn("调用外部接口[解绑机顶盒MAC地址接口]的结果 : returnCode="
					+ unbindReturn.getReturnCode() + " returnMessage= "
					+ unbindReturn.getReturnMessage());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
}
