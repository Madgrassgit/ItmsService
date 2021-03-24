package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

public class HbdxIptvSetValueChecker extends BaseChecker{
    private static final Logger logger = LoggerFactory.getLogger(HbdxIptvSetValueChecker.class);

    private String inParam = null;

    private String deviceId = "";

    public HbdxIptvSetValueChecker(String inXml)
    {
        this.inParam = inXml;
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean check() {
        logger.debug("HbdxIptvSetValueChecker==>check()" + inParam);

        SAXReader reader = new SAXReader();
        Document document = null;


        try {

            document = reader.read(new StringReader(inParam));
            Element root = document.getRootElement();

            cmdId = root.elementTextTrim("CmdID");// 接口调用唯一ID 每次调用此值不可重复
            cmdType = root.elementTextTrim("CmdType");// 接口类型 CX_01,固定

            /* 客户端类型
             * 1：大唐IOM
             * 2：IPOSS
             * 3：网调
             * 4：RADIUS
             */
            clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

            Element param = root.element("Param");

            // 用户信息类型:1：LOID
            userInfoType = StringUtil.getIntegerValue(param.elementTextTrim("UserInfoType"));

            // 用户信息类型所对应的用户信息
            userInfo = param.elementTextTrim("UserInfo");

            logger.warn(userInfo);

        } catch (Exception e) {
            logger.error("inParam format is err,mesg({})", e.getMessage());
            result = 1;
            resultDesc = "数据格式错误";
            return false;
        }

        if(userInfoType!=1){
            result = 1001;
            resultDesc = "用户信息类型非法";
            return false;
        }

        if (StringUtil.IsEmpty(userInfo)) {
            result = 1000;
            resultDesc = "用户信息为空";
            return false;
        }

        // 参数合法性检查
        if (false == baseCheck()) {
            return false;
        }
        result = 0;
        resultDesc = "成功";

        return true;
    }

    @Override
    public String getReturnXml() {
        logger.debug("getReturnXml()");
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");
        Element root = document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        // 结果代码
        root.addElement("RstCode").addText(StringUtil.getStringValue(result));
        // 结果描述
        root.addElement("RstMsg").addText(resultDesc);

        return document.asXML();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
