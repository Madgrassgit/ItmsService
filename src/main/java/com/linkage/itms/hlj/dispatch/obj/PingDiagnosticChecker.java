
package com.linkage.itms.hlj.dispatch.obj;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class PingDiagnosticChecker extends HljBaseChecker
{

	// 日志记录对象
	private static Logger logger = LoggerFactory.getLogger(PingDiagnosticChecker.class);
	/**
	 * Wan通道
	 */
	private String wanPassageWay = "";
	private String inParam = null;
	private String userIndex = null;
	private String DestIp = null;
	private String TimeOut = null;
	private String PackSize = null;
	private String PackCount = null;

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public PingDiagnosticChecker(String inParam)
	{
		this.inParam = inParam;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("ServiceDoneFailChecker==>check()" + inParam);
		try
		{
			JSONObject jo = new JSONObject(inParam);
			String JsonStr = jo.getString("input");
			infcode = jo.getString("infcode");
			JSONObject jo2 = new JSONObject(JsonStr);
			StreamingNum = jo2.getString("StreamingNum");
			userIndex = jo2.getString("UserIndex");
			JSONObject jo3 = new JSONObject(userIndex);
			QueryNum = jo3.getString("Index");
			QueryType = Integer.parseInt(jo3.getString("Type"));
			DestIp = jo3.getString("DestIp");
			TimeOut = jo3.getString("TimeOut");
			PackSize = jo3.getString("PackSize");
			PackCount = jo3.getString("PackCount");
			wanPassageWay = jo3.getString("wanPassageWay");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!"20".equals(infcode))
		{
			result = 8;
			resultDesc = "接口代码与方法名不一致";
			return false;
		}
		if (2 == QueryType)
		{
			if (QueryNum.length() < 6)
			{
				result = 8;
				resultDesc = "SN至少输入6位数";
				return false;
			}
		}
		if (!"1".equals(wanPassageWay)&&!"2".equals(wanPassageWay)){
			result = 8;
			resultDesc = "wanPassageWay只能为1或者2";
			return false;
		}
		if (StringUtil.IsEmpty(DestIp) || StringUtil.IsEmpty(userIndex)
				|| StringUtil.IsEmpty(TimeOut) || StringUtil.IsEmpty(PackSize)
				|| StringUtil.IsEmpty(PackCount))
		{
			result = 8;
			resultDesc = "入参不合法";
			return false;
		}
		// 参数合法性检查
//		if (StringUtil.IsEmpty(QueryNum) || (1 != QueryType && 2 != QueryType  && 3 != QueryType)
//				|| StringUtil.IsEmpty(StreamingNum))
//		{
//			result = 8;
//			return false;
//		}
		return true;
	}

	@Override
	public String getReturnXml()
	{
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("resultCode", result);
			jo.put("streamingNum", StreamingNum);
			jo.put("sourceIP", "");
			jo.put("resultList", "");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}

	public String getDestIp()
	{
		return DestIp;
	}

	public void setDestIp(String destIp)
	{
		DestIp = destIp;
	}

	public String getTimeOut()
	{
		return TimeOut;
	}

	public void setTimeOut(String timeOut)
	{
		TimeOut = timeOut;
	}

	public String getPackSize()
	{
		return PackSize;
	}

	public void setPackSize(String packSize)
	{
		PackSize = packSize;
	}

	public String getPackCount()
	{
		return PackCount;
	}

	public void setPackCount(String packCount)
	{
		PackCount = packCount;
	}

	public String getUserIndex()
	{
		return userIndex;
	}

	public void setUserIndex(String userIndex)
	{
		this.userIndex = userIndex;
	}

	
	public String getWanPassageWay()
	{
		return wanPassageWay;
	}

	
	public void setWanPassageWay(String wanPassageWay)
	{
		this.wanPassageWay = wanPassageWay;
	}
	
}
