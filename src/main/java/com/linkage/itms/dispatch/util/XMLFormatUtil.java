package com.linkage.itms.dispatch.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
/**
 * Created by wpj on 2017/5/8.
 */
public class XMLFormatUtil {
    /***
     *XML格式字符串转换成MAP,不包括List
     * @param xml
     * @return
     */
    public static Map xmlToMap(String xml) {
        try {
            Map map = new HashMap();
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            org.dom4j.Element nodeElement = document.getRootElement();
            List node = nodeElement.elements();
            for (Iterator it = node.iterator(); it.hasNext();) {
                org.dom4j.Element elm = (org.dom4j.Element) it.next();
                map.put(elm.getName(), elm.getText());
                elm = null;
            }
            node = null;
            nodeElement = null;
            document = null;
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /***
     * XML格式字符串转换成list
     * @param xml
     * @return
     */
    public static List xmlToList(String xml) {
        try {
            List<Map> list = new ArrayList<Map>();
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            org.dom4j.Element nodesElement = document.getRootElement();
            List nodes = nodesElement.elements();
            for (Iterator its = nodes.iterator(); its.hasNext();) {
                org.dom4j.Element nodeElement = (org.dom4j.Element) its.next();
                Map map = xmlToMap(nodeElement.asXML());
                list.add(map);
                map = null;
            }
            nodes = null;
            nodesElement = null;
            document = null;
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /***
     * XML格式字符串转换成map,包括List
     * @param xml
     * @return
     */
    public static Map xmlToListMap(String xml) {
        try {
            Map reMap = new HashMap();
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            org.dom4j.Element nodesElement = document.getRootElement();
            List nodes = nodesElement.elements();
            for (Iterator its = nodes.iterator(); its.hasNext();) {
                org.dom4j.Element nodeElement = (org.dom4j.Element) its.next();
                if(nodeElement.isTextOnly()){
                    reMap.put(nodeElement.getName(), nodeElement.getText());
                }else{
                    List<Map> reList = xmlToList(nodeElement.asXML());
                    reMap.put(nodeElement.getName(),reList);
                    reList = null;
                }
            }
            nodes = null;
            nodesElement = null;
            document = null;
            return reMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /***
     * List<Map>转xml格式String
     * @param list
     * @param rootName
     * @return
     */
    public static String list2xml(List<?> list,String rootName) {
        org.dom4j.Document document = org.dom4j.DocumentHelper.createDocument();
        org.dom4j.Element nodesElement = document.addElement(rootName);
        list2xml(list,nodesElement);
        return doc2String(document);
    }
    /***
     * list的element转xml格式String
     * @param list
     * @param element
     * @return
     */
    public static org.dom4j.Element list2xml(List list,org.dom4j.Element element){
        int i = 0;
        for (Object o : list) {
            org.dom4j.Element nodeElement = element.addElement("Map");
            if (o instanceof Map) {
                //nodeElement.addAttribute("type", "o");
                Map m = (Map) o;
                for (Iterator iterator = m.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    org.dom4j.Element keyElement = nodeElement.addElement(entry.getKey().toString());
                    if (entry.getValue() instanceof List) {
                        //keyElement.addAttribute("type", "l");
                        list2xml((List) entry.getValue(),keyElement);
                    } else {
                        //keyElement.addAttribute("type", "s");
                        keyElement.setText(entry.getValue().toString());
                    }
                }
            } else if (o instanceof List) {
                //nodeElement.addAttribute("type", "l");
                list2xml((List)o,nodeElement);
            }
            else {
                org.dom4j.Element keyElement = nodeElement.addElement("value");
                keyElement.addAttribute("num", String.valueOf(i));
                keyElement.setText(String.valueOf(o));
            }
            i++;
        }
        return element;
    }
    /***
     * 文本转换String
     * @param document
     * @return
     */
    public static String doc2String(org.dom4j.Document document) {
        String s = "";
        try {
            // 使用输出流来进行转化
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 使用UTF-8编码
            OutputFormat format = new OutputFormat();
            format.setSuppressDeclaration(true);
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            s = out.toString("UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return s;
    }
}
