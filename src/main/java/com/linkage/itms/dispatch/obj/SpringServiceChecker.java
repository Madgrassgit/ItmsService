package com.linkage.itms.dispatch.obj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringServiceChecker extends BaseChecker{

	private static Logger logger = LoggerFactory.getLogger(SpringServiceChecker.class);
	
	private String sysName ;
	
	private String fileName;
	
	private String filePath;
	
	

	public SpringServiceChecker()
	{
	}
	public SpringServiceChecker(String inXml)
	{
		callXml = inXml;
	}

	@Override
	public boolean check()
	{
		logger.debug("check()");
		try
		{
			sysName = this.parseXml("<sysName>", "</sysName>", callXml);
			
			fileName = this.parseXml("<filename>", "</filename>", callXml);
			
			filePath = this.parseXml("<filepath>", "</filepath>", callXml);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public  String getReturnXml()
	{
		logger.debug("getReturnXml()");
		// 本接口无需返回结果
		return null;
	}
	
	
	/**
	 * 智能网管的在没有单一根节点的情况下调用，，真实奇葩
	 * @param fieldStart  
	 * @param filedEnd
	 * @param inxml
	 * @return
	 */
   private String parseXml(String fieldStart, String filedEnd, String inxml)
	{
		String regex = fieldStart + "([\\s\\S]*)" + filedEnd;
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(inxml);
		while (m.find())
		{
			return (m.group(1));
		}
		return null;
	}
	public String getSysName() {
		return sysName;
	}
	public void setSysName(String sysName) {
		this.sysName = sysName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
