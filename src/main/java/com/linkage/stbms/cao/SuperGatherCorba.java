package com.linkage.stbms.cao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SuperGather.GatherParam;
import SuperGather.GatherResult;

import com.linkage.stbms.dao.InitDAO;
import com.linkage.stbms.itv.main.Global;

/**
 * @author Jason(3412)
 * @date 2009-12-18
 */
public class SuperGatherCorba {

	private static Logger logger = LoggerFactory
			.getLogger(SuperGatherCorba.class);

	/**
	 * 设备信息重新采集.
	 * 
	 * @param deviceIdArr
	 * @param type
	 */
	public void gatherCpeParams(String[] deviceIdArr, int type) {
		logger.debug("gatherCpeParams({},{})", deviceIdArr, type);
		// 准实时,多次采集直接报成功
		gatherCpeParams(deviceIdArr, type, 0);
	}

	/**
	 * 设备信息重新采集.
	 * 
	 * @param deviceId
	 * @param type
	 */
	public void gatherCpeParams(String deviceId, int type) {
		logger.debug("gatherCpeParams({},{})", deviceId, type);
		// 准实时,多次采集直接报成功
		gatherCpeParams(deviceId, type, 0);
	}

	/**
	 * 获取设备采集信息.
	 * 
	 * @param deviceIdArr
	 * @param type
	 */
	public GatherResult[] getCpeParams(String[] deviceIdArr, int type) {
		logger.debug("getCpeParams({},{})", deviceIdArr, type);
		// 准实时,多次采集直接报成功
		return getCpeParams(deviceIdArr, type, 0);
	}

	/**
	 * 获取设备采集信息.
	 * 
	 * @param deviceId
	 * @param type
	 */
	public int getCpeParams(String deviceId, int type) {
		logger.debug("getCpeParams({},{})", deviceId, type);
		// 准实时,多次采集直接报成功
		return getCpeParams(deviceId, type, 0);
	}

	/**
	 * 设备信息重新采集.
	 * 
	 * @param deviceIdArr
	 *            deviceIds数组, type 采集结点, invokeType是否实时采集
	 * @param type
	 */
	public void gatherCpeParams(String[] deviceIdArr, int type, int invokeType) {
		logger.debug("gatherCpeParams({},{})", deviceIdArr, type);

		if (deviceIdArr == null || deviceIdArr.length == 0) {
			logger.error("deviceIdArr == null");

			return;
		}

		try {
			Global.G_SuperGatherManager.getCpeParams(createGatherParam(
					deviceIdArr, type, invokeType));
		} catch (Exception e) {
			logger.warn("CORBA SuperGather Error:{},Rebind.", e.getMessage());

			InitDAO.initSuperGather();
			try {
				Global.G_SuperGatherManager.getCpeParams(createGatherParam(
						deviceIdArr, type, invokeType));
			} catch (RuntimeException e1) {
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
	}

	/**
	 * 设备信息重新采集.
	 * 
	 * @param deviceId
	 *            deviceId, type 采集结点, invokeType是否实时采集
	 * @param type
	 */
	public void gatherCpeParams(String deviceId, int type, int invokeType) {
		logger.debug("gatherCpeParams({},{})", deviceId, type);

		if (deviceId == null) {
			logger.error("deviceIdArr == null");

			return;
		}

		String[] deviceIdArr = new String[] { deviceId };

		try {
			Global.G_SuperGatherManager.getCpeParams(createGatherParam(
					deviceIdArr, type, invokeType));
		} catch (Exception e) {
			logger.warn("CORBA SuperGather Error:{},Rebind.", e.getMessage());

			InitDAO.initSuperGather();
			try {
				Global.G_SuperGatherManager.getCpeParams(createGatherParam(
						deviceIdArr, type, invokeType));
			} catch (RuntimeException e1) {
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}
	}

	/**
	 * 获取设备采集信息.
	 * 
	 * @param deviceIdArr
	 *            deviceIds数组, type 采集结点, invokeType是否实时采集
	 * @param type
	 */
	public GatherResult[] getCpeParams(String[] deviceIdArr, int type,
			int invokeType) {
		logger.debug("getCpeParams({},{})", deviceIdArr, type);

		GatherResult[] resultArr = null;

		if (deviceIdArr == null || deviceIdArr.length == 0) {
			logger.error("deviceIdArr == null");

			return resultArr;
		}

		try {
			resultArr = Global.G_SuperGatherManager
					.getCpeParams(createGatherParam(deviceIdArr, type,
							invokeType));
		} catch (Exception e) {
			logger.warn("CORBA SuperGather Error:{},Rebind.", e.getMessage());

			InitDAO.initSuperGather();
			try {
				resultArr = Global.G_SuperGatherManager
						.getCpeParams(createGatherParam(deviceIdArr, type,
								invokeType));
			} catch (RuntimeException e1) {
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}

		return resultArr;
	}

	/**
	 * 获取设备采集信息.
	 * 
	 * @param deviceId
	 *            deviceId, type 采集结点, invokeType是否实时采集
	 * @param type
	 */
	public int getCpeParams(String deviceId, int type, int invokeType) {
		logger.debug("getCpeParams({},{})", deviceId, type);

		int result = 5;

		if (deviceId == null) {
			logger.error("deviceId == null");

			return result;
		}

		GatherResult[] resultArr = null;
		String[] deviceIdArr = new String[] { deviceId };

		try {
			resultArr = Global.G_SuperGatherManager
					.getCpeParams(createGatherParam(deviceIdArr, type,
							invokeType));
		} catch (Exception e) {
			logger.warn("CORBA SuperGather Error:{},Rebind.", e.getMessage());

			InitDAO.initSuperGather();
			try {
				resultArr = Global.G_SuperGatherManager
						.getCpeParams(createGatherParam(deviceIdArr, type,
								invokeType));
			} catch (Exception e1) {
				logger.error("CORBA SuperGather Error:{}", e1.getMessage());
			}
		}

		if (resultArr != null) {
			result = resultArr[0].result;

			resultArr = null;
		}

		return result;
	}

	/**
	 * 生成SuperGather模块的调用对象
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-8-20
	 * @return GatherParam
	 */
	public GatherParam createGatherParam(String[] deviceIdArr, int paramType,
			int invokeType) {
		logger.debug("createGatherParam({},{},{})", new Object[] { deviceIdArr,
				paramType, invokeType });
		if (null == deviceIdArr) {
			logger.warn("deviceIdArr is null");
			return null;
		}
		GatherParam gatherParam = new GatherParam();
		gatherParam.deviceIdArr = deviceIdArr;
		gatherParam.invokeType = invokeType;
		gatherParam.paramType = paramType;
		return gatherParam;
	}
}
