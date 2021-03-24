
package com.linkage.itms.hlj.dispatch.obj;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class DownLoadByHTTPChecker extends HljBaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(DownLoadByHTTPChecker.class);
	private String inParam = null;
	/**
	 * Wan通道
	 */
	private String wanPassageWay = null;
	/**
	 * 
	 */
	private String downURL = null;
 

	/**
	 * 构造方法
	 * 
	 * @param inParam
	 *            接口调用入参，xml字符串
	 */
	public DownLoadByHTTPChecker(String inParam)
	{
		this.inParam = inParam;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		try
		{
			JSONObject jo = new JSONObject(inParam);
			String JsonStr = jo.getString("input");
			infcode = jo.getString("infcode");
			JSONObject jo2 = new JSONObject(JsonStr);
			QueryNum = jo2.getString("QueryNum");
			QueryType = Integer.parseInt(jo2.getString("QueryType"));
			StreamingNum = jo2.getString("StreamingNum");
			wanPassageWay = jo2.getString("wanPassageWay");
			downURL = jo2.getString("url");
			logger.warn("获取infcode为[{}]", infcode);
		}
		catch (Exception e)
		{
			result = 8;
			logger.warn("获取参数错误");
			e.printStackTrace();
			return false;
		}
		if (null == infcode || (Integer.parseInt(infcode) != 40 && Integer.parseInt(infcode) != 42)){
			result = 8;
			resultDesc = "接口代码与方法名不一致";
			return false;
		} 
		// 参数合法性检查
		if (StringUtil.IsEmpty(QueryNum) || (1 != QueryType && 0 != QueryType && 2 != QueryType)
				|| StringUtil.IsEmpty(StreamingNum))
		{
			result = 8;
			resultDesc = "参数合法性检查未通过";
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}
	
	@Override
	public String getReturnXml()
	{
		logger.debug("DownLoadByHTTPChecker————getReturnXml()");

		JSONObject jo = new JSONObject();
		try
		{
			jo.put("resultCode", result);
			jo.put("resultMessage", resultDesc);
			jo.put("SpeedResult", "");
			jo.put("streamingNum", StreamingNum);
			jo.put("startTime", "");
			jo.put("endTime", "");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	
	}

	public String getDownURL()
	{
		return downURL;
	}

	public String getWanPassageWay()
	{
		return wanPassageWay;
	}

	public void setWanPassageWay(String wanPassageWay)
	{
		this.wanPassageWay = wanPassageWay;
	}

	public void setDownURL(String downURL)
	{
		this.downURL = downURL;
	}

	
}
