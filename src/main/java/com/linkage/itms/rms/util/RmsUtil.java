package com.linkage.itms.rms.util;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import com.linkage.itms.rms.obj.JsonEntity;

/**
 * 
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2015年3月19日
 * @category com.linkage.itms.rms
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class RmsUtil
{
	/**
	 * 生成cmdId,目前算法是当前毫秒数.
	 * @return cmdId
	 */
	public static String genCmdId(){
		Calendar calendar = Calendar.getInstance(); 
		return String.valueOf(calendar.getTimeInMillis());
	}
	
	/**
	 * 写出json.
	 * @param entity 需要序列化成json的实体
	 * @param response 响应
	 * @throws IOException
	 */
	public static void writeJson(JsonEntity entity, HttpServletResponse response) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
		jsonGenerator.writeObject(entity);
		jsonGenerator.close();
	}
}
