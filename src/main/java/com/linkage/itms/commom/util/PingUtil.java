package com.linkage.itms.commom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class PingUtil {
	private static final Logger logger = LoggerFactory.getLogger(PingUtil.class);

	public static HashMap<String, String> getPingResult(HashMap<String, String> parMap) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		try {
			Runtime rt = Runtime.getRuntime();  
			StringBuffer command = new StringBuffer();
			boolean falt = false;
			if(System.getProperty("os.name").contains("Linux")){
				// 包大小(byte)
				command.append("ping -s ").append(parMap.get("size"))
				// 包数目
				.append(" -c ").append(parMap.get("num"))
				// 超时时间(ms)
				.append(" -w ").append(parMap.get("timeOut"))
				.append(" ").append(parMap.get("ip"));
				logger.info("ping command: " + command.toString());
				final Process process = rt.exec(command.toString());
				// 设置ping命令超时时间
				setTimeOut(process, Integer.parseInt(parMap.get("num")));
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
				String line = null;
				
				StringBuffer result = new StringBuffer();
				while ((line = in.readLine()) != null) {
					result.append(line).append("\n");
					// ping命令执行错误  解析结果直接返回
					if (line.contains("errors")) {
						String data[] = line.split(" ");
						String failNum = String.valueOf(Integer.parseInt(data[0]) - Integer.parseInt(data[3]));
						map.put("succesNum", data[3]);
						map.put("failNum", failNum);
						map.put("packetLossRate", data[7]);
						map.put("result", "-1");
						map.put("resultDesc", "执行ping指令出错");
						logger.info("ping command result is:" + result.toString());
						return map;
					}
					// ping命令执行正确
					if (line.contains("packets transmitted")) {
						String data[] = line.split(" ");
						String failNum = String.valueOf(Integer.parseInt(data[0]) - Integer.parseInt(data[3]));
						map.put("succesNum", data[3]);
						map.put("failNum", failNum);
						map.put("packetLossRate", data[5]);
						falt = true;
					}
					if (line.contains("rtt min/avg/max/mdev")) {
						String data[] = line.split(" ");
						String times[] = data[3].split("/");
						map.put("minResponseTime", times[0]);
						map.put("avgResponseTime", times[1]);
						map.put("maxResponseTime", times[2]);
						falt = true;
					}
				}
				logger.info("ping command result is:" + result.toString());
			}
			if (falt) {
				map.put("result", "0");
				map.put("resultDesc", "成功");
			}
			else {
				map.put("result", "-1");
				map.put("resultDesc", "执行ping指令超时");
			}
		} catch (Exception e) {
			map.put("result", "-1");
			map.put("resultDesc", "执行ping指令出错");
			logger.info("getPingResult is error", e);
		}
		return map;
	}
	
	/**
	 * 设置超时时间
	 * @param process
	 * @param times
	 */
	private static void setTimeOut(final Process process, final int times) {
		Thread thread = new Thread(new Runnable() { 
			public void run() {
				try {
					Thread.sleep(1000 * times);
					process.destroy();
				} catch (InterruptedException e) {
					logger.error("setTimeOut error:",e);
					Thread.currentThread().interrupt();
				}
			}
		});
		thread.start();
	}
}
