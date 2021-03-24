package com.linkage.itms.commom.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * FTP工具类
 * 
 * @author chenjie
 * @version 1.0
 * @since 1.0
 * @date 2011-11-10
 */


/**
 * FTP工具类
 * 
 * @author chenjie
 * @version 1.0
 * @since 1.0
 * @date 2011-11-10
 */

public class FtpClient {
	
	//	logger
	private static Logger log = LoggerFactory.getLogger(FtpClient.class);
	
	private String server;

	private String username;

	private String password;

	private FTPClient ftp;

	private boolean binaryTransfer = true;

	/**
	 * @param server
	 *            ftp服务器地址
	 * @param username
	 *            ftp服务器登陆用户
	 * @param password
	 *            ftp用户密码
	 */
	public FtpClient(String server, String username, String password) {

		this.server = server;
		this.username = username;
		this.password = password;
		ftp = new FTPClient();
		/*
		 * if(Configuration.PrintFTPCommandLog){ //打印FTP命令
		 * ftp.addProtocolCommandListener(new PrintCommandListener()); }
		 */
	}

	/**
	 * 根据配置文件构建一个FtpClient
	 */
	public FtpClient() {
		/*
		this(Configuration.FtpServer, Configuration.FtpUser,
				Configuration.FtpPassword);
				*/
	}

	public boolean connect() {
		log.warn("FtpClient connect to server: ip[{}], username:[{}], password:[{}]", new Object[]{server, username, password});
		try {
			int reply;
			ftp.connect(server);

			// 连接后检测返回码来校验连接是否成功
			reply = ftp.getReplyCode();

			if (FTPReply.isPositiveCompletion(reply)) {
				if (ftp.login(username, password)) {
					// passive mode off
					ftp.enterLocalActiveMode();
					log.warn("connect to server, success!");
					return true;
				}
			} else {
				ftp.disconnect();
				log.error("FTP server refused connection.");
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}
			log.error("Could not connect to server.", e);
		}
		return false;
	}

	/**
	 * 下载一个文件到默认的本地路径中
	 * 
	 * @param fileName
	 *            文件名称(不含路径)
	 * @param delFile
	 *            成功后是否删除该文件
	 * @return
	 */
	/*
	public boolean get(String fileName, boolean delFile) {
		String remote = Configuration.RemoteDownPath + fileName;
		String local = Configuration.LocalDownPath + fileName;
		return get(remote, local, delFile);
	}
	*/

	/**
	 * 上传一个文件到默认的远程路径中
	 * 
	 * @param fileName
	 *            文件名称(不含路径)
	 * @param delFile
	 *            成功后是否删除该文件
	 * @return
	 */
	/*
	public boolean put(String fileName, boolean delFile) {
		String remote = Configuration.RemoteUpPath + fileName;
		String local = Configuration.LocalUpPath + fileName;
		return put(remote, local, delFile);
	}
	*/

	/**
	 * 上传多个文件到默认的远程路径中
	 * 
	 * @param fileNames
	 *            文件名数组
	 * @param delFile
	 *            成功后是否删除文件
	 * @return
	 */
	/*
	public boolean[] put(String[] fileNames, boolean delFile) {
		boolean[] result = new boolean[fileNames.length];
		for (int j = 0; j < result.length; j++) {
			result[j] = false;
		}
		String remoteFile;
		String localFile;
		for (int i = 0; i < fileNames.length; i++) {
			localFile = fileNames[i];
			result[i] = put(localFile, delFile);
		}
		return result;
	}
	*/

	/**
	 * 上传一个本地文件到远程指定文件
	 * 
	 * @param remoteAbsoluteFile
	 *            远程文件名(包括完整路径)
	 * @param localAbsoluteFile
	 *            本地文件名(包括完整路径)
	 * @return 成功时，返回true，失败返回false
	 */
	public boolean put(String remoteAbsoluteFile, String localAbsoluteFile,
			boolean delFile) {
		
		log.warn("FtpClient put [{}] to [{}] , delFile[{}]", new Object[]{localAbsoluteFile, remoteAbsoluteFile, delFile} );
		
		InputStream input = null;
		try {
			// //设置文件传输类型
			if (binaryTransfer) {
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			} else {
				ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
			}
			// 处理传输
			input = new FileInputStream(localAbsoluteFile);
			OutputStream os = ftp.storeFileStream(remoteAbsoluteFile);
			uploadFile(input, os);
			
			log.debug("put " + localAbsoluteFile);
			input.close();
			if (delFile) {
				(new File(localAbsoluteFile)).delete();
			}
			log.debug("delete " + localAbsoluteFile);
			return true;
		} catch (FileNotFoundException e) {
			log.error("local file not found.", e);
		} catch (IOException e1) {
			log.error("Could put file to server.", e1);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (Exception e2) {
			}
		}

		return false;
	}

	/**
	 * 下载一个远程文件到本地的指定文件
	 * 
	 * @param remoteAbsoluteFile
	 *            远程文件名(包括完整路径)
	 * @param localAbsoluteFile
	 *            本地文件名(包括完整路径)
	 * @return 成功时，返回true，失败返回false
	 */
	public boolean get(String remoteAbsoluteFile, String localAbsoluteFile,
			boolean delFile) {
		log.warn("FtpClient get [{}] to [{}] , delFile[{}]", new Object[]{remoteAbsoluteFile, localAbsoluteFile, delFile} );
		
		OutputStream output = null;
		boolean result;
		try {
			// 设置文件传输类型
			if (binaryTransfer) {
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			} else {
				ftp.setFileType(FTPClient.ASCII_FILE_TYPE);
			}
			// 处理传输
			output = new FileOutputStream(localAbsoluteFile);
			result = ftp.retrieveFile(remoteAbsoluteFile, output);
			output.close();
			if (delFile) { // 删除远程文件
				ftp.deleteFile(remoteAbsoluteFile);
			}
			return result;
		} catch (FileNotFoundException e) {
			log.error("local file not found.", e);
		} catch (IOException e1) {
			log.error("Could get file from server.", e1);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e2) {
			}
		}
		return false;
	}

	/**
	 * 列出远程目录下所有的文件
	 * 
	 * @param remotePath
	 *            远程目录名
	 * @return 远程目录下所有文件名的列表，目录不存在或者目录下没有文件时返回0长度的数组
	 */
	public String[] listNames(String remotePath) {
		log.warn("FtpClient listNames, remotePath[{}]", remotePath);
		String[] fileNames = null;
		try {
			FTPFile[] remotefiles = ftp.listFiles(remotePath);
			fileNames = new String[remotefiles.length];
			for (int i = 0; i < remotefiles.length; i++) {
				fileNames[i] = remotefiles[i].getName();
			}

		} catch (IOException e) {
			log.error("Could not list file from server.", e);
		}
		return fileNames;
	}

	/**
	 * 断开ftp连接
	 */
	public void disconnect() {
		try {
			ftp.logout();
			if (ftp.isConnected()) {
				ftp.disconnect();
			}
		} catch (IOException e) {
			log.error("Could not disconnect from server.", e);
		}
	}
	
    private boolean uploadFile(InputStream srcFileStream,
			OutputStream desFileStream) throws IOException
	{
		try
		{
			int poi = 0;
			byte[] buffered = new byte[1024];
			while ((poi = srcFileStream.read(buffered)) != -1)
			{
				desFileStream.write(buffered, 0, poi);
			}
		}
		catch (Exception e)
		{
			log.error("文件上传发生异常,异常信息如下", e);
			return false;
		}
		finally
		{
			if (srcFileStream != null)
			{
				srcFileStream.close();
			}
			if (desFileStream != null)
			{
				desFileStream.flush();
				desFileStream.close();
			}
		}
		return true;
	}

	/**
	 * @return Returns the binaryTransfer.
	 */
	public boolean isBinaryTransfer() {
		return binaryTransfer;
	}

	/**
	 * @param binaryTransfer
	 *            The binaryTransfer to set.
	 */
	public void setBinaryTransfer(boolean binaryTransfer) {
		this.binaryTransfer = binaryTransfer;
	}

	public static void main(String[] args) {
		FtpClient ftp = new FtpClient("202.102.39.141", "linkage", "JSNet@)!@Test");
	//	FtpClient ftp = new FtpClient("202.102.39.141", "linkage", "JSNet@)!!Test");
		ftp.connect();
//		String[] temp = ftp.listNames("/export/home/linkage/chenjie5");
//		System.out.println("connect sucess");
//		System.out.println(temp.length);
//		for(String s : temp)
//		{
//			System.err.println(s);
//		}
//		boolean result = ftp.put("/export/home/linkage/chenjie5/test.txt", "\\d:/test.txt", false);
		boolean result = ftp.put( "/export/home/linkage/chenjie6/classes.jar",  "\\d:/classes.jar", true);
		
		System.err.println(result);
		
		ftp.disconnect();
	}
}