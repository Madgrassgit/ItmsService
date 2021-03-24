package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.dao;

import com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity.LogicIdResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MobileOperationDaoImpl{
//{
//  private static final Log log = LogFactory.getLog(MobileOperationDaoImpl.class);
//
  public List<LogicIdResult> getFeedbackInfoByLogicId(List<String> loid){return null;}
//  {
//    int cnt = 1;
//    Connection conn = null;
//    PreparedStatement ps = null;
//    ResultSet rs = null;
//    List resultList = new ArrayList();
//    List loidIdList = new ArrayList();
//
//    StringBuilder querySql = new StringBuilder("SELECT LOID,SERIAL_NUMBER,BROADBAND_STATUS,IPTV_STATUS,VOIP_STATUS FROM DEVICE_PRECONFIG WHERE ");
//
//    if (loid.size() == 0) {
//      log.info("loid list is null");
//      return resultList;
//    }
//    for (String s : loid) {
//      if (StringUtils.isNotBlank(s))
//        loidIdList.add(s);
//    }
////    try
////    {
////      conn = DBUtils.getConnection();
////      for (String s : loidIdList) {
////        querySql.append(" LOID = ? OR");
////      }
////      if (!loidIdList.isEmpty()) {
////        querySql.append("#END#");
////      }
////      ps = conn.prepareStatement(querySql.toString().replace("OR#END#", ""));
////      for (String logicId : loidIdList) {
////        ps.setString(cnt++, logicId);
////      }
//      rs = ps.executeQuery();
//      while (rs.next()) {
//        String userLogicId = rs.getString("LOID");
//        String serialNumber = rs.getString("SERIAL_NUMBER");
//        if ((StringUtils.isBlank(serialNumber)) || (serialNumber.equals("0")))
//        {
//          serialNumber = "";
//        }
//        LogicIdResult result = new LogicIdResult(userLogicId);
//        result.setSerial_number(serialNumber);
//        result.setService_status(getServiceStatus(userLogicId, serialNumber));
//
//        result.setService_list(getServiceList(rs.getInt("BROADBAND_STATUS"), rs.getInt("IPTV_STATUS"), rs.getInt("VOIP_STATUS")));
//
//        resultList.add(result);
//
//        loidIdList.remove(userLogicId);
//      }
//      if (loidIdList.size() > 0) {
//        log.info(new StringBuilder().append("can`t find info by logic id: ").append(loidIdList).toString());
//        for (String logicId : loidIdList) {
//          LogicIdResult result = new LogicIdResult(logicId);
//          resultList.add(result);
//        }
//      }
//    } catch (SQLException e) {
//      log.error(new StringBuilder().append("Could not execute sql :").append(querySql.toString()).toString(), e);
//      if (loidIdList.size() > 0) {
//        log.info(new StringBuilder().append("can`t find info by logic id: ").append(loidIdList).toString());
//        for (String logicId : loidIdList) {
//          LogicIdResult result = new LogicIdResult(logicId);
//          result.setError_msg("system error");
//          resultList.add(result);
//        }
//      }
//    } finally {
//      DBUtils.close(rs, ps, conn);
//    }
//
//    return checkDeviceOnline(resultList);
//  }

  public int getServiceStatus(String userLogicId, String serialNumber) {
    if ((StringUtils.isNotBlank(userLogicId)) && (StringUtils.isNotBlank(serialNumber)))
    {
      return 0;
    }if ((StringUtils.isBlank(serialNumber)) && (StringUtils.isNotBlank(userLogicId)))
    {
      return 1;
    }
    return 2;
  }

  private String getServiceList(int wBand, int iptv, int voip)
  {
    StringBuilder sb = new StringBuilder();
    if (wBand == 2) {
      sb.append("broadband,");
    }
    if (iptv == 2) {
      sb.append("IPTV,");
    }
    if (voip == 2) {
      sb.append("VOIP,");
    }
    if (sb.length() > 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

//  private List<LogicIdResult> checkDeviceOnline(List<LogicIdResult> resultList) {
//    Set set = new HashSet();
//    Connection conn = null;
//    PreparedStatement ps = null;
//    ResultSet rs = null;
//
//    StringBuilder querySql = new StringBuilder("SELECT SERIALNUMBER FROM DEVICE T WHERE ACTIVATED = 1 AND MANAGED = 1 AND DELETED = 0 AND ( ");
//    try
//    {
//      int size = 0;
//      for (LogicIdResult result : resultList) {
//        if ((StringUtils.isNotBlank(result.getSerial_number())) && (result.getService_status() == 0))
//        {
//          querySql.append(" SERIALNUMBER = ? OR");
//          size++;
//        }
//      }
//      if (size != 0)
//        querySql.append("#END#");
//      else {
//        return resultList;
//      }
//      querySql.append(")");
//      conn = DBUtils.getConnection();
//      ps = conn.prepareStatement(querySql.toString().replace("OR#END#", ""));
//      int index = 0;
//      for (LogicIdResult result : resultList) {
//        if ((StringUtils.isNotBlank(result.getSerial_number())) && (result.getService_status() == 0))
//        {
//          ps.setString(index + 1, result.getSerial_number());
//          index++;
//        }
//      }
//      rs = ps.executeQuery();
//      while (rs.next()) {
//        String serialNumber = rs.getString("SERIALNUMBER");
//        set.add(serialNumber);
//      }
//    } catch (Exception e) {
//      log.error("checkDeviceOnline error", e);
//    } finally {
//      DBUtils.close(rs, ps, conn);
//    }
//    for (LogicIdResult result : resultList) {
//      if ((StringUtils.isNotBlank(result.getSerial_number())) && (result.getService_status() == 0))
//      {
//        if (!set.contains(result.getSerial_number())) {
//          result.setService_status(1);
//        }
//      }
//    }
//    return resultList;
//  }
}