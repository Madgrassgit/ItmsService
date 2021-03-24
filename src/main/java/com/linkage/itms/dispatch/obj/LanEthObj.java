package com.linkage.itms.dispatch.obj;

import com.linkage.itms.cao.IParamTree;


/**
 * @author Jason(3412)
 * @date 2009-7-10
 */
public class LanEthObj {

	private int lanid;
	
	private int lanEthid;
	
	private String status;
	
	private String enable;
	
	private String mac;
	
	private long gatherTime;

	
	/**
	 * 获取LAN.ETH结点字符串描述
	 * 
	 * @param 
	 * @author Jason(3412)
	 * @date 2009-7-10
	 * @return String
	 */
	public String getLanInterface(){
		StringBuffer inter = new StringBuffer();
		inter.append(IParamTree.LANDEVICE);
		inter.append("." + lanid + ".");
		inter.append(IParamTree.ETHERNET);
		inter.append("." + lanEthid);
		
		return inter.toString();
	}
	
	
	/** getter, setter method fields**/
	
	public int getLanid() {
		return lanid;
	}

	public void setLanid(int lanid) {
		this.lanid = lanid;
	}

	public int getLanEthid() {
		return lanEthid;
	}

	public void setLanEthid(int lanEthid) {
		this.lanEthid = lanEthid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public long getGatherTime() {
		return gatherTime;
	}

	public void setGatherTime(long gatherTime) {
		this.gatherTime = gatherTime;
	}

}
