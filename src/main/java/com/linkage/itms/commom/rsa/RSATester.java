
package com.linkage.itms.commom.rsa;

import java.util.Map;

/**
 * 数据在RSA加密后，密文是一串数字，先用Base64编码后再进行传输。接收方收到数据后，先进行Base64解码，然后再RSA解密
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2013-7-8
 * @category com.linkage.itms.commom.rsa
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class RSATester
{

	static String publicKey;
	static String privateKey;
	static
	{
		try
		{
			Map<String, Object> keyMap = RSAUtils.genKeyPair();
			//publicKey = RSAUtils.getPublicKey(keyMap);
			//privateKey = RSAUtils.getPrivateKey(keyMap);
			publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRaiW7YgQjrpJI4XOILDeyi25S3Hi4Wu90Zze6JIoFod0T5SqHSmnWqR/2kHBbA6pGzenQYxAobcLgoqUPLCVQkVU7O4YKhWerjsssnRout3JdcwRyArg7GdYvV+paizhoLxDENOFsGtOpQ7vCT7w9WVg6t4S6+LhQ1q7VmwXXKQIDAQAB";
			privateKey ="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJFqJbtiBCOukkjhc4gsN7KLblLceLha73RnN7okigWh3RPlKodKadap"
+"H/aQcFsDqkbN6dBjEChtwuCipQ8sJVCRVTs7hgqFZ6uOyyydGi63cl1zBHICuDsZ1i9X6lqLOGgvEMQ04Wwa06lDu8JPvD1ZWDq3hLr4uFDWrtWbBdcpAgMBAAECgYBOztZQ"
+"LZjMtcm2EemJPV2aOBK8eZw7XZ002tPK4A6lasnkBGj+FYgyvEoEWNdrnBrmcKZtN6MEJihdkggepvA5XkvP5Vi8fouFerw0aPrMODu8aXIStrC/lRWlNo9YXxs0HdqHcqQA"
+"wVsj+N5fD2IWxuXkByTdLLk/J6DIl6gkAQJBAM6PNvlpSGYC2VHrcs/n6CQyqqj/t3PE+//uitRMIK9cn1sDtSuJIVbalSL6gzwIMOGlzl+SVdomFzc5TpM+3MECQQC0OFP3"
+"UlOQIpynkDQBWzwwVPTJtlZsu3jV2yvbci5Ht2CR3bWyaR51LhpIfsON/mprPMYdNnnO6NyJl3WVtExpAkA14hYtKdHBCq02OX1RzuU3zB/IxixM/u3d7ZQ5nUJJfJf1Xho8"
+"qSERTyPoZAEn/9sAggg71iifJ6CWv1RIBPYBAkEAoHWSlR9Q1YFrt2mWVCrWYTYTKVHWi0Mz6D0XRHDaMfJc4iLj9vDPNIgS0L9FADUorUhUIIhzVb1RNpSLFfqNGQJBALM/"
+"2/9o04qtRL5zXP4hxyJDjHaWussFWPNjjq1rKAF++LEg6r/lwYI0F2bd1Ubsh+8Plc8eX1fHZPynhEmCLGc=";
			System.out.println("公钥:\n" + publicKey);
			System.out.println("私钥:\n" + privateKey);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception
	{
		test();
//		String str = "aaaaaaaa";
//		String[] st = str.split(",");
//		System.out.println(st.length);
//		System.out.println(st[0]);
	}

	static void test() throws Exception
	{
		System.out.println("公钥加密——私钥解密");
		String source = "<RadiusInfo><userId>18070079208</userId><password >887120</password><userType >1</userType></RadiusInfo>";
		System.out.println("\r加密前文字：\r\n" + source);
		byte[] data = source.getBytes();
		// RSA加密
		byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
		// 加密后用Base64编码输出
		String base64en = RSAUtils.Base64encode(encodedData);
		System.out.println("加密后文字：\r\n" + base64en);
		// Base64编码传输
		// Base64解码
		byte[] base64de = RSAUtils.Base64decode(base64en);
		// RSA解密
		byte[] decodedData = RSAUtils.decryptByPrivateKey(base64de, privateKey);
		String target = new String(decodedData);
		System.out.println("解密后文字: \r\n" + target);
	}
}
