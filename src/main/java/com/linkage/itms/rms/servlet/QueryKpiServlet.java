package com.linkage.itms.rms.servlet;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.DateTimeUtil;
import com.linkage.itms.dao.CityDAO;
import com.linkage.itms.rms.dao.QueryKpiDao;
import com.linkage.itms.rms.obj.AllKpi;
import com.linkage.itms.rms.obj.City;
import com.linkage.itms.rms.obj.JsonEntity;
import com.linkage.itms.rms.util.RmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
		
public class QueryKpiServlet extends HttpServlet 
{
	public static final Logger logger = LoggerFactory.getLogger(QueryKpiServlet.class);
	private  DateTimeUtil dateTimeUtil=new DateTimeUtil();
	private  QueryKpiDao queryKpiDao=new QueryKpiDao();
	private static Map<String,String> cityIdCityNameMap=CityDAO.getCityIdCityNameMap();
	 //省的id为00
	private static ArrayList<String> secondCityList=CityDAO.getNextCityIdsByCityPidCore("00");
	private String totalQuantity="",lastQuantity="";
	//数据的开始时间和结束时间
	private String st="",et="";
	private City max=new City();
	private City min=new City();
	private City rate=new City();
	private List<City> cityList=null;
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
	   logger.warn("QueryKpiServlet->doPost");
	   String timeType= request.getParameter("timeType");
	   String time=request.getParameter("time");
	   String businessType=request.getParameter("businessType");
	   long start,medium,end;
	   JsonEntity entity = new JsonEntity();
	   if((!"1".equals(timeType))&&(!"2".equals(timeType))&&(!"3".equals(timeType))){
			entity.setCode("0");
			entity.setDetail("时间类型不存在");
			RmsUtil.writeJson(entity, response);
			return;
	   }
	   if(!this.isValidDate(time)){
		    entity.setCode("0");
			entity.setDetail("时间格式不合法");
			RmsUtil.writeJson(entity, response);
			return;
	   }
	   //日
	   if(Global.DAY.equals(timeType)){
		 //当天的0点即上一天的24点
		   medium=dateTimeUtil.getLastDayEnd(time);
		   //上一天的0点
		   start=medium-24*3600;
		   //下一天的0点即当天的24点
		   end=medium+24*3600;
		   st=time+" 00";
		   et=time+" 24";
		   this.getAllKpi(businessType, start,medium,end);
	   }
	   //周
	   else if(Global.WEEK.equals(timeType)){
		   st=dateTimeUtil.getFirstDayOfWeek("CN", time);
		   //这周一的0点即上周日的24点
		   medium=dateTimeUtil.getLastDayEnd(st);
		   //上周一的0点
		   start=medium-7*24*3600;
		   //下周一0点即这周周日的24点
		   end=medium+7*24*3600;
		   //减1秒获取周日的日期
		   et=dateTimeUtil.getDate(dateTimeUtil.calendarCreate(end-1));
		   this.getAllKpi(businessType, start,medium,end);
	   }
	   //月
	   else {
		   st=dateTimeUtil.getFirstDayOfMonth(time);
		   //这个月1号的0点即上个月最后一天的24点
		   medium=dateTimeUtil.getLastDayEnd(st);
		  //上个月1号的0点
		   start=dateTimeUtil.getLastDayEnd(dateTimeUtil.getFirstDayOfLastMonth(time));
		   //下个月1号的0点即这个月最后一天的24点
		   end=dateTimeUtil.getLastDayEnd(dateTimeUtil.getFirstDayOfNextMonth(time));
		   et=dateTimeUtil.getDate(dateTimeUtil.calendarCreate(end-1));
		   this.getAllKpi(businessType, start,medium,end);
	   }
	       // 书写json对象实体
			entity.setCode("1");
			entity.setDetail("查询成功");
			AllKpi value=new AllKpi();
			value.setMax(max);
			value.setMin(min);
			value.setRate(rate);
			value.setTotal(totalQuantity);
			value.setList(cityList);
			value.setSt(st);
			value.setEt(et);
			entity.setValue(value);
			// 写出json
			RmsUtil.writeJson(entity, response);
	}
	/**
	 * 手机端已默认传当前时间的前一天,上一周的第一天，上一月的第一天
	 */
	private void getAllKpi(String businessType,long start,long medium,long end){
		  logger.warn("进入到getAllKpi方法, 参数start is: {},medium is: {},end is: {}",new Object[]{start,medium,end});
		   //求截止到当天,或上周最后一天,或上月最后一天24点已经开通所有用户总数
		   totalQuantity= StringUtil.getStringValue(queryKpiDao.getTotalUserQuantity(businessType,0,end), "quantity","0");
		   //求截止开通最大用户数城市
		   List<HashMap<String,String>> maxCitylist=queryKpiDao.getMaxUserQuantity(businessType,0,end);
		   if(maxCitylist!=null&&maxCitylist.size()>0
			  &&StringUtil.getStringValue(maxCitylist.get(0),"city_id")!=null
			  &&StringUtil.getStringValue(maxCitylist.get(0),"quantity")!=null){
			       max.setK(StringUtil.getStringValue(cityIdCityNameMap,StringUtil.getStringValue(maxCitylist.get(0),"city_id")).substring(0, 2));
			       max.setV(StringUtil.getStringValue(maxCitylist.get(0),"quantity"));
		   }
		   //求截止开通最小用户数城市
		   List<HashMap<String,String>> minCitylist=queryKpiDao.getMinUserQuantity(businessType,0,end);
		   if(minCitylist!=null&&minCitylist.size()>0
			  &&StringUtil.getStringValue(minCitylist.get(0),"city_id")!=null
			  &&StringUtil.getStringValue(minCitylist.get(0),"quantity")!=null){
			     min.setK(StringUtil.getStringValue(cityIdCityNameMap,StringUtil.getStringValue(minCitylist.get(0),"city_id")).substring(0, 2));
			     min.setV(StringUtil.getStringValue(minCitylist.get(0),"quantity"));
		   }
		   //求截止昨天,或上上周最后一天,或上上月最后一天开通用户的总数
		   lastQuantity= StringUtil.getStringValue(queryKpiDao.getTotalUserQuantity(businessType,0,medium), "quantity","0");
		   //求环比
		   rate.setK("环比");
		   rate.setV(this.getRate(totalQuantity, lastQuantity));
		   //求视图
		   //当前天，周，月的各个城市数据
		   List<HashMap<String,String>> currentCityList=queryKpiDao.getDayUserCountList(businessType, medium, end);
		   //前一天,一周，一月各个城市数据
		   List<HashMap<String,String>> lastCityList=queryKpiDao.getDayUserCountList(businessType, start, medium);
		   //创建视图所用list
		   Map<String,City> map=new HashMap<String,City>();
			   for(String cityId:secondCityList){
				   City city=new City();
				   city.setK(StringUtil.getStringValue(cityIdCityNameMap,cityId).substring(0, 2));
				   map.put(cityId, city);
			   }
		   //设置数量
		   if(currentCityList!=null){
			   for(HashMap<String,String> cityMap:currentCityList){
				   map.get(StringUtil.getStringValue(cityMap,"city_id")).setV(cityMap.get("census_count"));
			   } 
		   }
		   //设置环比
		   if(lastCityList!=null){
			   for(HashMap<String,String> cityMap:lastCityList){
				   map.get(StringUtil.getStringValue(cityMap,"city_id")).setR(this.getRate(map.get(StringUtil.getStringValue(cityMap,"city_id")).getV(), StringUtil.getStringValue(cityMap,"census_count")));
			   }
		   }
		  //转化为List同时排序
		   cityList=this.sortByQuantity(new ArrayList<City>(map.values()));
	}
	/**
	        * @description 计算环比，按照日，周，月输出
			* @param lastQuantity
			* @param moreLastQuantity
			* @param date
			* @return
	 */
	private String getRate(String lastQuantity,String moreLastQuantity){
		String rateV="-";
		if(Float.parseFloat(moreLastQuantity)>0){
			   BigDecimal  b = BigDecimal.valueOf((Float.parseFloat(lastQuantity)-Float.parseFloat(moreLastQuantity))/Float.parseFloat(moreLastQuantity)*100);
			   //小数保留3位
			   float rate=b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
				  if(rate>0){
					  rateV="+"+rate+"%";
				  }else{
					  rateV=rate+"%";
				  }
		   }
		return rateV;
	}
	/**
	 * @description 
	 * @author cczhong
	 */
	private List<City> sortByQuantity(List<City> list){
		//经典选择排序
		for (int i=0;i<list.size()-1 ;i++ ){
			    int min=i;
		for (int j=i+1;j<list.size() ;j++ ){
			if (StringUtil.getLongValue(list.get(min).getV())<StringUtil.getLongValue(list.get(j).getV())){
			           min=j;
			   }
			 }
	     if (min!=i){
	    	 //最大的和相应的替换
	    	 String cityName=list.get(i).getK();
	    	 String quantity=list.get(i).getV();
	    	 String rate=list.get(i).getR();
	    	 list.get(i).setK(list.get(min).getK());
	    	 list.get(i).setV(list.get(min).getV());
	    	 list.get(i).setR(list.get(min).getR());
	    	 list.get(min).setK(cityName);
	    	 list.get(min).setV(quantity);
	    	 list.get(min).setR(rate);
	       }
		}
		return list;
	}
	/**
	 *      日期格式判断
			* @param dateStr
			* @return
	 */
   private  boolean isValidDate(String dateStr) {
       boolean convertSuccess=true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        try {
           format.setLenient(true);
           format.parse(dateStr);
        } catch (Exception e) {
            convertSuccess=false;
        } 
        return convertSuccess;
 }
}

	