package com.linkage.itms.dispatch.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping {

	public static void ping(String ipAddress) throws Exception {
		String line = null;
		try {
			Process pro = Runtime.getRuntime().exec("ping " + ipAddress);
			BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream(),"GBK"));
			while ((line = buf.readLine()) != null)
				System.out.println(line);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public static boolean isOnline(String hostname) {
		try {
			InetAddress ia;
			boolean isonline = false;
			ia = InetAddress.getByName(hostname);// 锟斤拷锟界：www.baidu.com
			isonline = ia.isReachable(1500); // 锟斤拷时时锟斤拷1.5锟斤拷
			System.out.println("Name: " + ia.getHostName());
			return isonline;
		} catch (UnknownHostException e) {
			System.out.println("address:" + hostname + " is not unknown");
		} catch (IOException e) {
			System.out.println("address:" + hostname + " is not reachable");
		}
		return false;
	}

	public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
		BufferedReader in = null;
		String pingCommand = null;
		Runtime r = Runtime.getRuntime();
		String osName = System.getProperty("os.name");
		if (osName.contains("Windows")) {
			// 锟斤拷要执锟叫碉拷ping锟斤拷锟斤拷,锟斤拷锟斤拷锟斤拷锟斤拷windows锟斤拷式锟斤拷锟斤拷锟斤拷
			pingCommand = "ping " + ipAddress + " -n " + pingTimes + " -w " + timeOut;
		} else {
			// 锟斤拷要执锟叫碉拷ping锟斤拷锟斤拷,锟斤拷锟斤拷锟斤拷锟斤拷Linux锟斤拷式锟斤拷锟斤拷锟斤拷
			// -c:锟斤拷锟斤拷,-w:锟斤拷时时锟斤拷(锟斤拷位/ms) ping -c 10 -w 0.5 192.168.120.206
			pingCommand = "ping " + " -c  " + pingTimes + " -w " + timeOut +"  "+ ipAddress;
		}
		try {
			// 执锟斤拷锟斤拷锟筋并锟斤拷取锟斤拷锟�
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			int connectedCount = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				connectedCount += getCheckResult(line, osName);
			}
			return connectedCount >= 2 ? true : false;
		} catch (Exception ex) {
			ex.printStackTrace(); // 锟斤拷锟斤拷锟届常锟津返回硷拷
			return false;
		} finally {
			try {
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 锟斤拷line锟斤拷锟斤拷=18 ms ttl=64锟斤拷锟斤拷,说锟斤拷锟窖撅拷ping通,锟斤拷锟斤拷1,锟斤拷t锟斤拷锟斤拷0.
	private static int getCheckResult(String line, String osName) {
		if (osName.contains("Windows")) {
			if (line.contains("TTL=")) {
				return 1;
			}
		} else {
			if (line.contains("ttl=")) {
				return 1;
			}
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		String ipAddress = "www.baidu.com";
		long currentTimeMillis = System.currentTimeMillis();
		//System.out.println(isOnline(ipAddress));
		//ping(ipAddress);
        System.out.println(ping(ipAddress, 3, 3000));
        System.out.println(System.currentTimeMillis()-currentTimeMillis);
	}
}
