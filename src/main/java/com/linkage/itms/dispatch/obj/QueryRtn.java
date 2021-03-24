package com.linkage.itms.dispatch.obj;


public class QueryRtn
{
	String rstCode = "1";
	String rstDesc = "";
	QueryRtnMsg rstMsg = new QueryRtnMsg();
	
	public String getRstCode()
	{
		return rstCode;
	}
	
	public void setRstCode(String rstCode)
	{
		this.rstCode = rstCode;
	}
	
	public QueryRtnMsg getRstMsg()
	{
		return rstMsg;
	}
	
	public void setRstMsg(QueryRtnMsg rstMsg)
	{
		this.rstMsg = rstMsg;
	}

	
	public String getRstDesc()
	{
		return rstDesc;
	}

	
	public void setRstDesc(String rstDesc)
	{
		this.rstDesc = rstDesc;
	}
	
	
}
