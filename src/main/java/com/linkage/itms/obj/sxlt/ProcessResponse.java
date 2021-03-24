package com.linkage.itms.obj.sxlt;

import java.io.Serializable;

public class ProcessResponse implements Serializable
{
	private static final long serialVersionUID = 6349981392888422002L;
	private String processResponse;
    
	@Override
	public String toString()
	{
		return "StartRebootDiagResponse [processResponse=" + processResponse + "]";
	}


	public String getProcessResponse()
	{
		return processResponse;
	}

	
	public void setProcessResponse(String processResponse)
	{
		this.processResponse = processResponse;
	}
}
