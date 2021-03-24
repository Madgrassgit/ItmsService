/**
 * 
 */
package com.linkage.itms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.itms.Global;


/**
 * @author zxj E-mail：qixq@lianchuang.com
 * @version $Revision$ 
 * @since 2009-11-6
 * @category com.linkage.module.gwms.dao.tabquery
 * 
 */
public class AreaDAO {

	//日志记录
	private static final Logger m_logger = LoggerFactory.getLogger(AreaDAO.class);
	
	/**
	 * 获取所有的域ID(area_id)
	 * 该方法对外开放
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * 
	 * @return ArrayList 所有的域area_id
	 */
	public static ArrayList<String> getAllAreaIdList(){
		
		m_logger.debug("getAllAreaIdList()");
		
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(Global.G_Area_Id_List);
		
		return list;
	}
	
	/**
	 * 获取所有子域与父域的对应
	 * 获取所有域的ConcurrentHashMap<ciry_id,parent_id>
	 * 
	 * 该方法对外开放
	 * 
	 * @param 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * @return Map 返回所有的的area_id,area_pid的映射Map
	 */
	public static Map<String,String> getAreaIdPidMap(){
		
		m_logger.debug("getAreaIdPidMap()");
		
		Map<String,String> map = new ConcurrentHashMap<String,String>();
		map.putAll(Global.G_Area_PArea_Map);
		
		return map;
	}
	
	/**
	 * 获取属地的对应域
	 * 
	 * 该方法对外开放
	 * 
	 * @param 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * @return 
	 */
	public static String getAreaIdByCityId(String cityId){
		
		m_logger.debug("getAreaIdByCityId(cityId:{})",cityId);
		
		return Global.G_CityId_AreaId_Map.get(cityId);
	}
	
	/**
	 * 根据当前域查询该域的所有上级域，直至省中心（包括自己）
	 * 
	 * 此方法对外提供
	 * 
	 * @param areaId
	 * @return
	 */
	public static List<String> getAllPAreaIdByAreaId(String areaId){
		
		m_logger.debug("getAllPAreaIdByAreaId(areaId:{})",areaId);
		
		List<String> list = new ArrayList<String>();
		list.add(areaId);
		if("0".equals(areaId) || "1".equals(areaId)){
			return list;
		}
		String tempAreaId = Global.G_Area_PArea_Map.get(areaId);
		do{
			list.add(tempAreaId);
			tempAreaId = Global.G_Area_PArea_Map.get(tempAreaId);
		}while(!"1".equals(tempAreaId) && null!=tempAreaId);
		
		return list;
	}
	
	/**
	 * 查询该域的所有子域(包括自己)
	 * 
	 * 此方法对外开发
	 * 
	 * @param areaId 域
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * 
	 * @return ArrayList 返回待查询域的所有子域的area_id(包括自己)
	 */
	public static ArrayList<String> getAllNextAreaIdsByAreaPid(String areaId){
		
		m_logger.debug("getAllNextAreaIdsByAreaPid(areaId:{})",areaId);
		
		ArrayList<String> list = getAllNextAreaIdsByAreaPidCore(areaId);
		list.add(areaId);
		
		return list;
	}
	
	/**
	 * 查询该域的所有子域(不包括自己)
	 * 
	 * 此方法对外开发
	 * 
	 * @param areaId 域
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * 
	 * @return ArrayList 返回待查询域的所有子域的area_id(不包括自己)
	 */
	public static ArrayList<String> getAllNextAreaIdsByAreaPidCore(String areaId){
		m_logger.debug("getAllNextAreaIdsByAreaPidCore(areaId:{})",areaId);
		
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(Global.G_PArea_AreaList_Map.get(areaId));
		
		return list;
	}
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////以下方法为只供系统初始化调用或者该类内部调用///////////////////
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 获取所有的域ID(area_id)
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-11-09
	 * @return ArrayList 经查询数据库的全部域的area_id
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getAllAreaIdListCore(){
		
		m_logger.debug("getAllAreaIdListCore()");
		
		String strSQL = " select area_id from tab_area ";
		ArrayList<String> resultList = new ArrayList<String>();
		ArrayList<HashMap<String, String>> list = DBOperation.getRecords(strSQL);
		for(Map map : list){
			resultList.add(String.valueOf(map.get("area_id")));
		}
		
		return resultList;
	}
	
	/**
	 * 获取所有子域与父域的对应
	 * 获取所有属地的ConcurrentHashMap<area_id,area_pid>
	 * 
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-11-09
	 * @return Map 返回经查询数据库的area_id,area_pid的映射Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getAreaIdPidMapCore(){
		
		m_logger.debug("getAreaIdPidMapCore()");
		
		String strSQL = "select area_id,area_pid from tab_area order by area_id";
		Map map = DBOperation.getMap(strSQL);
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
	 * @param areaMap
	 * @param areaId
	 * 
	 * @author qixueqi
	 * @date 2009-11-09
	 * @return ArrayList 
	 */
	private ArrayList<String> getAllChild(Map<String,String> areaMap,String areaId){
		
		m_logger.debug("getAllChild(areaMap({}),areaId({}))");

		ArrayList<String> childList = new ArrayList<String>();
		Set<String> areaSet = areaMap.keySet();
		for (String strAreaId : areaSet) {
			String strPid = areaMap.get(strAreaId);
			if (areaId.equals(strPid)) {
				childList.add(strAreaId);
			}
		}
		if(childList.size()>1){
			for(int i=0;i<childList.size();i++){
				childList.addAll(getAllChild(areaMap,childList.get(i)));
			}
		}
		return childList;
	}
	
	/**
	 * 查询出所有的属地的子域List集合，并将area_id,List形成Map返回
	 * 
	 * 此方法不对外开发
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @author onelinesky(4174)
	 * @date 2009-11-09
	 * 
	 * @return Map 返回所有的属地与该属地的所有的子属地对应关系
	 */
	public Map<String, ArrayList<String>> getAllAreaIdChildListMapCore(){
		
		m_logger.debug("getAllAreaIdChildListMap()");
		
		Map<String, ArrayList<String>> resultMap = new ConcurrentHashMap<String, ArrayList<String>>();
		
		if(null==Global.G_Area_Id_List || Global.G_Area_Id_List.size()<1){
			m_logger.error("Global.G_Area_Id_List没有初始化！");
		}
		if(null==Global.G_Area_PArea_Map || Global.G_Area_PArea_Map.size()<1){
			m_logger.error("Global.G_Area_PArea_Map没有初始化！");
		}
		
		for(int i=0;i<Global.G_Area_Id_List.size();i++){
			String areaId = Global.G_Area_Id_List.get(i);
			resultMap.put(areaId, getAllChild(Global.G_Area_PArea_Map,areaId));
		}
		
		return resultMap;
	}
	
	/**
	 * 获取所有属地与域的对应
	 * 获取所有属地的ConcurrentHashMap<city_id,area_id>
	 * 
	 * 该方法不对外开放
	 * 只供系统初始化调用或者该类内部调用
	 * 
	 * @param 
	 * @author qixueqi
	 * @date 2009-11-09
	 * @return Map 返回经查询数据库的city_id,area_id的映射Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getCityIdAreaIdMapCore(){
		
		m_logger.debug("getCityIdAreaIdCore()");
		
		String strSQL = " select city_id,area_id from tab_city_area ";
		Map map = DBOperation.getMap(strSQL);
		Map<String, String> resultMap = new ConcurrentHashMap<String, String>();
		resultMap.putAll(map);
		return resultMap;
	}
	
}
