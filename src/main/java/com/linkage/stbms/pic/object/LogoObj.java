package com.linkage.stbms.pic.object;

public class LogoObj {

	/**
	 * 机顶盒启动图片的下载地址
	 */
	private String startPicURL = null;
	/**
	 * 机顶盒开机图片的下载地址
	 */
	private String bootPicURL = null;
	/**
	 * 机顶盒认证图片的下载地址
	 */
	private String authenticatePicURL = null;
	
	private int isSetStartPicURL = 0;
	private int isSetBootPicURL = 0;
	private int isSetAuthenticatePicURL = 0;

	
	public String getStartPicURL()
	{
		return startPicURL;
	}

	
	public void setStartPicURL(String startPicURL)
	{
		this.startPicURL = startPicURL;
	}

	
	public String getBootPicURL()
	{
		return bootPicURL;
	}

	
	public void setBootPicURL(String bootPicURL)
	{
		this.bootPicURL = bootPicURL;
	}

	
	public String getAuthenticatePicURL()
	{
		return authenticatePicURL;
	}

	
	public void setAuthenticatePicURL(String authenticatePicURL)
	{
		this.authenticatePicURL = authenticatePicURL;
	}


	
	public int getIsSetStartPicURL()
	{
		return isSetStartPicURL;
	}


	
	public void setIsSetStartPicURL(int isSetStartPicURL)
	{
		this.isSetStartPicURL = isSetStartPicURL;
	}


	
	public int getIsSetBootPicURL()
	{
		return isSetBootPicURL;
	}


	
	public void setIsSetBootPicURL(int isSetBootPicURL)
	{
		this.isSetBootPicURL = isSetBootPicURL;
	}


	
	public int getIsSetAuthenticatePicURL()
	{
		return isSetAuthenticatePicURL;
	}


	
	public void setIsSetAuthenticatePicURL(int isSetAuthenticatePicURL)
	{
		this.isSetAuthenticatePicURL = isSetAuthenticatePicURL;
	}
	
}
