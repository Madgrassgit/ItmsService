package com.linkage.itms.dispatch.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * 列出FTP服务器上指定目录下面的所有文件
 */
public class FTPListAllFiles {
    public         FTPClient         ftp;
    public         ArrayList<String> arFiles;

    /**
     * 重载构造函数
     *
     * @param isPrintCommmand 是否打印与FTPServer的交互命令
     */
    public FTPListAllFiles(boolean isPrintCommmand) {
        ftp = new FTPClient();
        arFiles = new ArrayList<String>();
        if (isPrintCommmand) {
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        }
    }

    /**
     * 登陆FTP服务器
     *
     * @param host     FTPServer IP地址
     * @param port     FTPServer 端口
     * @param username FTPServer 登陆用户名
     * @param password FTPServer 登陆密码
     * @return 是否登录成功
     * @throws IOException
     */
    public boolean login(String ftpHost, int ftpPort, String ftpUserName, String ftpPassword) throws IOException {
        try {
        	this.ftp.connect(ftpHost, ftpPort);// 连接FTP服务器
        	this.ftp.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(this.ftp.getReplyCode())) {
                System.out.println("未连接到FTP，用户名或密码错误。");
                this.ftp.disconnect();
            } else {
                System.out.println("FTP连接成功。");
                this.ftp.setControlEncoding("GBK");
                return true;
            }
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("FTP的IP地址可能错误，请正确配置。");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FTP的端口错误,请正确配置。");
        }
        return false;
    }

    /**
     * 关闭数据链接
     *
     * @throws IOException
     */
    public void disConnection() throws IOException {
        if (this.ftp.isConnected()) {
            this.ftp.disconnect();
        }
    }

    /**
     * 递归遍历出目录下面所有文件
     *
     * @param pathName 需要遍历的目录，必须以"/"开始和结束
     * @throws IOException
     */
    public void List(String pathName) throws IOException {
        if (pathName.startsWith("/") && pathName.endsWith("/")) {
            //更换目录到当前目录
            this.ftp.changeWorkingDirectory(pathName);
            this.ftp.enterLocalPassiveMode();
            FTPFile[] files = this.ftp.listFiles();
            for (FTPFile file : files) {
                if (file.isFile()) {
                    arFiles.add(pathName + file.getName());
                } else if (file.isDirectory()) {
                  List(pathName + file.getName() + "/");
                }
            }
        }
    }

    /**
     * 递归遍历目录下面指定的文件名
     *
     * @param pathName 需要遍历的目录，必须以"/"开始和结束
     * @param ext      文件的扩展名
     * @throws IOException
     */
    public void List(String pathName, String ext) throws IOException {
        if (pathName.startsWith("/") && pathName.endsWith("/")) {
            //更换目录到当前目录
            this.ftp.changeWorkingDirectory(pathName);
            this.ftp.enterLocalPassiveMode();
            FTPFile[] files = this.ftp.listFiles();
            for (FTPFile file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith(ext)) {
                        arFiles.add(pathName + file.getName());
                    }
                } else if (file.isDirectory()) {
                  List(pathName + file.getName() + "/", ext);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FTPListAllFiles f = new FTPListAllFiles(true);
        if (f.login("IP", 7000, "userName", "password")) {
            f.List("/", "tar");
        }
        f.disConnection();
        for (String arFile : f.arFiles) {
            System.out.println(arFile);
        }

    }
}