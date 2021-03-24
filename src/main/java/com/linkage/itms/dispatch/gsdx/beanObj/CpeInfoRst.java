
package com.linkage.itms.dispatch.gsdx.beanObj;

import java.util.Arrays;

public class CpeInfoRst
{

	private int iOpRst;
	private CpeInfo CpeInfo;
	// AHLT getCpeBasicInfo接口回参
	private int errorCode;
	private String errorInfo;
	private Para[] basicInfoList;

	public int getiOpRst()
	{
		return iOpRst;
	}

	public void setiOpRst(int iOpRst)
	{
		this.iOpRst = iOpRst;
	}

	public CpeInfo getCpeInfo()
	{
		return CpeInfo;
	}

	public void setCpeInfo(CpeInfo cpeInfo)
	{
		CpeInfo = cpeInfo;
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(int errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getErrorInfo()
	{
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo)
	{
		this.errorInfo = errorInfo;
	}

	public Para[] getBasicInfoList()
	{
		return basicInfoList;
	}

	public void setBasicInfoList(Para[] basicInfoList)
	{
		this.basicInfoList = basicInfoList;
	}

	@Override
	public String toString()
	{
		return "CpeInfoRst{" + "iOpRst=" + iOpRst + ", CpeInfo=" + CpeInfo
				+ ", errorCode=" + errorCode + ", errorInfo='" + errorInfo + '\''
				+ ", basicInfoList=" + Arrays.toString(basicInfoList) + '}';
	}
}
