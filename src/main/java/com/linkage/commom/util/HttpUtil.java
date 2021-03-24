
package com.linkage.commom.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Google (Ailk No.)
 * @version 1.0
 * @since 2021年2月19日
 * @category com.linkage.commom.util
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class HttpUtil
{

	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	private static String  charset = "UTF-8";
	
	
	private HttpUtil()
	{
	}
	
	/**
	 * httpClient的get请求方式 使用GetMethod来访问一个URL对应的网页实现步骤： 1.生成一个HttpClient对象并设置相应的参数；
	 * 2.生成一个GetMethod对象并设置响应的参数； 3.用HttpClient生成的对象来执行GetMethod生成的Get方法； 4.处理响应状态码；
	 * 5.若响应正常，处理HTTP响应内容； 6.释放连接。
	 * 
	 * @param url
	 * @param charset
	 * @return
	 */
	public static String doGet(String url, Map<String, String> headers)
	{
		/**
		 * 1.生成HttpClient对象并设置参数
		 */
		// 获取输入流
		InputStream is = null;
		BufferedReader br = null;
		HttpClient httpClient = new HttpClient();
		// 设置Http连接超时为5秒
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
		/**
		 * 2.生成GetMethod对象并设置参数
		 */
		GetMethod getMethod = new GetMethod(url);
		// 设置get请求超时为5秒
		getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 20000);
		getMethod.addRequestHeader("Content-Type", "application/json;charset=utf-8");
		if (headers != null)
		{
			for (Map.Entry<String, String> e : headers.entrySet())
			{
				getMethod.setRequestHeader(e.getKey(), e.getValue());
			}
		}
		// 设置请求重试处理，用的是默认的重试处理：请求三次
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		StringBuilder response = new StringBuilder();
		/**
		 * 3.执行HTTP GET 请求
		 */
		try
		{
			int statusCode = httpClient.executeMethod(getMethod);
			/**
			 * 4.判断访问的状态码
			 */
			if (statusCode != HttpStatus.SC_OK)
			{
				logger.error("请求出错：{}" , getMethod.getStatusLine());
			}
			is = getMethod.getResponseBodyAsStream();
			br = new BufferedReader(new InputStreamReader(is, charset));
			String str = "";
			while ((str = br.readLine()) != null)
			{
				response.append(str);
			}
		}
		catch (HttpException e)
		{
			// 发生致命的异常，可能是协议不对或者返回的内容有问题
			logger.error("请检查输入的URL!");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// 发生网络异常
			logger.error("发生网络异常!");
		}
		finally
		{
			/**
			 * 6.释放连接
			 */
			// 关闭资源
			if (null != br)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (null != is)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			getMethod.releaseConnection();
		}
		return response.toString();
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param json
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String doPost(String url, JSONObject paramMap,
			Map<String, String> headers)
	{
		// 获取输入流
		InputStream is = null;
		BufferedReader br = null;
		String result = null;
		// 创建httpClient实例对象
		HttpClient httpClient = new HttpClient();
		// 设置httpClient连接主机服务器超时时间：15000毫秒
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(20000);
		// 创建post请求方法实例对象
		PostMethod postMethod = new PostMethod(url);
		// 设置post请求超时时间
		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 20000);
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		postMethod.addRequestHeader("accept", "*/*");
		postMethod.addRequestHeader("connection", "Keep-Alive");
		// 设置json格式传送
		postMethod.addRequestHeader("Content-Type", "application/json;charset=utf-8");
		// 必须设置下面这个Header
		postMethod.addRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
		if (headers != null)
		{
			for (Map.Entry<String, String> e : headers.entrySet())
			{
				postMethod.setRequestHeader(e.getKey(), e.getValue());
			}
		}
		try
		{
			String toJson = paramMap.toString();
			RequestEntity se = new StringRequestEntity(toJson, "application/json",
					charset);
			postMethod.setRequestEntity(se);
			int statusCode = httpClient.executeMethod(postMethod);
			// 判断是否成功
			if (statusCode != HttpStatus.SC_OK)
			{
				logger.error("请求出错：{}" , postMethod.getStatusLine());
			}
			// 获取远程返回的数据
			is = postMethod.getResponseBodyAsStream();
			// 封装输入流
			br = new BufferedReader(new InputStreamReader(is, charset));
			StringBuilder sbf = new StringBuilder();
			String temp = null;
			while ((temp = br.readLine()) != null)
			{
				sbf.append(temp).append("\r\n");
			}
			result = sbf.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			// 关闭资源
			if (null != br)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (null != is)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			// 释放连接
			postMethod.releaseConnection();
		}
		return result;
	}
}
