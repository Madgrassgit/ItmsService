package com.linkage.itms.commom.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

/**
 * @author Jason(3412)
 * @date 2009-10-13
 */
public class SocketUtil {
	/**
	 * 日志记录
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(SocketUtil.class);

	//超时时间，10s
	static final int TIME_OUT = 10*1000;
	
	/**
	 * 向server地址的port端口发送数据mesg，并接收返回的字符串形式的信息返回 异常情况下返回null
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2009-10-13
	 * @return String
	 */
	public static String sendStrMesg(String server, int port, String mesg) {
		logger.debug("sendStrMesg({},{},{})",
				new Object[] { server, port, mesg });
		if(StringUtil.IsEmpty(server) || StringUtil.IsEmpty(mesg)){
			logger.debug("server is null or mesg is null");
			return null;
		}
		String retResult = null;
		Socket socket = null;
		DataOutputStream dos = null;
		BufferedReader dis = null;
		try {
			// 短连接
			socket = new Socket(server, port);
			//设置超时时间
			socket.setSoTimeout(TIME_OUT);
			if(socket.isConnected()){
				dos = new DataOutputStream(socket.getOutputStream());
				dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				dos.write(mesg.getBytes());
				String back = dis.readLine();
				if(null != back){
//					byte[] byteRes = back.getBytes("ISO-8859-1");
//					retResult = new String(byteRes, "gbk");
					retResult = back;
				}else{
					logger.warn("Socket服务器未返回");
				}
			}else{
				logger.warn("无法连接到Socket服务器");
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if(null != dos){
					dos.close();
				}
				if(null != dis){
					dis.close();
				}
				if(null != socket){
					socket.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return retResult;
	}
}
