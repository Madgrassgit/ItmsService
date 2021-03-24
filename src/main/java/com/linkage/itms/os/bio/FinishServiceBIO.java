package com.linkage.itms.os.bio;

import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * 
 * @author xiangzl (Ailk No.)
 * @version 1.0
 * @since Jul 29, 2013
 * @category com.linkage.itms.os.bio
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class FinishServiceBIO
{
	private static Logger logger = LoggerFactory.getLogger(FinishServiceBIO.class);

	public String doFinishSheet(String strXML)
	{
		logger.warn("doFinishSheet({})",strXML);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(strXML));
			Element root = document.getRootElement();
			String cmdId = root.elementText("CmdID");
			String loid = root.elementText("Loid");
			String serviceType = root.elementText("ServiceType");
			String serviceUsername = root.elementText("ServiceUsername");
			String openStatus = root.elementText("OpenStatus");
			String cityId = root.elementText("CityId");
			
			if(isNumeric(cmdId))
			{
				updateOrderStatus(cmdId,openStatus,cityId);
			}
			else
			{
				logger.error("cmdId : {} 不是数字类型，不更新 SPJK_TABLE 表",cmdId);
			}
		
		}
		catch (Exception e) {
			logger.error("Exception:\n{}", e);
		}
		return "成功";
	}

	private void updateOrderStatus(String cmdId, String openStatus,String cityId)
	{
		if("1".equals(openStatus))
		{
			String sql = "update SPJK_TABLE set STS = 'C' where SPJK_ID = '"+cmdId+"' ";
			PrepareSQL psql = new PrepareSQL(sql);
			if (!cityId.startsWith("1001"))
			{
				executeUpdate(psql.getSQL(), "midbdw");
			}
			else 
			{
				executeUpdate(psql.getSQL(), "midwd");
			}
			
		}
		
	}
	
	public static int executeUpdate(String sql, String alias)
	{
		logger.debug("executeUpdate({},{})", sql, alias);
		int iCode = -1;
		Connection conn = null;
		PreparedStatement pst = null;
		try
		{
			conn = DBAdapter.getJDBCConnection("proxool." + alias);
			if (conn == null)
			{
				logger.debug("conn == null");
				return iCode;
			}
			pst = conn.prepareStatement(sql);
			iCode = pst.executeUpdate();
		}
		catch (SQLException e1)
		{
			logger.error("SQLException:{}\n{}", new Object[] { e1.getMessage(), sql });
		}
		catch (Exception ex)
		{
			logger.error("Exception:{}\n{}", new Object[] { ex.getMessage(), sql });
		}
		finally
		{
			try
			{
				if (null != pst)
				{
					pst.close();
					pst = null;
				}
			}
			catch (SQLException e)
			{
				logger.error("SQLException:{}", e.getMessage());
			}
			try
			{
				if(conn != null){
					conn.close();
					conn = null;
				}
			}
			catch (Exception e)
			{
				logger.error("Exception:close connection,{}", e.getMessage());
			}
		}
		return iCode;
	}
	
	private static boolean isNumeric(String str){ 
		if(!StringUtil.IsEmpty(str))
		{
			  Pattern pattern = Pattern.compile("[0-9]*"); 
			  return pattern.matcher(str).matches();
		}
		else
		{
			return false;
		}
	       
	 } 
	
}
