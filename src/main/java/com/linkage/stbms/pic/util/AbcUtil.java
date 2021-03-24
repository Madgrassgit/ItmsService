package com.linkage.stbms.pic.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.DBUtil;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.DbUtils;
import com.linkage.stbms.ids.obj.SysConstant;

public class AbcUtil {
	
	/** log */
	private static final Logger log = LoggerFactory.getLogger(AbcUtil.class);
	public static long MAX_UNUSED_ID = -1L;
	
	public static long MIN_UNUSED_ID = -1L;
	
	public static int SUM_UNUSED_ID = SysConstant.maxIdNum;
	
	synchronized static public long generateLongId() {
		
		if(DBUtil.GetDB() == 1 || DBUtil.GetDB() == 2) {
			return generateLongIdOld();
		}
		
		return DbUtils.getUnusedID("sql_bind_log", SUM_UNUSED_ID);
	}
	
	/**
	 * get device_id
	 * 
	 * @param count
	 * @return
	 */
	static public long generateLongIdOld() {
		log.debug("GetUnusedBindLogId()");
		int count = 1;
//		long serial = -1;

//		if (count <= 0) {
//			serial = -2;
//
//			return serial;
//		}

		if( MIN_UNUSED_ID < 0 ){
			MIN_UNUSED_ID = getLongId(SUM_UNUSED_ID) - 1;
			MAX_UNUSED_ID = MIN_UNUSED_ID + SUM_UNUSED_ID;
		}
		
		if( MAX_UNUSED_ID <= (MIN_UNUSED_ID + count)){
			
//			if(SUM_UNUSED_ID < count){
//				MIN_UNUSED_ID = getMaxHGWUserId(SUM_UNUSED_ID) - 1;
//				MAX_UNUSED_ID = MIN_UNUSED_ID + count - 1;
//			} else {
				MIN_UNUSED_ID = getLongId(SUM_UNUSED_ID) - 1;
				MAX_UNUSED_ID = MIN_UNUSED_ID + SUM_UNUSED_ID ;
//			}

		}
		
//		serial = MIN_UNUSED_ID + 1;
		MIN_UNUSED_ID = MIN_UNUSED_ID + count;

		log.debug("ID={}", MIN_UNUSED_ID);

		return MIN_UNUSED_ID;
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
	public static Map<String, String> getCityIdAreaIdMapCore(){
		
		log.debug("getCityIdAreaIdCore()");
		
		String strSQL = " select city_id,area_id from tab_city_area ";
		PrepareSQL psql = new PrepareSQL(strSQL);
		psql.getSQL();
		Map map = DBOperation.getMap(strSQL);
		Map<String, String> resultMap = new ConcurrentHashMap<String, String>();
		resultMap.putAll(map);
		return resultMap;
	}

	/**
	 * 产生唯一主键，利用较低的重复概率
	 * @return long
	 */
	public static long getLongId(int count) {
//	    long bindLogId = 1L;
        String callPro = "";
        
        
        if(SysConstant.G_DBType.equals("oracle"))
		{
        	callPro = "{ call maxBindLogIdProc(?,?) }";
			return DBOperator.execProc(callPro,count);
		}
		else
		{
			callPro = "maxBindLogIdProc ?";
			PrepareSQL pSQL = new PrepareSQL(callPro);
			pSQL.setInt(1, count);
			Map map = DBOperation.getRecord(pSQL.getSQL());
			if(map != null && !map.isEmpty()){
				return Long.parseLong(map.values().toArray()[0].toString());
			}
			return -1L;
		}
        
        
        
//        Map map = DBOperation.getRecord(callPro);
//        if (null != map && !map.isEmpty()) {
//            bindLogId = StringUtil.getLongValue(map.values().toArray()[0].toString());
//        } else {
//            bindLogId = DBOperation.getMaxId("bind_id", "bind_log");
//        }
//		return bindLogId;
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
		
		log.debug("getLocationCityIdByCityId(cityId:{})",cityId);
		
		if(null==cityId){
			return null;
		}
		
		String rsCityId = cityId;
		
		if("-1".equals(cityId) || "00".equals(cityId)){
			rsCityId = "00";
		}else{
			while(!"00".equals(SysConstant.G_City_Pcity_Map.get(rsCityId)) && null!=(SysConstant.G_City_Pcity_Map.get(rsCityId)) && !"-1".equals(SysConstant.G_City_Pcity_Map.get(rsCityId))){
				rsCityId = SysConstant.G_City_Pcity_Map.get(rsCityId);
			}
		}
		
		return rsCityId;
	}
	
	public static boolean arrContains(String[] eventArr, String eVENT_CODE_BIND) {
		if (eventArr == null || eventArr.length == 0) {
			return false;
		}
		for (String eventCode : eventArr) {
			if (eventCode.equals(eVENT_CODE_BIND)) {
				return true;
			}
		}
		return false;
	}
}
