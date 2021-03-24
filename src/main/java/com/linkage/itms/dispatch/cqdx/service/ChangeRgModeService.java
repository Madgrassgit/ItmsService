package com.linkage.itms.dispatch.cqdx.service;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.util.SocketUtil;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.ChangeRgModeDealXML;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeRgModeService
{
  private static Logger logger = LoggerFactory.getLogger(ChangeRgModeService.class);
  
  public String work(String inXml)
  {
    logger.warn("servicename[ChangeRgModeService]执行，入参为：{}", inXml);
    ChangeRgModeDealXML deal = new ChangeRgModeDealXML();
    Document document = deal.getXML(inXml);
    if (document == null)
    {
      logger.warn("servicename[ChangeRgModeService]解析入参错误！");
      deal.setResult("-99");
      deal.setErrMsg("解析入参错误！");
      return deal.returnXML();
    }
    Element param = document.getRootElement().addElement("Param");
    String logicId = deal.getLogicId();
    String netName = deal.getPppUsename();
    
    PublicDAO pulicDao = new PublicDAO();
    List<HashMap<String, String>> userMapList = null;
    if (!StringUtil.IsEmpty(logicId))
    {
      userMapList = pulicDao.getBussinessInfo4net(2, logicId, "");
    }
    else if (!StringUtil.IsEmpty(netName))
    {
      userMapList = pulicDao.getBussinessInfo4net(1, netName, "");
    }
    else
    {
      logger.warn("servicename[ChangeRgModeService]入参格式错误！");
      deal.setResult("-99");
      deal.setErrMsg("入参格式错误！");
      return deal.returnXML();
    }
    if (userMapList.size() == 0)
    {
      logger.warn("servicename[ChangeRgModeService]入参格式错误！");
      deal.setResult("-1");
      deal.setErrMsg("用户不存在");
      return deal.returnXML();
    }
    if (StringUtil.IsEmpty(deal.getRgMode()))
    {
      logger.warn("servicename[ChangeRgModeService]入参格式错误！");
      deal.setResult("-99");
      deal.setErrMsg("入参格式错误！");
      return deal.returnXML();
    }
    String loid = StringUtil.getStringValue((Map)userMapList.get(0), "loid");
    String pppUsename = StringUtil.getStringValue((Map)userMapList.get(0), "pppusename");
    String passwd = StringUtil.getStringValue(deal.getBroadbandPassword());
    String wanType = StringUtil.getStringValue((Map)userMapList.get(0), "wan_type");
    if (("1".equals(deal.getRgMode())) && ("2".equals(wanType)))
    {
      logger.warn("[{}]servicename[ChangeRgModeService]已经是路由模式！", loid);
      deal.setResult("-99");
      deal.setErrMsg("已经是路由模式");
      return deal.returnXML();
    }
    if (("2".equals(deal.getRgMode())) && ("1".equals(wanType)))
    {
      logger.warn("[{}]servicename[ChangeRgModeService]已经是桥接模式！", loid);
      deal.setResult("-99");
      deal.setErrMsg("已经是桥接模式");
      return deal.returnXML();
    }
    if ("1".equals(deal.getRgMode())) {
      wanType = "2";
    } else {
      wanType = "1";
    }
    StringBuffer bssSheet = new StringBuffer();
    bssSheet.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
    bssSheet.append("<itms_97_interface>");
    bssSheet.append("<service_type>21</service_type>");
    bssSheet.append("<service_opt>9</service_opt>");
    bssSheet.append("<itms_97_info>");
    bssSheet.append("<work_asgn_id>" + System.currentTimeMillis() + "_itmsService" + "</work_asgn_id>");
    bssSheet.append("<logic_id>" + loid + "</logic_id>");
    bssSheet.append("<customer_id></customer_id>");
    bssSheet.append("<account_name>" + pppUsename + "</account_name>");
    bssSheet.append("<passwd>" + passwd + "</passwd>");
    bssSheet.append("<rg_mode>" + deal.getRgMode() + "</rg_mode> ");
    bssSheet.append("<vlan_id></vlan_id>");
    bssSheet.append("</itms_97_info>");
    bssSheet.append("</itms_97_interface>");
    logger.warn("发送工单：" + bssSheet.toString());
    String str = SocketUtil.sendStrMesg(
      Global.G_ITMS_SHEET_SERVER_CHINA_MOBILE, 
      StringUtil.getIntegerValue(Global.G_ITMS_SHEET_PORT_CHINA_MOBILE), 
      bssSheet.toString());
    logger.warn("{}回单：" + str, loid);
    deal.setResult("0");
    deal.setErrMsg("接收成功");
    String ret = deal.returnXML();
    deal.recordLog("ChangeRgModeService", "", "", inXml, ret);
    return deal.returnXML();
  }
}
