package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

/**
 * 甘肃万维宽带上网能力查询
 *
 * @Author lingmin
 * @Date 2020/9/21
 **/
public class InternetDetailsChecker extends BaseChecker{
    private static final Logger LOGGER = LoggerFactory.getLogger(InternetDetailsChecker.class);
    private String cpeOnlineStatus;
    private String cpeType;
    private String cpeModel;
    private String rxPower;
    private String txPower;
    private List<InternetDetailGSDXObj> internetDetails;

    public InternetDetailsChecker(String inXml) {
        callXml = inXml;
    }

    @Override
    public boolean check() {
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(new StringReader(callXml));
            Element root = document.getRootElement();
            cmdId = root.elementTextTrim("CmdID");
            cmdType = root.elementTextTrim("CmdType");
            clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
            Element param = root.element("Param");
            userInfo = param.elementTextTrim("UserInfo");
            userInfoType = Integer.parseInt(param.elementTextTrim("UserInfoType"));
        } catch (Exception e) {
            LOGGER.error("解析xml发生异常，e={}", e);
            result = 1;
            resultDesc = "数据格式错误";
            return false;
        }

        if(1 != userInfoType && 2 != userInfoType){
            result = 1001;
            resultDesc = "用户信息类型非法";
            return false;
        }
        if(StringUtil.IsEmpty(userInfo)){
            result = 1;
            resultDesc = "参数为空";
            return false;
        }
        if(!baseCheck()){
            return false;
        }
        result = 0;
        resultDesc = "成功";
        return true;
    }

    @Override
    public String getReturnXml() {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");
        Element root =  document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        root.addElement("RstCode").addText(StringUtil.getStringValue(result));
        root.addElement("RstMsg").addText(StringUtil.getStringValue(resultDesc));
        root.addElement("CPE_ONLINESTATUS").addText(StringUtil.getStringValue(cpeOnlineStatus));
        root.addElement("CPETYPE").addText(StringUtil.getStringValue(cpeType));
        root.addElement("CPEMODLE").addText(StringUtil.getStringValue(cpeModel));
        root.addElement("RXPOWER").addText(StringUtil.getStringValue(rxPower));
        root.addElement("TXPOWER").addText(StringUtil.getStringValue(txPower));
        Element details =  root.addElement("INTERNETDETAILS");
        if(internetDetails == null || internetDetails.size() == 0){
            Element detail =  details.addElement("INTERNETDETAILSNUM");
            detail.addElement("CONNECTIONTYPE").addText("");
            detail.addElement("CONNECTIONSTATUS").addText("");
            detail.addElement("LANINTERFACEBIND").addText("");
        }else {
            for(InternetDetailGSDXObj detailObj : internetDetails){
                Element detail =  details.addElement("INTERNETDETAILSNUM");
                detail.addElement("CONNECTIONTYPE").addText(StringUtil.getStringValue(detailObj.getConnectionType()));
                detail.addElement("CONNECTIONSTATUS").addText(StringUtil.getStringValue(detailObj.getConnectionStatus()));
                detail.addElement("LANINTERFACEBIND").addText(StringUtil.getStringValue(detailObj.getLanInterfaceBind()));
            }
        }
        LOGGER.warn("document = {}, xml = {}", document, document.asXML());
        return document.asXML();
    }

    public String getCpeOnlineStatus() {
        return cpeOnlineStatus;
    }

    public void setCpeOnlineStatus(String cpeOnlineStatus) {
        this.cpeOnlineStatus = cpeOnlineStatus;
    }

    public String getCpeType() {
        return cpeType;
    }

    public void setCpeType(String cpeType) {
        this.cpeType = cpeType;
    }

    public String getCpeModel() {
        return cpeModel;
    }

    public void setCpeModel(String cpeModel) {
        this.cpeModel = cpeModel;
    }

    public String getRxPower() {
        return rxPower;
    }

    public void setRxPower(String rxPower) {
        this.rxPower = rxPower;
    }

    public String getTxPower() {
        return txPower;
    }

    public void setTxPower(String txPower) {
        this.txPower = txPower;
    }

    public List<InternetDetailGSDXObj> getInternetDetails() {
        return internetDetails;
    }

    public void setInternetDetails(List<InternetDetailGSDXObj> internetDetails) {
        this.internetDetails = internetDetails;
    }
}
