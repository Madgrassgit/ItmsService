package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class PingResult implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//成功次数
	private int success_count;
	//失败次数
    private int failure_count;
    //平均响应时间
    private int average_responsetime;
    //最小响应时间
    private int minimum_responsetime;
    //最大响应时间
    private int maximum_responsetime;
    
    @Override
	public String toString() {
		return "Order [success_count=" + success_count + ", failure_count=" + failure_count
				+ ", average_responsetime=" + average_responsetime+ ", minimum_responsetime=" 
				+ minimum_responsetime+ ", maximum_responsetime=" + maximum_responsetime + "]";
	}

	
	public int getSuccess_count()
	{
		return success_count;
	}

	
	public void setSuccess_count(int success_count)
	{
		this.success_count = success_count;
	}

	
	public int getFailure_count()
	{
		return failure_count;
	}

	
	public void setFailure_count(int failure_count)
	{
		this.failure_count = failure_count;
	}

	
	public int getAverage_responsetime()
	{
		return average_responsetime;
	}

	
	public void setAverage_responsetime(int average_responsetime)
	{
		this.average_responsetime = average_responsetime;
	}

	
	public int getMinimum_responsetime()
	{
		return minimum_responsetime;
	}

	
	public void setMinimum_responsetime(int minimum_responsetime)
	{
		this.minimum_responsetime = minimum_responsetime;
	}

	
	public int getMaximum_responsetime()
	{
		return maximum_responsetime;
	}

	
	public void setMaximum_responsetime(int maximum_responsetime)
	{
		this.maximum_responsetime = maximum_responsetime;
	}

	
}