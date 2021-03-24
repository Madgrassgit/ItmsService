
package com.linkage.itms.dispatch.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.dao.UpgradeResultsDAO;
import com.linkage.itms.dispatch.obj.UpgradeResultsChecker;
import com.linkage.system.utils.StringUtils;

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2014年12月25日
 * @category com.linkage.itms.dispatch.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class UpgradeResultsService
{

	private static Logger logger = LoggerFactory.getLogger(UpgradeResultsService.class);

	public String work(String param)
	{
		UpgradeResultsChecker checker = new UpgradeResultsChecker(param);
		if (false == checker.check())
		{
			logger.error("servicename[UpgradeResultsService]cmdId[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getWrongReturnXml() });
			return checker.getReturnXml();
		}
		UpgradeResultsDAO dao = new UpgradeResultsDAO();
		Long taskNumber = checker.getTaskNumber();
		// 城市名称
		String cityNames = checker.getCityName();
		List<String> cityIdList = new ArrayList<String>();
		if (cityNames != null && !"".equals(cityNames))
		{
			String[] cityNameArr = cityNames.split("、");
			if (cityNameArr != null)
			{
				for (String cityName : cityNameArr)
				{
					String cityId = dao.selectCityIdByCityName(cityName);
					if (cityId != null&& !"".equals(cityId))
					{
						cityIdList.add(cityId);
					}
				}
			}
		}
		// 查询升级成功的device_id列表
		ArrayList<HashMap<String, String>> list = dao.selectSuccessDeviceListIdByTaskId(
				taskNumber, StringUtils.weave(cityIdList, "','"));
		// 版本型号：
		String device_model = null;
		// 设备型号id
		String device_model_id = null;
		// 厂商id
		String vendor_id = null;
		// 软件版本
		String softwareversion = null;
		Integer successfulNumber = null;
		Integer upgradeNumber = null;
		String reason = null;
		// 老版本
		StringBuffer softwareversions = new StringBuffer();
		if (list != null && !list.isEmpty())
		{
			successfulNumber = list.size();
			HashMap<String, String> map = list.get(0);
			if (map != null)
			{
				String deviceId = map.get("device_id");
				Map<String, String> deviceModelMap = dao
						.selectDeviceModelByDeviceId(deviceId);
				device_model = deviceModelMap.get("device_model");
				device_model_id = deviceModelMap.get("device_model_id");
				vendor_id = deviceModelMap.get("vendor_id");
				// 查询版本表
				Map<String, String> deviceTypeByDeviceId = dao
						.selectDeviceTypeByDeviceId(deviceId);
				if (deviceTypeByDeviceId != null)
				{
					softwareversion = deviceTypeByDeviceId.get("softwareversion");
					reason = deviceTypeByDeviceId.get("reason");
				}
				// 通过厂商id和版本型号，查询软件版本
				ArrayList<HashMap<String, String>> versions = dao
						.queryByVendorIdAndDeviceModelId(vendor_id, device_model_id);
				if (versions != null && !versions.isEmpty())
				{
					for (int i = 0; i < versions.size(); i++)
					{
						HashMap<String, String> map3 = versions.get(i);
						String oldVersion = null;
						if (map3 != null)
						{
							oldVersion = map3.get("softwareversion");
						}
						softwareversions.append(oldVersion != null ? oldVersion : "");
						if (i < versions.size() - 1)
						{
							softwareversions.append("、");
						}
					}
				}
			}
		}
		upgradeNumber = dao.selectCountDeviceByTaskIdAndCityIds(taskNumber,
				StringUtils.weave(cityIdList, "','"));
		device_model = device_model != null ? device_model : "";
		checker.setDevice_model(device_model);
		
		softwareversion = softwareversion != null ? softwareversion : "";
		checker.setSoftwareversion(softwareversion);
		
		softwareversions = softwareversions != null ? softwareversions : new StringBuffer("");
		checker.setSoftwareversions(softwareversions.toString());
		
		reason = reason != null ? reason : "";
		checker.setReason(reason);
		
		upgradeNumber = upgradeNumber != null ? upgradeNumber : 0;
		checker.setUpgradeNumber(upgradeNumber);
		
		successfulNumber = successfulNumber != null ? successfulNumber : 0;
		checker.setSuccessfulNumber(successfulNumber);
		
		cityNames = cityNames != null ? cityNames : "";
		checker.setCityName(cityNames);
		taskNumber = taskNumber != null ? taskNumber : 0;
		checker.setTaskNumber(taskNumber);
		logger.info(
				"数据准备结束，准备回参，回参参数：device_model={}, softwareversion={}, softwareversions={}, reason={}, upgradeNumber={}, successfulNumber={}, taskNumber={}, cityNames={}",
				new Object[] { device_model, softwareversion, softwareversions, reason,
						upgradeNumber, successfulNumber, taskNumber, cityNames });
		return checker.getReturnXml();
	}
}
