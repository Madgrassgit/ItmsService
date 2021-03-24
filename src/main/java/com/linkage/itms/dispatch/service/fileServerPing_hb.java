package com.linkage.itms.dispatch.service;

import com.linkage.commons.util.StringUtil;
import org.apache.commons.net.telnet.TelnetClient;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class fileServerPing_hb implements IService{

	private static Logger logger = LoggerFactory.getLogger(fileServerPing_hb.class);

	@Override
	public String work(String inXml) {
		logger.warn("fileServerPing_hb inParam:[{}]",inXml);
		List<Map<String, String>> list=new ArrayList<Map<String, String>>();
		
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(inXml));
			Element root = document.getRootElement();
			List<Element> param = root.elements("param");
			
			for (int i = 0; i < param.size(); i++) {
				Map<String, String> map=new HashMap<String, String>();
				String id = StringUtil.getStringValue(param.get(i).elementTextTrim("id"));
				String server_name = StringUtil.getStringValue(param.get(i).elementTextTrim("server_name"));
				String host = StringUtil.getStringValue(param.get(i).elementTextTrim("host"));
				String fileserverport = StringUtil.getStringValue(param.get(i).elementTextTrim("fileserverport"));
				map.put("id", id);
				map.put("server_name", server_name);
				map.put("host", host);
				map.put("fileserverport", fileserverport);
				list.add(map);
			}
			
			int num=list.size();
			 final CountDownLatch countDownLatch = new CountDownLatch(num);
				for (int i = 0; i < num; i++) {
					final Map<String,String> map=(Map<String,String>) list.get(i);
					new Thread() {
						public void run() {
							try {
								Thread.sleep(1000);
								String host=String.valueOf(map.get("host"));
								String fileserverport=String.valueOf(map.get("fileserverport"));
								boolean isOnline = isConnect(host, Integer.parseInt(fileserverport));
					   			 if (isOnline) {
					   				map.put("isonline", "1");
					   			}else {
					   				map.put("isonline", "0");
					   			}
							} catch (InterruptedException e) {
								e.printStackTrace();
								// Restore interrupted state...
								Thread.currentThread().interrupt();
							}finally {
								countDownLatch.countDown();
							}
						}
					}.start();
				}
				try {
					countDownLatch.await(10,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					// Restore interrupted state...
					Thread.currentThread().interrupt();
				}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Document document1 = DocumentHelper.createDocument();
		document1.setXMLEncoding("GBK");
		Element root1 = document1.addElement("root");
		for (int i = 0; i < list.size(); i++) {
			  Element paramElem = root1.addElement("param");
		      Map<String, String> map = list.get(i);
		      for (Map.Entry<String, String> m : map.entrySet()) {
		    	  paramElem.addElement(m.getKey().toLowerCase()).addText(m.getValue());
		      }
		}
		return document1.asXML();
	}

	public boolean isConnect(String host,int port) {
		
		TelnetClient telnet;  
		telnet = new TelnetClient();  
        try {
        	telnet.connect(host, port);
        	boolean connected = telnet.isConnected();
            return connected;
        } catch (IOException e) {
        	e.printStackTrace();
			//当连不通时，直接抛异常，异常捕获即可
        	logger.warn("telnet异常false");
            return false;
        }finally{
            try {
            	telnet.disconnect();  
            } catch (IOException e1) {
            	e1.printStackTrace();
            	logger.warn("telnet异常");
            }
        }
	}
	
	public static void main(String[] args) {
		TelnetClient telnet;  
		telnet = new TelnetClient();  
        try {
        	telnet.connect("192.168.26.128", 1521);
        	boolean connected = telnet.isConnected();
        } catch (IOException e) {
        	e.printStackTrace();
            System.out.println("连接异常false");//当连不通时，直接抛异常，异常捕获即可
        }finally{
            try {
            	telnet.disconnect();  
            } catch (IOException e1) {
            	e1.printStackTrace();
            }
        }
		/*
		String returnXml="";
		List<Map<String, String>> list=new ArrayList<Map<String, String>>();
		for (int i = 0; i < 5; i++) {
			Map<String, String> map=new HashMap<String, String>();
			map.put("server_name", "1");
			map.put("host", "2");
			map.put("port", "3");
			map.put("fileserverport", "4");
			list.add(map);
		}
		for (int i = 0;i < list.size(); i++) {
			Map<String,String> map=(Map<String,String>) list.get(i);
			map.put("isOnline", "1");
		}
		
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		
		for (int i = 0; i < list.size(); i++) {
			  Element paramElem = root.addElement("param");
		      Map<String, String> map = list.get(i);
		      for (Map.Entry<String, String> m : map.entrySet()) {
		    	  paramElem.addElement(m.getKey().toLowerCase()).addText(m.getValue());
		      }
		      
		}
		String asXML = document.asXML();
		System.out.println(asXML);
	*/}
}
