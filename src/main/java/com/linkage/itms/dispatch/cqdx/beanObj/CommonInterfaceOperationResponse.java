
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class CommonInterfaceOperationResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//返回结果xml
	private String response_xml;
    
    @Override
	public String toString() {
		return "CommonInterfaceOperationResponse [response_xml=" + response_xml + "]";
	}

	
	public String getResponse_xml()
	{
		return response_xml;
	}

	
	public void setResponse_xml(String response_xml)
	{
		this.response_xml = response_xml;
	}
    
}