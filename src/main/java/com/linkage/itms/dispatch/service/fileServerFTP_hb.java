package com.linkage.itms.dispatch.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.dispatch.util.FTPListAllFiles;
import com.linkage.itms.dispatch.util.XMLFormatUtil;

public class fileServerFTP_hb implements IService {

	private static Logger logger = LoggerFactory.getLogger(fileServerFTP_hb.class);

	@Override
	public String work(String inXml) {
		logger.warn("fileServerFTP_hb inParam:[{}]", inXml);
		List<Map<String, String>> fileDirectoryList = new ArrayList<Map<String, String>>();

		int colspanNum=0;
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(new StringReader(inXml));
			Element root = document.getRootElement();
			String host = root.elementText("host");
			String port = root.elementText("port");
			String username = root.elementText("username");
			String password = root.elementText("password");

			FTPListAllFiles f = new FTPListAllFiles(false);
	        try {
				if (f.login(host, Integer.parseInt(port), username, password)) {
				    f.List("/export/home/stb/FileServer/STB/STB/");
				}
			}catch (IOException e) {
				e.printStackTrace();
			}finally {
				f.disConnection();
			}
	        for (String arFile : f.arFiles) {
	        	Map<String, String> map=new LinkedHashMap<String, String>();
	        	String[] split = arFile.split("/STB/STB/");
	        	//文件服务器相对路径
	        	String path=split[1];
	        	String[] paths = path.split("/");
	        	//记录最大路径
	        	if (paths.length>colspanNum) {
	        		colspanNum=paths.length;
				}
	        	for (int i = 0; i < paths.length; i++) {
					map.put("path"+i, paths[i]);
				}
	        	
	        	fileDirectoryList.add(map);
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}

		Document document1 = DocumentHelper.createDocument();
		document1.setXMLEncoding("GBK");
		Element root = document1.addElement("root");
		root.addElement("colspanNum").addText(colspanNum+"");
		
		for (int i = 0; i < fileDirectoryList.size(); i++) {
			Element paramElem = root.addElement("param");
			Map<String, String> map = fileDirectoryList.get(i);
			for (Map.Entry<String, String> m : map.entrySet()) {
				paramElem.addElement(m.getKey().toLowerCase()).addText(m.getValue());
			}
		}

		return document1.asXML();
	}


	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			list.add("/export/home/stb/FileServer/STB/STB/");
		}
		for (int i = 0; i < 10; i++) {
			list.add("/export/home/stb/FileServer/STB/STB/123");
		}
		for (int i = 0; i < 10; i++) {
			list.add("/export/home/stb/FileServer/STB/STB/123/456");
		}
		int count = 0;
		List<Map<String, String>> fileDirectoryList = new ArrayList<Map<String, String>>();

		for (String arFile : list) {
			String[] split = arFile.split("/STB/STB/");
			if (split.length > 1) {
				Map<String, String> map = new HashMap<String, String>();

				String path = split[1];
				String[] paths = path.split("/");
				// 记录最大路径
				if (paths.length > count) {
					count = paths.length;
				}
				for (int i = 0; i < paths.length; i++) {
					map.put("path" + i, paths[i]);
				}
				fileDirectoryList.add(map);
			}
		}

		System.out.println(fileDirectoryList.toString());
		
		Document document1 = DocumentHelper.createDocument();
		document1.setXMLEncoding("GBK");
		Element root = document1.addElement("root");
		root.addElement("colspanNum").addText("5");
		
		for (int i = 0; i < fileDirectoryList.size(); i++) {
			Element paramElem = root.addElement("param");
			Map<String, String> map = fileDirectoryList.get(i);
			for (Map.Entry<String, String> m : map.entrySet()) {
				paramElem.addElement(m.getKey().toLowerCase()).addText(m.getValue());
			}
		}
		
		String asXML = document1.asXML();
		asXML=asXML.split("<colspanNum>")[0]+asXML.split("<colspanNum>")[1].split("</colspanNum>")[1];
		System.out.println(asXML);
		List xmlToList = XMLFormatUtil.xmlToList(asXML);
		System.out.println(xmlToList);
	}
}
