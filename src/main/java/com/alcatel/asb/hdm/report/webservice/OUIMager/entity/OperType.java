package com.alcatel.asb.hdm.report.webservice.OUIMager.entity;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年6月25日
 * @category com.alcatel.asb.hdm.report.webservice.OUIMager.entity
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public enum OperType
{
	
	OPERATOR_ADD("1"),
	OPERATOR_DEL("2"),
	OPERATOR_QUERY("3");
	
	private String index;
	OperType(String index){
		this.index = index;
	}
	public String getIndex()
	{
		return index;
	}
}
