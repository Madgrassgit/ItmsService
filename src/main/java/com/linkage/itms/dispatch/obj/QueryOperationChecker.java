package com.linkage.itms.dispatch.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

/**
 * @author songxq
 * @date 2021/1/14
 */
public class QueryOperationChecker extends BaseChecker{
    private static final Logger logger = LoggerFactory.getLogger(QueryOperationChecker.class);

    private static final int ONE = 1;

    private static final int TWO = 2;

    private String inParam = null;

    private List<HashMap<String, String>> resultList = null;

    public QueryOperationChecker(String inXml)
    {
        this.inParam = inXml;
    }

    @Override
    public boolean check() {
        logger.debug("QueryOperationChecker==>check() [{}]" , inParam);

        SAXReader reader = new SAXReader();
        try
        {
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        }
        catch (SAXException e)
        {
            logger.error("QueryOperationChecker.check error:",e);
        }
        Document document = null;


        try {

            document = reader.read(new StringReader(inParam));
            Element root = document.getRootElement();
            /**
             * 接口调用唯一ID 每次调用此值不可重复
             */
            cmdId = root.elementTextTrim("CmdID");
            /**
             * 接口类型 CX_01,固定
             */
            cmdType = root.elementTextTrim("CmdType");

            /* 客户端类型
             * 1：BSS
                2：IPOSS
                3：综调
                4：RADIUS
                5：爱运维
                6：预处理
                7：网监
                8：预留
             */
            clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));

            Element param = root.element("Param");

            // 用户信息类型:1：LOID  2:宽带账号
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

        if(userInfoType != ONE && userInfoType != TWO){
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
        if (!baseCheck()) {
            return false;
        }
        result = 0;
        resultDesc = "成功";

        return true;
    }

    @Override
    public String getReturnXml() {
        logger.debug("getReturnXml()");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("GBK");
        Element root = document.addElement("root");
        // 接口调用唯一ID
        root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
        // 结果代码
        root.addElement("RstCode").addText(StringUtil.getStringValue(result));
        // 结果描述
        root.addElement("RstMsg").addText(resultDesc);
        if(0 == result)
        {
            for (int i = 0; i <resultList.size() ; i++) {
                HashMap<String,String> map = resultList.get(i);
                Element msg = root.addElement("Msg"+StringUtil.getStringValue(i+1));
                msg.addElement("time").addText(sdf.format(StringUtil.getLongValue(map.get("add_time")) * 1000L));
                String action = StringUtil.getStringValue(map.get("oper_action"));
                if("1".equals(action))
                {
                    msg.addElement("act").addText("路由改桥");
                }
                else if("2".equals(action))
                {
                    msg.addElement("act").addText("桥改路由");
                }
                else if("3".equals(action))
                {
                    msg.addElement("act").addText("桥接改桥接");
                }
                else if("4".equals(action))
                {
                    msg.addElement("act").addText("路由改路由");
                }
                else
                {
                    msg.addElement("act").addText("");
                }
                msg.addElement("loid").addText(StringUtil.getStringValue(map.get("loid")));
                msg.addElement("UserInfo").addText(StringUtil.getStringValue(map.get("username")));
                msg.addElement("origin").addText(StringUtil.getStringValue(map.get("oper_origon")));
                msg.addElement("staff").addText(StringUtil.getStringValue(map.get("oper_staff")));
            }
        }

        return document.asXML();
    }

    public List<HashMap<String, String>> getResultList() {
        return resultList;
    }

    public void setResultList(List<HashMap<String, String>> resultList) {
        this.resultList = resultList;
    }
}
