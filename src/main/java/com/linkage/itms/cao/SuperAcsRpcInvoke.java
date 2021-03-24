
package com.linkage.itms.cao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ACS.DevRpc;

import com.ailk.tr069.devrpc.obj.mq.Rpc;
import com.ailk.tr069.devrpc.obj.rpc.DevRpcCmdOBJ;
import com.linkage.commons.util.StringUtil;
import com.linkage.litms.acs.soap.io.XML;
import com.linkage.litms.acs.soap.io.XmlToRpc;
import com.linkage.litms.acs.soap.object.Fault;
import com.linkage.litms.acs.soap.object.ParameterValueStruct;
import com.linkage.litms.acs.soap.object.SoapOBJ;
import com.linkage.litms.acs.soap.service.GetParameterValues;
import com.linkage.litms.acs.soap.service.GetParameterValuesResponse;
import com.linkage.litms.acs.soap.service.SetParameterValues;

/**
 * @author Jason(3412)
 * @date 2009-6-15
 */
public abstract class SuperAcsRpcInvoke
{

	private static final Logger logger = LoggerFactory.getLogger(SuperAcsRpcInvoke.class);
	// 设备ID device_id
	protected String deviceId;
	// 参数更新时间,gather_time
	protected long updateTime;

	/** 设置设备参数 */
	public abstract SetParameterValues getSetParam();

	/** 采集设备结点参数 */
	public abstract GetParameterValues getResponseParam();

	/** 获取操作设备的参数数组 */
	public abstract DevRpc[] createDevRPCArray();

	private int flag = -9;

	/**
	 * 获取设备的采集结点信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return void
	 */
	private List<DevRpcCmdOBJ> gatherDeviceParam()
	{
		logger.debug("gatherDeviceParam()");
		DevRpc[] arrParam = null;
		if (null == deviceId || null == (arrParam = createDevRPCArray()))
		{
			logger.warn("gatherDeviceParam(deviceId , String rpcArr)  "
					+ "deivceId or rpcArr is null");
			logger.warn("gatherDeviceParam({} , {})  ", deviceId, arrParam);
		}
		ACSCorba acsCorba = new ACSCorba();
		List<DevRpcCmdOBJ> devRPCRep = acsCorba.execRPC(arrParam, 2);
		logger.debug("gatherDeviceParam(): return " + devRPCRep);
		return devRPCRep;
	}

	/**
	 * 从response的数组中取出，要获取的参数值 默认取出devRPCRep[0].rpcArr[0];如果不符合要求，覆盖该方法;
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-7-6
	 * @return String
	 */
	public String getRespStr()
	{
		logger.debug("getRespStr()");
		List<DevRpcCmdOBJ> devRPCRep = gatherDeviceParam();
		String rpcRes = null;
		if (null != devRPCRep)
		{
			if (null != devRPCRep.get(0))
			{
				flag = devRPCRep.get(0).getStat();
				ArrayList<Rpc> rpcList = devRPCRep.get(0).getRpcList();
				if (null != rpcList && !rpcList.isEmpty())
				{
					for (Rpc rpc : rpcList)
					{
						if (rpc.getRpcName() != null
								&& "GetParameterValuesResponse".equals(rpc.getRpcName()))
						{
							rpcRes = rpc.getValue();
							break;
						}
					}
				}
				else
				{
					logger.warn("ACS Reponse devRPCRep.get(0).getRpcList() is NULL...");
				}
			}
			else
			{
				logger.warn("ACS Reponse devRPCRep.get(0) is NULL...");
			}
		}
		else
		{
			logger.warn("ACS Reponse DevRPCRep is NULL...");
		}
		logger.debug("getRespStr(): return " + rpcRes);
		return rpcRes;
	}

	/**
	 * 获取设备的采集结点信息
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-6-15
	 * @return void
	 */
	public Map<String, String> getDevParamMap(String getRes)
	{
		logger.debug("getDevParamMap({})", getRes);
		Map<String, String> paramMap = null;
		if (false == StringUtil.IsEmpty(getRes))
		{
			GetParameterValuesResponse getParameterValuesResponse = new GetParameterValuesResponse();
			SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(getRes));
			if (soapOBJ == null)
			{
				return paramMap;
			}
			getParameterValuesResponse = XmlToRpc.GetParameterValuesResponse(soapOBJ
					.getRpcElement());
			int arrayLen = getParameterValuesResponse.getParameterList().length;
			ParameterValueStruct[] paramStruct = new ParameterValueStruct[arrayLen];
			paramStruct = getParameterValuesResponse.getParameterList();
			// 获取ping测试结构Map
			paramMap = new HashMap<String, String>();
			if (null != paramStruct && paramStruct.length > 0)
			{
				for (int i = 0; i < paramStruct.length; i++)
				{
					paramMap.put(paramStruct[i].getName(),
							paramStruct[i].getValue().para_value);
				}
			}
		}
		else
		{
			logger.warn("ACS Reponse getRes is NULL...");
		}
		logger.debug("getDevParamMap(): return " + paramMap);
		return paramMap;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public long getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(long updateTime)
	{
		this.updateTime = updateTime;
	}

	/**
	 * 返回调用是否成功
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-11-3
	 * @return boolean 成返回true； 失败返回false
	 */
	public static boolean diagSeccuss(int faultCode)
	{
		logger.debug("diagSeccuss()");
		return 1 == faultCode;
	}

	/**
	 * 错误码分析，返回错误代码
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-11-3
	 * @return int Integer.MAX_VALUE表示成功，其他表示相关错误码
	 */
	public static int fault(String rpcRes)
	{
		logger.debug("fault({})", rpcRes);
		int flag = -9;
		if (StringUtil.IsEmpty(rpcRes))
		{
			return flag;
		}
		if ("XXX".equals(rpcRes.substring(0, 3)))
		{
			flag = StringUtil.getIntegerValue(rpcRes.substring(3), -9);
		}
		else
		{
			Fault fault = null;
			try
			{
				SoapOBJ soapOBJ = XML.getSoabOBJ(XML.CreateXML(rpcRes));
				if (soapOBJ != null)
				{
					fault = XmlToRpc.Fault(soapOBJ.getRpcElement());
				}
				else
				{
					return flag;
				}
			}
			catch (Exception e)
			{
				logger.error("{}", e.getMessage());
			}
			if (fault != null)
			{
				flag = StringUtil.getIntegerValue(fault.getDetail().getFaultCode(), -9);
				logger.warn("setValue()={}", fault.getDetail().getFaultString());
			}
			else if (null != rpcRes)
			{
				flag = Integer.MAX_VALUE;
			}
		}
		return flag;
	}

	/**
	 * @return the flag
	 */
	public int getFlag()
	{
		return flag;
	}

	/**
	 * @param flag
	 *            the flag to set
	 */
	public void setFlag(int flag)
	{
		this.flag = flag;
	}
}
