
package com.linkage.itms.hlj.dispatch.obj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * 接口数据检查基类(抽象类)
 * 
 * @author Jason(3412)
 * @date 2010-6-17
 */
public abstract class HljBaseChecker
{

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(HljBaseChecker.class);
	// 查询结果
	protected int result;
	// 查询结果描述
	protected String resultDesc;
	// 新疆FTTH用户逻辑标识
	protected String loid;
	protected String QueryNum;
	protected int QueryType;
	protected String StreamingNum;
	protected String infcode;
	protected String resultCode;

	/**
	 * 检查客户端的XML字符串是否合法，如果合法将字符串转换成对象的属性
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return boolean
	 */
	public abstract boolean check();

	/**
	 * 返回调用结果
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-4-1
	 * @return String
	 */
	public abstract String getReturnXml();

	/**
	 * @author 岩
	 * @date 2016-8-3
	 * @return
	 */
	protected boolean queryNumCheck()
	{
		if (StringUtil.IsEmpty(QueryNum))
		{
			result = 101;
			resultDesc = "查询号码为空";
			return false;
		}
		return true;
	}

	/**
	 * @author 岩
	 * @date 2016-8-3
	 * @return
	 */
	boolean queryTypeCheck()
	{
		if (1 != QueryType && 2 != QueryType && 0 != QueryType && 3 != QueryType)
		{
			result = 102;
			resultDesc = "查询号码类别非法";
			return false;
		}
		return true;
	}

	protected boolean streamingNumCheck()
	{
		if (StringUtil.IsEmpty(StreamingNum))
		{
			result = 103;
			resultDesc = "流水号为空";
			return false;
		}
		return true;
	}

	public int getResult()
	{
		return result;
	}

	public void setResult(int result)
	{
		this.result = result;
	}

	public String getResultDesc()
	{
		return resultDesc;
	}

	public void setResultDesc(String resultDesc)
	{
		this.resultDesc = resultDesc;
	}

	public String getLoid()
	{
		return loid;
	}

	public void setLoid(String loid)
	{
		this.loid = loid;
	}

	public String getQueryNum()
	{
		return QueryNum;
	}

	public void setQueryNum(String queryNum)
	{
		QueryNum = queryNum;
	}

	public int getQueryType()
	{
		return QueryType;
	}

	public void setQueryType(int queryType)
	{
		QueryType = queryType;
	}

	public String getStreamingNum()
	{
		return StreamingNum;
	}

	public void setStreamingNum(String streamingNum)
	{
		StreamingNum = streamingNum;
	}

	public String getInfcode()
	{
		return infcode;
	}

	public void setInfcode(String infcode)
	{
		this.infcode = infcode;
	}

	public String getResultCode()
	{
		return resultCode;
	}

	public void setResultCode(String resultCode)
	{
		this.resultCode = resultCode;
	}
}
