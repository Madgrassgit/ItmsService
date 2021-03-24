
package com.linkage.itms.dispatch.obj;

public class ConfigRtn
{

	String rstCode = "1";
	String rstDesc = "";
	String failedNode = "";

	public String getFailedNode()
	{
		return failedNode;
	}

	public void setFailedNode(String failedNode)
	{
		this.failedNode = failedNode;
	}

	public String getRstCode()
	{
		return rstCode;
	}

	public void setRstCode(String rstCode)
	{
		this.rstCode = rstCode;
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
