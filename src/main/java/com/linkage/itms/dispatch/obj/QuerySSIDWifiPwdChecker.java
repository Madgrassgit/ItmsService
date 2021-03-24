package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.commom.enums.ErrorCodeEnum;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class QuerySSIDWifiPwdChecker extends BaseChecker {

    private static final Logger logger = LoggerFactory.getLogger(QuerySSIDWifiPwdChecker.class);

    private static final String RETURN_XML_CODE = "GBK";

    private String wifiPasswd;

    private String ssid;

    private String devNumber;

    public QuerySSIDWifiPwdChecker(String callXml) {
        this.callXml = callXml;
    }

    /**
     * 解析入参
     * 参数合法性检查
     * @return 校验结果 true or false
     */
    @Override
    public boolean check() {
        logger.debug("QuerySSIDWifiPwdChecker.check begin");
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(new StringReader(callXml));
            Element root = document.getRootElement();

            cmdId = root.elementTextTrim("CmdID");
            cmdType = root.elementTextTrim("CmdType");
            clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

            Element param = root.element("Param");
            userInfoType = StringUtil.getIntegerValue(param
                    .elementTextTrim("UserInfoType"));
            userInfo = param.elementTextTrim("UserInfo");
            result = ErrorCodeEnum.SUCCESS.getCode();
            resultDesc = ErrorCodeEnum.SUCCESS.getDesc();

        }catch (Exception e){
            logger.error("QuerySSIDWifiPwdChecker.check error:",e);
            result = ErrorCodeEnum.INVALID_PARAM.getCode();
            resultDesc = ErrorCodeEnum.INVALID_PARAM.getDesc();
            return false;
        }
        // 参数合法性检查 任何一个检查失败则返回false
        return baseCheck() && userInfoTypeCheck() && userInfoCheck();
    }

    protected boolean userInfoTypeCheck(){
        if(1 != userInfoType && 2 != userInfoType && 3 != userInfoType){
            result = ErrorCodeEnum.INVALID_USERINFO_TYPE.getCode();
            resultDesc = ErrorCodeEnum.INVALID_USERINFO_TYPE.getDesc();
            return false;
        }
        return true;
    }

    protected boolean userInfoCheck(){
        //用户loid长度不可小于 6
        if(StringUtil.IsEmpty(userInfo) || (userInfoType == 2 && userInfo.length() < 6)){
            result = ErrorCodeEnum.INVALID_USERINFO.getCode();
            resultDesc = ErrorCodeEnum.INVALID_USERINFO.getDesc();
            return false;
        }
        return true;
    }

    @Override
    public String getReturnXml() {
        logger.debug("QuerySSIDWifiPwdChecker.getReturnXml()");
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding(RETURN_XML_CODE);
        Element root = document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        // 结果代码
        root.addElement("RstCode").addText("" + result);
        // 结果描述
        root.addElement("RstMsg").addText("" + resultDesc);
        // SSID
        root.addElement("SSID").addText("" + ssid);
        //设备连接数
        root.addElement("DevNumber").addText("" + devNumber);
        // wifi密码
        root.addElement("WifiPasswd").addText("" + wifiPasswd);

        return document.asXML();
    }

    public String getDevNumber() {
        return devNumber;
    }

    public void setDevNumber(String devNumber) {
        this.devNumber = devNumber;
    }

    public String getWifiPasswd() {
        return wifiPasswd;
    }

    public void setWifiPasswd(String wifiPasswd) {
        this.wifiPasswd = wifiPasswd;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
