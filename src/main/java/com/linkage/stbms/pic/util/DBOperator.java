package com.linkage.stbms.pic.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBAdapter;
import com.linkage.commons.db.DBOperation;
import com.linkage.stbms.ids.obj.SysConstant;


/**
 * @author zhangsm
 * @version 1.0
 * @since 2011-7-12 下午04:13:23
 * @category com.linkage.litms.eserver.db<br>
 * @copyright 亚信联创 网管产品部
 */
public class DBOperator extends DBOperation
{
	public static Logger logger = LoggerFactory.getLogger(DBOperator.class);
	/**
	 * get db type.
	 * 
	 * @return
	 * <li>0:other db.</li>
	 * <li>1:oracle</li>
	 * <li>2:sybase</li>
	 */
	public static int GetDB() {
		int i = 2;

//		if (DBConnectHome.poolName.equals("oraclepool")) {
//			i = 1;
//		} else if (DBConnectHome.poolName.equals("sybasepool")) {
//			i = 2;
//		} else {
//			i = 0;
//		}
		if(SysConstant.G_DBType.equals("oracle"))
		{
			i = 1;
		}
		else if(SysConstant.G_DBType.equals("sybase"))
		{
			i = 2;
		}
		else
		{
			i = 0;
		}
		return i;
	}
	public static long execProc(String sql,int param)
	{
	    long reslt = -1;
	    Connection conn = DBAdapter.getJDBCConnection();
	    CallableStatement proc = null;
	    try {
//	      proc = conn.prepareCall("{ call HYQ.TESTB(?,?) }");
	      proc = conn.prepareCall(sql);
	      proc.setInt(1, param);
	      proc.registerOutParameter(2, Types.BIGINT);
	      proc.execute();
	      reslt =  proc.getLong(2);
	      logger.info("reslt=====" + reslt);
	    }
	    catch (SQLException ex2) {
	    	logger.error(sql);
	    	logger.error(ex2.getMessage());
		}
	    catch (Exception ex2) {
	    	logger.error(sql);
	    	logger.error(ex2.getMessage());
		}
	    finally{
	      try {
	          if(proc!=null){
	        	  proc.close();
	          }
	          if(conn!=null){
	            conn.close();
	          }
	      }
	      catch (SQLException ex1) {
	      }
	    }
	    return reslt;
	}
}
