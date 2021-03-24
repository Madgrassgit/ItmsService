
package com.linkage.itms.radius.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.rsa.RSAUtils;

/**
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.radius.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RadiusPasswordChecker
{

	private static final Logger logger = LoggerFactory
			.getLogger(RadiusPasswordChecker.class);
	/**
	 * <Pre>
	 * 用户wifi上网帐号，不带域名
	 * 各省保证账号在省内的唯一性，
	 * 当用户修改账号时，此处填旧账号
	 * 
	 * </pre>
	 */
	private String username;
	/*
	 * 用户wifi上网密码
	 */
	private String password;
	/**
	 * 请求的原始xml报文
	 */
	private String syncRequest;
	/**
	 * 原始请求xml报文经过加密，该字段保持解密后的原始报文
	 */
	private String decodeRequest;

	public RadiusPasswordChecker(String syncRequest)
	{
		this.syncRequest = syncRequest;
	}

	public boolean check()
	{
		return decode() && analize() && doCheck();
	}

	/**
	 * 对原始请求xml报文进行解码
	 */
	private boolean decode()
	{
		logger.info("before decode, sync request is [{}]", syncRequest);
		if (!StringUtil.IsEmpty(syncRequest))
		{
			try
			{
				byte[] base64de = RSAUtils.Base64decode(syncRequest);
				// RSA解密
				byte[] decodedData = RSAUtils.decryptByPrivateKey(base64de,
						Global.RSA_PRIVATE_KEY);
				decodeRequest = new String(decodedData);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				return false;
			}
		}
		logger.warn("after decode, source sync request is [{}]", decodeRequest);
		return true;
	}

	/**
	 * 对解码后的xml报文进行解析，获取请求参数
	 */
	private boolean analize()
	{
		if (!StringUtil.IsEmpty(decodeRequest))
		{
			try
			{
				SAXReader reader = new SAXReader();
				Document doc = reader.read(new StringReader(decodeRequest));
				Element root = doc.getRootElement();
				username = root.elementTextTrim("userId");
				password = root.elementTextTrim("password");
				return true;
			}
			catch (DocumentException e)
			{
				logger.warn("analize sync request[{}] error.", decodeRequest);
				logger.warn(e.getMessage(), e);
				return false;
			}
			catch (Exception e)
			{
				logger.warn("analize sync request[{}] error.", decodeRequest);
				logger.warn(e.getMessage(), e);
				return false;
			}
		}
		return false;
	}

	/**
	 * 对用户请求参数进行校验
	 */
	private boolean doCheck()
	{
		if (StringUtil.IsEmpty(username))
		{
			logger.warn("username is empty");
			return false;
		}
		if (StringUtil.IsEmpty(password))
		{
			logger.warn("password is empty");
			return false;
		}
		return true;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
