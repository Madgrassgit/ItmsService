
package com.linkage.itms.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.CreateObjectFactory;
import com.linkage.itms.dao.RecordLogDAO;
import com.linkage.itms.dao.UpgradeDao;
import com.linkage.itms.dispatch.obj.UpgradeChecker;

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月24日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpgradeService
{

	private static Logger logger = LoggerFactory.getLogger(UpgradeService.class);

	/**
	 * @param param
	 *            <?xml version="1.0" encoding="GBK"?> <root>
	 *            <CmdID>123456789012345</CmdID> <CmdType>CX_01</CmdType>
	 *            <ClientType>5</ClientType> <Param> <Operator>njadmin</Operator>
	 *            <CallDate>1304234577</CallDate> <Tasknumber>2014113001</Tasknumber>
	 *            </Param> </root>
	 * @return
	 */
	public String work(String param)
	{
		UpgradeChecker checker = new UpgradeChecker(param);
		try
		{
			// 检查合法性
			if (false == checker.check())
			{
				logger.error("servicename[UpgradeService]cmdId[{}]验证未通过，返回：{}",
						new Object[] { checker.getCmdId(), checker.getReturnXml() });
				return checker.getReturnXml();
			}
			Long taskNumber = checker.getTaskNumber();
			UpgradeDao dao = new UpgradeDao();
			List<HashMap<String, String>> list = dao
					.selectVersionUpgradeDevDeviceIdByTaskId(taskNumber);
			List<String> deviceIds = new ArrayList<String>();
			List<String> deviceTypeIds = new ArrayList<String>();
			if (list != null && !list.isEmpty())
			{
				for (Map map : list)
				{
					Object object = map.get("device_id");
					if (object != null)
					{
						deviceIds.add(object + "");
						Map deviceTypeIdByDeviceId = dao
								.selectDeviceTypeIdByDeviceId(object + "");
						Object object2 = deviceTypeIdByDeviceId.get("devicetype_id");
						deviceTypeIds.add(object2 + "");
					}
				}
			}
			// 通过tasknumber查询到的结果是空
			else{
				checker.setResult(1);
				checker.setResultDesc("该任务号不存在或该任务号下不存在设备");
			}
			logger.info("升级设备的参数：deviceIds = {}, deviceTypeIds = {}", deviceIds,
					deviceTypeIds);
			if (!deviceIds.isEmpty() && !deviceTypeIds.isEmpty())
			{
				String[] deviceIdArr = new String[deviceIds.size()];
				String[] deviceTypeIdArr = new String[deviceIds.size()];
				for (int i = 0; i < deviceIds.size(); i++)
				{
					deviceIdArr[i] = deviceIds.get(i);
					deviceTypeIdArr[i] = deviceTypeIds.get(i);
				}
				boolean processDeviceStrategy = CreateObjectFactory.createPreProcess()
						.processDeviceStrategy(deviceIdArr, "5", deviceTypeIdArr);
				// 升级完成后，将任务状态改成已完成
				// Long taskId, String operator, Long calldate, Integer status)
				dao.updateVersionUpgradeByTaskId(taskNumber, checker.getOperator(),
						checker.getCallDate(), 1);
				// 记录日志
				new RecordLogDAO().recordDispatchLog(checker, checker.getOperator(),
						"UpgradeService");
			}
		}
		catch (Exception e)
		{
			logger.error("升级设备发生异常:{}", e);
			checker.setResult(1);
			checker.setResultDesc("升级设备发生异常");
			return checker.getReturnXml();
		}
		return checker.getReturnXml();
	}
}
