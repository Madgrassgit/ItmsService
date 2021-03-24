package com.linkage.commom.util;
 

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.*;

import java.nio.charset.Charset;
import java.util.*;

public class HttpClientCallSoapUtil {

    static int socketTimeout = 30000;// 请求超时时间
    static int connectTimeout = 30000;// 传输超时时间
    static Logger logger = Logger.getLogger(HttpClientCallSoapUtil.class);

    /**
     * 使用SOAP1.1发送消息
     *
     * @param postUrl
     * @param soapXml
     * @param soapAction
     * @return
     */
    public static String doPostSoap(String postUrl, String soapXml,String soapAction) {
        String retStr = "";
        // 创建HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // HttpClient
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpPost httpPost = new HttpPost(postUrl);
        //  设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout).build();
        httpPost.setConfig(requestConfig);
        try {
            httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
            httpPost.setHeader("SOAPAction", soapAction);
            StringEntity data = new StringEntity(soapXml,Charset.forName("UTF-8"));
            httpPost.setEntity(data);
            CloseableHttpResponse response = closeableHttpClient
                    .execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                // 打印响应内容
                retStr = EntityUtils.toString(httpEntity, "UTF-8");
               /* Map<String,Object> map1 = ((Map<String, Object>) xml2map(retStr).get("Body"));

                System.out.println(map1.toString());*/

            }
            // 释放资源
            closeableHttpClient.close();
        } catch (Exception e) {
            logger.error("exception in doPostSoap1_1:{}", e);
        }
        return StringEscapeUtils.unescapeXml(retStr);
    }

    /**
     * 将xml格式响应报文解析为Map格式
     * @param responseXmlTemp
     * @return
     * @throws DocumentException
     */
    public static Map<String, Object> xml2map(String responseXmlTemp) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(responseXmlTemp);
        } catch (DocumentException e) {
            logger.error("parse text error : ", e);
        }
        if(doc == null){
            return new HashMap<String,Object>();
        }
        Element rootElement = doc.getRootElement();
        Map<String,Object> mapXml = new HashMap<String,Object>();
        element2Map(mapXml,rootElement);
        return mapXml;
    }

    /**
     * 使用递归调用将多层级xml转为map
     * @param map
     * @param rootElement
     */
    public static void element2Map(Map<String, Object> map, Element rootElement) {

        //获得当前节点的子节点
        List<Element> elements = rootElement.elements();
        if (elements.isEmpty()) {
            //没有子节点说明当前节点是叶子节点，直接取值
            map.put(rootElement.getName(),rootElement.getText());
        }else if (elements.size() == 1) {
            //只有一个子节点说明不用考虑list的情况，继续递归
            Map<String,Object> tempMap = new HashMap<String,Object>();
            element2Map(tempMap,elements.get(0));
            map.put(rootElement.getName(),tempMap);
        }else {
            //多个子节点的话就要考虑list的情况了，特别是当多个子节点有名称相同的字段时
            Map<String,Object> tempMap = new HashMap<String,Object>();
            for (Element element : elements) {
                tempMap.put(element.getName(),null);
            }
            Set<String> keySet = tempMap.keySet();
            for (String string : keySet) {
                Namespace namespace = elements.get(0).getNamespace();
                List<Element> sameElements = rootElement.elements(new QName(string,namespace));
                //如果同名的数目大于1则表示要构建list
                if (sameElements.size() > 1) {
                    List<Map> list = new ArrayList<Map>();
                    for(Element element : sameElements){
                        Map<String,Object> sameTempMap = new HashMap<String,Object>();
                        element2Map(sameTempMap,element);
                        list.add(sameTempMap);
                    }
                    map.put(string,list);
                }else {
                    //同名的数量不大于1直接递归
                    Map<String,Object> sameTempMap = new HashMap<String,Object>();
                    element2Map(sameTempMap,sameElements.get(0));
                    map.put(string,sameTempMap);
                }
            }
        }
    }

    public static void main(String[] args) {
        StringBuffer xml = new StringBuffer();
//
//        xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.motosoft.com\">\n");
//        xml.append("   <soapenv:Header/>\n");
//        xml.append("   <soapenv:Body>\n");
//        xml.append("      <web:Audit_GcVolPriService>\n");
//        xml.append("         <I_REQUEST>\n");
//        xml.append("            <BASEINFO>\n");
//        xml.append("               <MSGID>1</MSGID>\n");
//        xml.append("               <PMSGID>1</PMSGID>\n");
//        xml.append("               <RETRY>1</RETRY>\n");
//        xml.append("               <SENDTIME>1</SENDTIME>\n");
//        xml.append("               <SERVICENAME>If_gcxc2volumeprice_Service</SERVICENAME>\n");
//        xml.append("               <s_PROVINCE>1</s_PROVINCE>\n");
//        xml.append("               <s_SYSTEM>1</s_SYSTEM>\n");
//        xml.append("               <t_PROVINCE>1</t_PROVINCE>\n");
//        xml.append("               <t_SYSTEM>1</t_SYSTEM>\n");
//        xml.append("            </BASEINFO>\n");
//        xml.append("            <MESSAGE>\n");
//        xml.append("               <IFCODE>gcxc2volumeprice_iteminfo</IFCODE>\n");
//        xml.append("               <IFINFO><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        xml.append("   <si_gcxc_volumeprice_syn>\n");
//        xml.append("   	<bedgetid>CSZGZ00022</bedgetid>\n");
//        xml.append("   </si_gcxc_volumeprice_syn>]]>\n");
//        xml.append("               </IFINFO>\n");
//        xml.append("            </MESSAGE>\n");
//        xml.append("         </I_REQUEST>\n");
//        xml.append("      </web:Audit_GcVolPriService>\n");
//        xml.append("   </soapenv:Body>\n");
//        xml.append("</soapenv:Envelope>\n");
//
//        String postUrl = "http://10.140.11.99:9083/budgetsj/services/If_gcxc2volumeprice_Service?wsdl";
//
//        System.out.println(doPostSoap(postUrl, xml.toString(), ""));


        xml.append("        <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
        xml.append("    <soapenv:Header/>\n");
        xml.append("    <soapenv:Body>\n");
        xml.append("       <ns:Audit_GcVolPriServiceResponse xmlns:ns=\"http://webservice.motosoft.com\">\n");
        xml.append("          <return xsi:type=\"ax21:AuditGcxcReturnInfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ax21=\"http://webservice.motosoft.com/xsd\">\n");
        xml.append("             <BASEINFO>\n");
        xml.append("                <MSGID>1</MSGID>\n");
        xml.append("                <PMSGID>1</PMSGID>\n");
        xml.append("                <SENDTIME>20180730220801</SENDTIME>\n");
        xml.append("                <S_PROVINCE>1</S_PROVINCE>\n");
        xml.append("                <S_SYSTEM>1</S_SYSTEM>\n");
        xml.append("                <SERVICENAME>If_gcxc2volumeprice_Service</SERVICENAME>\n");
        xml.append("                <T_PROVINCE>1</T_PROVINCE>\n");
        xml.append("                <T_SYSTEM>1</T_SYSTEM>\n");
        xml.append("                <RETRY>1</RETRY>\n");
        xml.append("             </BASEINFO>\n");
        xml.append("             <MESSAGE>\n");
        xml.append("                <RESULT>0</RESULT>\n");
        xml.append("                <REMARK>处理成功</REMARK>\n");
        xml.append("                <XMLDATA><si_gcxc_volumeprice_syn><xmbh>001</xmbh><gcmc>[江苏]量价审计用、户测试专用-(备用)</gcmc><gcbh>17JS000511001</gcbh><htbh>CSZGZ00022</htbh><fxmc>[江苏]量价审计用测试专用(备用)111</fxmc><zylb>通信线路工程</zylb><gclx>光缆线路</gclx><shkbz>南京市</shkbz><zydh>TXL</zydh><gcrq>2018-06-13 09:34:44</gcrq><jsgm>建设规模</jsgm><bzr>在</bzr><jsdw></jsdw><bzrphone>15638169557</bzrphone><sgdw>施工单位</sgdw><sjdw>设计单位</sjdw><jldw>监理单位</jldw><sjfz>设计负责</sjfz><cs>参审</cs><sgfz>施工负责</sgfz><sh>审核</sh><remark1></remark1><remark2></remark2></si_gcxc_volumeprice_syn></XMLDATA>\n");
        xml.append("             </MESSAGE>\n");
        xml.append("          </return>\n");
        xml.append("       </ns:Audit_GcVolPriServiceResponse>\n");
        xml.append("    </soapenv:Body>\n");
        xml.append(" </soapenv:Envelope>\n");

        Document doc= null;
        try {
            doc = (Document) DocumentHelper.parseText(xml.toString());

            Element MESSAGE = doc.getRootElement().element("Body").element("Audit_GcVolPriServiceResponse").element("return").element("MESSAGE");

            System.out.println("MESSAGE"+MESSAGE.element("XMLDATA").asXML());






        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

}
