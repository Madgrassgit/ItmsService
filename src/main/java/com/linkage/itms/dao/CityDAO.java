package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.Global;

/**
 * 属地相关数据库操作类
 * 
 * @author Jason(3412)
 * @date 2010-6-21
 */
public class CityDAO {

	private static Logger m_logger = LoggerFactory.getLogger(CityDAO.class);

	/**
	 * 获取所有的属地ID(city_id)
	 * 该方法对外开放
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return ArrayList 所有的属地city_id
	 */
	public static ArrayList<String> getAllCityIdList(){
		
		m_logger.debug("getAllCityIdList()");
		
		return Global.G_CityIds;
	}
	
	/**
	 * 获取所有属地的G_CityId_CityName_Map
	 * 属地ID、属地名Map<city_id,city_name>
	 * 
	 * 该方法对外开放
	 * 
	 * @param 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * @return Map 返回全部的city_id与city_name的映射Map
	 */
	public static Map<String,String> getCityIdCityNameMap(){
		
		m_logger.debug("getCityIdCityNameMap()");
		
		return Global.G_CityId_CityName_Map;
	}
	
	/**
	 * 获取所有子属地与父属地的对应
	 * 获取所有属地的ConcurrentHashMap<ciry_id,parent_id>
	 * 
	 * 该方法对外开放
	 * 
	 * @param 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * @return Map 返回所有的的city_id,parent_id的映射Map
	 */
	public static Map<String,String> getCityIdPidMap(){
		
		m_logger.debug("getCityIdPidMap()");
		return Global.G_City_Pcity_Map;
	}
	
	/**
	 * 根据当前属地查询该属地的所有上级属地，直至省中心（包括自己）
	 * 
	 * 此方法对外提供
	 * 
	 * @param cityId
	 * @return
	 */
	public static List<String> getAllPcityIdByCityId(String cityId){
		
		m_logger.debug("getAllPCityIdByCityId(cityId:{})",cityId);
		
		if(null==cityId){
			return null;
		}
		
		List<String> list = new ArrayList<String>();
		list.add(cityId);
		
		if("-1".equals(cityId) || "00".equals(cityId)){
			return list;
		}
		
		String tempCityId = Global.G_City_Pcity_Map.get(cityId);
		
		do{
			list.add(tempCityId);
			tempCityId = Global.G_City_Pcity_Map.get(tempCityId);
		}while(!"00".equals(tempCityId) && null!=tempCityId);
		
		return list;
	}
	
	/**
	 * 查询该属地的本地网属地（如果是省中心则返回省中心）
	 * 
	 * 此方法对外提供
	 * 
	 * @param cityId
	 * @return
	 */
	public static String getLocationCityIdByCityId(String cityId){
		
		m_logger.debug("getLocationCityIdByCityId(cityId:{})",cityId);
		
		if(null==cityId){
			return null;
		}
		
		String rsCityId = cityId;
		
		if("-1".equals(cityId) || "00".equals(cityId)){
			rsCityId = "00";
		}else{
			while(!"00".equals(Global.G_City_Pcity_Map.get(rsCityId)) && null!=(Global.G_City_Pcity_Map.get(rsCityId)) && !"-1".equals(Global.G_City_Pcity_Map.get(rsCityId))){
				rsCityId = Global.G_City_Pcity_Map.get(rsCityId);
			}
		}
		
		return rsCityId;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID(包含自己)
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return ArrayList 待查询属地的下一级子属地(包含自己),
	 */
	public static ArrayList<String> getNextCityIdsByCityPid(String m_CityPid) {
		
		m_logger.debug("getNextCityIdsByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		ArrayList<String> list = getNextCityIdsByCityPidCore(m_CityPid);
		list.add(m_CityPid);
		
		return list;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID(不包含自己)
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return ArrayList 待查询属地的下一级子属地(不包含自己),
	 */
	public static ArrayList<String> getNextCityIdsByCityPidCore(String m_CityPid) {
		
		m_logger.debug("getNextCityIdsByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		ArrayList<String> list = new ArrayList<String>();
		
		Iterator it = Global.G_City_Pcity_Map.keySet().iterator();
		
		while(it.hasNext()) {
			
			String key = (String) it.next();
			if(Global.G_City_Pcity_Map.get(key).equals(m_CityPid)){
				list.add(key);
			}
		}
		
		return list;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID与NAME的对应关系(包含自己)
	 * 返回格式为Map<String,String>
	 * 例如：<00,省中心>
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return Map<String,String> 待查询属地ID获取下一层属地ID与NAME的对应关系(包含自己)
	 */
	public static Map<String,String> getNextCityMapByCityPid(String m_CityPid) {
		
		m_logger.debug("getNextCityIdsByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		Map<String,String> map =  getNextCityMapByCityPidCore(m_CityPid);
		
		map.put(m_CityPid, Global.G_CityId_CityName_Map.get(m_CityPid));
		
		return map;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID与NAME的对应关系(不包含自己)
	 * 返回格式为Map<String,String>
	 * 例如：<00,省中心>
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return Map<String,String> 待查询属地ID获取下一层属地ID与NAME的对应关系(不包含自己)
	 */
	public static Map<String,String> getNextCityMapByCityPidCore(String m_CityPid) {
		
		m_logger.debug("getNextCityIdsByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		Map<String,String> map =  new ConcurrentHashMap<String,String>();
		
		Iterator it = Global.G_City_Pcity_Map.keySet().iterator();
		
		while(it.hasNext()) {
			
			String key = (String) it.next();
			if(Global.G_City_Pcity_Map.get(key).equals(m_CityPid)){
				map.put(key, Global.G_CityId_CityName_Map.get(key));
			}
		}
		
		return map;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID与NAME的对应关系(包含自己)
	 * 返回格式为List<Map<String,String>>
	 * 例如：<<city_id,00>,<city_name,省中心>>
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return List<Map<String,String>> 待查询属地ID获取下一层属地ID与NAME的对应关系(包含自己)
	 */
	public static List<Map<String,String>> getNextCityListByCityPid(String m_CityPid) {
		
		m_logger.debug("getNextCityListByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		List<Map<String,String>> list =  getNextCityListByCityPidCore(m_CityPid);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("city_id", m_CityPid);
		map.put("city_name", Global.G_CityId_CityName_Map.get(m_CityPid));
		list.add(map);
		
		return list;
	}
	
	/**
	 * 根据父属地ID获取下一层属地ID与NAME的对应关系(不包含自己)
	 * 返回格式为List<Map<String,String>>
	 * 例如：<<city_id,00>,<city_name,省中心>>
	 * 
	 * 此方法对外提供
	 *
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return List<Map<String,String>> 待查询属地ID获取下一层属地ID与NAME的对应关系(不包含自己)
	 */
	public static List<Map<String,String>> getNextCityListByCityPidCore(String m_CityPid) {
		
		m_logger.debug("getNextCityListByCityPidCore(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		List<Map<String,String>> list =  new ArrayList<Map<String,String>>();
		
		Iterator it = Global.G_City_Pcity_Map.keySet().iterator();
		
		while(it.hasNext()) {
			
			String key = (String) it.next();
			if(Global.G_City_Pcity_Map.get(key).equals(m_CityPid)){
				Map<String, String> map = new HashMap<String, String>();
				map.put("city_id", key);
				map.put("city_name", Global.G_CityId_CityName_Map.get(key));
				list.add(map);
			}
		}
		
		return list;
	}
	
	/**
	 * 查询该属地的所有子属地(包括自己)
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return ArrayList 返回待查询属地的所有子属地的city_id(包括自己)
	 */
	public static ArrayList<String> getAllNextCityIdsByCityPid(String m_CityPid){
		
		m_logger.debug("getAllNextCityIdsByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		ArrayList<String> list = getAllNextCityIdsByCityPidCore(m_CityPid);
		list.add(m_CityPid);
		return list;
	}
	
	/**
	 * 查询该属地的所有子属地(不包括自己)
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return ArrayList 返回待查询属地的所有子属地的city_id(不包括自己)
	 */
	public static ArrayList<String> getAllNextCityIdsByCityPidCore(String m_CityPid){
		m_logger.debug("getAllNextCityIdsByCityPidCore(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		ArrayList<String> list = new ArrayList<String>();
		
		if(null==Global.G_City_Child_List_Map.get(m_CityPid)){
			m_logger.warn("getAllNextCityIdsByCityPidCore({})无法获取子属地",m_CityPid);
		}else{
			list.addAll(Global.G_City_Child_List_Map.get(m_CityPid));
		}
		return list;
	}
	
	/**
	 * 查询该属地的所有子属地的city_id与city_name的所有映射(包括自己)
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return Map 返回待属地的所有子属地的city_id与city_name的所有映射(包括自己)
	 */
	public static Map<String,String> getAllNextCityMapByCityPid(String m_CityPid){
		m_logger.debug("getAllNextCityMapByCityPid(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		Map<String,String> map = getAllNextCityMapByCityPidCore(m_CityPid);
		map.put(m_CityPid, Global.G_CityId_CityName_Map.get(m_CityPid));
		
		return map;
	}
	
	/**
	 * 查询该属地的所有子属地的city_id与city_name的所有映射(不包括自己)
	 * 返回格式<"00","省中心">
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return Map 返回待属地的所有子属地的city_id与city_name的所有映射(不包括自己)
	 */
	public static Map<String, String> getAllNextCityMapByCityPidCore(String m_CityPid){
		m_logger.debug("getAllNextCityMapByCityPidCore(m_CityPid:{})",m_CityPid);
		
		if(null==m_CityPid){
			return null;
		}
		
		Map<String,String> map = new ConcurrentHashMap<String,String>();
		ArrayList<String> list = Global.G_City_Child_List_Map.get(m_CityPid);
		for(String cityId:list){
			map.put(cityId, Global.G_CityId_CityName_Map.get(cityId));
		}
		
		return map;
	}
	
	/**
	 * 查询该属地的所有子属地(包括自己)
	 * 返回格式为List<Map<String,String>>
	 * 其中Map:<"city_id","00"><"city_name","省中心">
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return List 其中Map:<"city_id","00"><"city_name","省中心">(包括自己)
	 */
	public static List<Map<String,String>> getAllNextCityListByCityPid(String m_CityPid){
		m_logger.debug("getAllNextCityListByCityPid(m_CityPid:{})",m_CityPid);
		
		List<Map<String,String>> rsList = new ArrayList<Map<String,String>>();
		Map<String,String> map = new HashMap<String,String>();
		map.put("city_id", m_CityPid);
		map.put("city_name", Global.G_CityId_CityName_Map.get(m_CityPid));
		rsList.add(map);
		rsList.addAll(getAllNextCityListByCityPidCore(m_CityPid));
		
		return rsList;
	}
	
	/**
	 * 查询该属地的所有子属地(不包括自己)
	 * 返回格式为List<Map<String,String>>
	 * 其中Map:<"city_id","00"><"city_name","省中心">
	 * 
	 * 此方法对外开发
	 * 
	 * @param m_CityPid 属地
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return List 其中Map:<"city_id","00"><"city_name","省中心">(不包括自己)
	 */
	public static List<Map<String,String>> getAllNextCityListByCityPidCore(String m_CityPid){
		m_logger.debug("getAllNextCityListByCityPidCore(m_CityPid:{})",m_CityPid);
		
		List<Map<String,String>> rsList = new ArrayList<Map<String,String>>();
		ArrayList<String> list = Global.G_City_Child_List_Map.get(m_CityPid);
		for(String cityId:list){
			Map<String,String> map = new HashMap<String,String>();
			map.put("city_id", cityId);
			map.put("city_name", Global.G_CityId_CityName_Map.get(cityId));
			rsList.add(map);
		}
		
		return rsList;
	}
	
	
////////////////////////////////////////////////////////////////////////////////
/////////////////////以下方法为只供系统初始化调用或者该类内部调用///////////////////////
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 获取所有的属地ID(city_id)
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-5-4
	 * @return ArrayList 经查询数据库的全部属地的city_id
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getAllCityIdListCore(){
		
		m_logger.debug("getAllCityIdListCore()");
		
		String strSQL = " select city_id from tab_city order by city_id ";
		ArrayList<String> resultList = new ArrayList<String>();
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(new PrepareSQL(strSQL).getSQL());
		for(Map map : list){
			resultList.add(String.valueOf(map.get("city_id")));
		}
		
		return resultList;
	}
	
	/**
	 * 获取所有属地的G_CityId_CityName_Map
	 * 属地ID、属地名Map<city_id,city_name>
	 * 
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-5-4
	 * @return Map 返回经查询数据库的全部的city_id与city_name的映射Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getCityIdCityNameMapCore(){
		
		m_logger.debug("getCityIdCityNameMapCore()");
		
		String strSQL = "select city_id, city_name from tab_city order by city_id";
		Map map = DBOperation.getMap(new PrepareSQL(strSQL).getSQL());
		Map<String, String> resultMap = new ConcurrentHashMap<String, String>();
		resultMap.putAll(map); 
		return resultMap;
	}
	
	/**
	 * 获取所有子属地与父属地的对应
	 * 获取所有属地的ConcurrentHashMap<ciry_id,parent_id>
	 * 
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-5-4
	 * @return Map 返回经查询数据库的city_id,parent_id的映射Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getCityIdPidMapCore(){
		
		m_logger.debug("getCityIdPidMapCore()");
		
		String strSQL = "select city_id, parent_id from tab_city order by city_id";
		Map map = DBOperation.getMap(new PrepareSQL(strSQL).getSQL());
		Map<String, String> resultMap = new ConcurrentHashMap<String, String>();
		resultMap.putAll(map);
		return resultMap;
	}
	
	/**
	 * @category 查询子节点，不包含自己
	 * 
	 * 此方法不对外开发
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param cityMap
	 * @param cityId
	 * 
	 * @author qixueqi
	 * @date 2009-9-14
	 * @return ArrayList 
	 */
	private ArrayList<String> getAllChild(Map<String,String> cityMap,String cityId){
		
		m_logger.debug("getAllChild(cityMap({}),cityId({}))");
		
		ArrayList<String> childList = new ArrayList<String>();
		Set<String> citySet = cityMap.keySet();
		for (String strCityId : citySet) {
			String strPid = cityMap.get(strCityId);
			if (cityId.equals(strPid)) {
				childList.add(strCityId);
			}
		}
		if(childList.size()>1){
			for(int i=0;i<childList.size();i++){
				childList.addAll(getAllChild(cityMap,childList.get(i)));
			}
		}
		return childList;
	}
	
	/**
	 * 查询出所有的属地的子属地List集合，并将city_id,List形成Map返回
	 * 
	 * 此方法不对外开发
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-9-14
	 * 
	 * @return Map 返回所有的属地与该属地的所有的子属地对应关系
	 */
	public Map<String, ArrayList<String>> getAllCityIdChildListMap(){
		
		m_logger.debug("getAllCityIdChildListMap()");
		
		Map<String, ArrayList<String>> resultMap = new ConcurrentHashMap<String, ArrayList<String>>();
		
		if(null==Global.G_CityIds || Global.G_CityIds.size()<1){
			m_logger.error("Global.G_CityIds没有初始化！");
		}
		if(null==Global.G_City_Pcity_Map || Global.G_City_Pcity_Map.size()<1){
			m_logger.error("Global.G_City_Pcity_Map没有初始化！");
		}
		
		for(int i=0;i<Global.G_CityIds.size();i++){
			String cityId = Global.G_CityIds.get(i);
			resultMap.put(cityId, getAllChild(Global.G_City_Pcity_Map,cityId));
		}
		
		return resultMap;
	}

	/**
	 * 根据城市名称查询对应的城市ID
	 * @param cityName 城市名称
	 * @return 该城市名称对应的城市ID，如果不存在，则返回null
	 */
	public static String getCityId(String cityName)
	{
		if (cityName == null || cityName.trim().length() == 0)
		{
			return null;
		}
		Set<String> cityIdSet = Global.G_CityId_CityName_Map.keySet();
		for (String cityId : cityIdSet)
		{
			if (cityName.equals(Global.G_CityId_CityName_Map.get(cityId)))
			{
				return cityId;
			}
		}
		return null;
	}

}
