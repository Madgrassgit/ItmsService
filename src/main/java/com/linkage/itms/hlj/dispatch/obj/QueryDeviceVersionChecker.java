package com.linkage.itms.hlj.dispatch.obj;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 终端版本查询
 * @author 岩 (Ailk No.)
 * @version 1.0
 * @since 2016-8-4
 * @category com.linkage.itms.hlj.dispatch.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class QueryDeviceVersionChecker extends HljBaseChecker {
	
	private static final Logger logger = LoggerFactory
			.getLogger(QueryDeviceVersionChecker.class);
	 
	private String inParam = null;

	public QueryDeviceVersionChecker(String inParam)
	{
		this.inParam = inParam;
	}
	
	/**
	 * 入参验证
	 */
	public boolean check(){

		logger.debug("ServiceDoneFailChecker==>check()"+inParam);
		
		try
		{
			JSONObject jo = new JSONObject(inParam);
			String JsonStr = jo.getString("input");
			infcode = jo.getString("infcode");
			
			JSONObject jo2 = new JSONObject(JsonStr);
			QueryNum = jo2.getString("QueryNum");
			QueryType = Integer.parseInt(jo2.getString("QueryType"));
			StreamingNum = jo2.getString("StreamingNum");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!"5".equals(infcode)){
			result = 104;
			resultDesc = "接口代码与方法名不一致";
			return false;
		}
		if (2 ==QueryType ){
			if(QueryNum.length()<6){
				result = 105;
				resultDesc = "SN至少输入6位数";
				return false;
			}
		}
		// 参数合法性检查
		if (false == queryNumCheck() || false == queryTypeCheck()
				|| false == streamingNumCheck())
		{
			return false;
		}
		return true;
	
	}
	
	public String getReturnXml(){
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("result", "");
			jo.put("resultDesc", resultDesc);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return jo.toString();
	}

	
	public String getInParam()
	{
		return inParam;
	}

	
	public void setInParam(String inParam)
	{
		this.inParam = inParam;
	}
	
 
}
