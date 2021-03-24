
package com.linkage.itms.socket.pwdsyn.bio;

import java.util.Map;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.cao.PreServInfoOBJ;
import com.linkage.itms.commom.DateUtil;
import com.linkage.itms.socket.core.DefaultMsgAction;
import com.linkage.itms.socket.pwdsyn.bean.PwdSynBean;
import com.linkage.itms.socket.pwdsyn.dao.PwdSynDAO;
import com.linkage.itms.socket.pwdsyn.util.PwdSynUtil;

/**
 * <pre>
 * 新疆电信\raduis和ITMS之间密码同步接口
 * 1.ITMS系统提供密码同步socket接口，供raduis系统调用,
 *   接口内容：UserPwdSync2ITMS {123} {test123} {bfa9e032e4eb4448} {20140221 144600}
 *   第一个表示序列号；第二个是宽带账号；第三个是加密后的宽带密码；第四个是yyyymmdd hhmmss（带空格）的时间戳
 * 2.ITMS需要校验raduis发送的时间戳，ITMS每次在更新时需要记录更新时间，
 *   如果接口发送过来的时间戳早于系统更新的时间戳，那么则不进行密码更新，
 *   直接返回接口协议中ReturnCode=2-不需要同步至ITMS
 * 3.同步完成后，接口需要进行判断此用户的上网方式是否为路由，
 *   如果为路由模式 则需要对更新的密码进行实时下发触发，如果为桥接则不需要进行下发
 * </pre>
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket.pwdsyn.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class PwdSynBIO extends DefaultMsgAction
{

	private PwdSynDAO pwdSynDAO = new PwdSynDAO();
	private PwdSynBean bean = null;

	@Override
	public String execute(String message)
	{
		bean = PwdSynUtil.analize(message);
		logger.warn("translate to pwd syn bean[{}]", bean);
		if (!validate())
		{
			return returnMsg();
		}
		doWork();
		return returnMsg();
	}

	private void doWork()
	{
		// 1.查询用户信息
		Map<String, String> customerMap = pwdSynDAO.queryCustomer(bean.getAccount());
		if (customerMap == null || customerMap.isEmpty())
		{
			setResult("1", "用户不存在");
			return;
		}
		long lastUpdateTime = StringUtil.getLongValue(customerMap, "updatetime");
		if (bean.getTimestamp() <= lastUpdateTime)
		{
			logger.warn(
					"pwdSyn timestamp is[{}] before net last modified time[{}], return.",
					DateUtil.transTime(bean.getTimestamp(), "yyyy-MM-dd HH:mm:ss"),
					DateUtil.transTime(lastUpdateTime, "yyyy-MM-dd HH:mm:ss"));
			setResult("2", "不需要同步至ITMS");
			return;
		}
		boolean succ = pwdSynDAO.updateCustomerPwd(bean,customerMap.get("wan_type"));
		if (!succ)
		{
			logger.warn("update hgwcust_serv_info passwd failed.");
			setResult("20", "其他错误");
			return;
		}
		// 如果是路由上网，则需要实时下发业务
		if ("2".equals(customerMap.get("wan_type")))
		{
			Map<String, String> deviceMap = pwdSynDAO.queryDevice(customerMap
					.get("user_id"));
			if (deviceMap == null || deviceMap.isEmpty())
			{
				logger.warn("username[{}] do not bind device, no need send service.",
						bean.getAccount());
			}
			else
			{
				// 业务下发
				PreServInfoOBJ preInfoObj = new PreServInfoOBJ(
						customerMap.get("user_id"), deviceMap.get("device_id"),
						deviceMap.get("oui"), deviceMap.get("device_serialnumber"), "10",
						"1");
				logger.warn("username[{}] send service.[{}]", bean.getAccount(), preInfoObj);
				CreateObjectFactory.createPreProcess().processServiceInterface(CreateObjectFactory.createPreProcess()
						.GetPPBindUserList(preInfoObj));
			}
		}
		setResult("0", "同步成功");
	}

	private boolean validate()
	{
		if (StringUtil.IsEmpty(bean.getAccount()))
		{
			setResult("1", "用户不存在");
			return false;
		}
		if (StringUtil.IsEmpty(bean.getUserPwd()))
		{
			setResult("20", "其他错误");
			return false;
		}
		return true;
	}

	private String returnMsg()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(bean.getSerialNo()).append("} {").append(resultCode)
				.append("} {").append(resultMsg).append("}\n");
		return sb.toString();
	}
}
