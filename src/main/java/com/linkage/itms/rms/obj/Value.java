package com.linkage.itms.rms.obj;

import java.util.List;
		
public class Value
{
	private String total;
	private City max;
	private City min;
	private City rate;
	private List<City> list;
	
	public String getTotal()
	{
		return total;
	}
	
	public void setTotal(String total)
	{
		this.total = total;
	}
	
	public City getMax()
	{
		return max;
	}
	
	public void setMax(City max)
	{
		this.max = max;
	}
	
	public City getMin()
	{
		return min;
	}
	
	public void setMin(City min)
	{
		this.min = min;
	}
	
	public City getRate()
	{
		return rate;
	}
	
	public void setRate(City rate)
	{
		this.rate = rate;
	}
	
	public List<City> getList()
	{
		return list;
	}
	
	public void setList(List<City> list)
	{
		this.list = list;
	}
	
}

	