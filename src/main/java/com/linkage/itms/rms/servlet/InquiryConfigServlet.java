package com.linkage.itms.rms.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.rms.dao.InquiryConfigDao;
import com.linkage.itms.rms.obj.Config;
import com.linkage.itms.rms.obj.JsonEntity;
import com.linkage.itms.rms.util.RmsUtil;
		
public class InquiryConfigServlet extends HttpServlet
{
	public static final Logger logger = LoggerFactory.getLogger(InquiryConfigServlet.class);
	private InquiryConfigDao inquiryConfigDao=new InquiryConfigDao();
	private CityDAO cityDao=new CityDAO();
	/**
	 * doGet
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
		}
	/**
	 * doPost
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 logger.warn("InquiryConfigServlet->doPost");
		//账号信息类型
		int userInfoType = Integer.parseInt(request.getParameter("userInfoType"));
		//账号信息
		String userInfo = request.getParameter("userInfo");
		//结果json
		JsonEntity entity = new JsonEntity();
		List<Config> configList=new ArrayList<Config>();
		Map<String,String> cityIdCityNameMap=cityDao.getCityIdCityNameMap();
		List<HashMap<String,String>> resultList=inquiryConfigDao.inquiryConfig(userInfoType,userInfo);
		if(resultList!=null&&resultList.size()>0){
			for(HashMap<String,String> columnMap:resultList){
					Config config=new Config();
				    config.setCityName(StringUtil.getStringValue(cityIdCityNameMap,StringUtil.getStringValue(columnMap,"city_id")));
				    config.setDeviceSerialNumber(StringUtil.getStringValue(columnMap,"device_serialnumber"));
				    configList.add(config);
			  }
			// 书写json对象实体
			entity.setCode("0");
			entity.setDetail("查询成功");
			entity.setValue(configList);
			}else{
				// 书写json对象实体
				entity.setCode("2");
				entity.setDetail("结果为空");
			}
		
				
		// 写出json
		RmsUtil.writeJson(entity, response);
	}
}

	