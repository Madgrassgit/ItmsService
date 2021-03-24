package com.alcatel.asb.hdm.report.webservice.OUIMager.entity;

import java.io.Serializable;


/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年6月25日
 * @category com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class Response implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3684164751975742552L;
	
	private int result_code;
	private String result_desc;
	private OUIRes oui_info;
	
	public Response(){
		
	}
	public int getResult_code()
	{
		return result_code;
	}

	
	public void setResult_code(int result_code)
	{
		this.result_code = result_code;
	}

	public Response(int code, String desc){
		this.result_code = code;
		this.result_desc = desc;
	}
	
	public String getResult_desc()
	{
		return result_desc;
	}
	
	public void setResult_desc(String result_desc)
	{
		this.result_desc = result_desc;
	}
	
	public OUIRes getOui_info()
	{
		return oui_info;
	}
	
	public void setOui_info(OUIRes oui_info)
	{
		this.oui_info = oui_info;
	}

	@Override
	public String toString()
	{
		return "Response [result_code=" + result_code + ", result_desc=" + result_desc
				+ ", oui_info=" + oui_info + "]";
	}
	
}
