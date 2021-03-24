package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;

/**
 * 终端PING接口(启动PING测试)
 * @author fanjm (AILK No.35572)
 * @version 1.0
 * @since 2019-6-21
 * @category com.linkage.itms.dispatch.gsdx.beanObj
 * @copyright AILK NBS-Network Mgt. RD Dept.
 */

public class pingInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4666688899624752187L;
	
	//目的IP
    private String ip;
    //包大小
    private int size;
    //次数
    private int num;

    private int overtime;

	private String Interface ;

	public String getInterface() {
		return Interface;
	}

	public void setInterface(String anInterface) {
		Interface = anInterface;
	}


	public String getIp()
	{
		return ip;
	}

	
	public void setIp(String ip)
	{
		this.ip = ip;
	}

	
	public int getSize()
	{
		return size;
	}

	
	public void setSize(int size)
	{
		this.size = size;
	}

	
	public int getNum()
	{
		return num;
	}

	
	public void setNum(int num)
	{
		this.num = num;
	}

	
	public int getOvertime()
	{
		return overtime;
	}

	
	public void setOvertime(int overtime)
	{
		this.overtime = overtime;
	}




	@Override
	public String toString()
	{
		return "pingInfo [ip=" + ip + ", size=" + size + ", num=" + num + ", overtime="
				+ overtime + ", Interface=" + Interface + "]";
	}
    
    
	
}
