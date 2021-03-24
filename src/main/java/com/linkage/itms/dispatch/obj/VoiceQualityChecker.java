
package com.linkage.itms.dispatch.obj;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;

public class VoiceQualityChecker extends BaseChecker
{

	private static Logger logger = LoggerFactory.getLogger(VoiceQualityChecker.class);
	
	private  String statTime;   //生成记录的时间，UTC时间
	private  String txPackets;//发送包数
	private  String rxPackets;//接收包数
	private  String meanDelay;//平均时延
	private  String meanJitter;//平均抖动
	private  String fractionLoss;//丢包率，单位：%
	private  String localIPAddress;//本端IP地址
	private  String localUDPPort;//本端端口
	private  String farEndIPAddress;//远端IP地址
	private  String farEndUDPPort;//远端端口
	private  String mosLq;//Mos值,单位0.1，可选
	private  String codec;//编解码

	/**
	 * 构造方法
	 * 
	 * @param inXml
	 *            接口调用入参，xml字符串
	 */
	public VoiceQualityChecker(String inXml)
	{
		this.callXml = inXml;
	}

	/**
	 * 检查接口调用字符串的合法性
	 */
	@Override
	public boolean check()
	{
		logger.debug("check()");
		SAXReader reader = new SAXReader();
		Document document = null;
		try
		{
			document = reader.read(new StringReader(callXml));
			Element root = document.getRootElement();
			cmdId = root.elementTextTrim("CmdID");
			cmdType = root.elementTextTrim("CmdType");
			clientType = StringUtil.getIntegerValue(root.elementTextTrim("ClientType"));
			Element param = root.element("Param");
			devSn = param.elementTextTrim("DevSn");
			oui = param.elementTextTrim("OUI");
			cityId = param.elementTextTrim("CityId");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = 1;
			resultDesc = "数据格式错误";
			return false;
		}
		// 参数合法性检查
		if (false == baseCheck() || false == devSnCheck() 	|| false == ouiCheck())
		{
			return false;
		}
		result = 0;
		resultDesc = "成功";
		return true;
	}

	@Override
	public String getReturnXml()
	{
		logger.debug("getReturnXml()");
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		// 接口调用唯一ID
		root.addElement("CmdID").addText(StringUtil.getStringValue(cmdId));
		// 结果代码
		root.addElement("RstCode").addText("" + result);
		// 结果描述
		root.addElement("RstMsg").addText(resultDesc);
		root.addElement("DevSn").addText(devSn == null ? "" : devSn);
		root.addElement("StatTime").addText(statTime == null ? "" : statTime);
		root.addElement("TxPackets").addText(txPackets == null ? "" : txPackets);
		root.addElement("RxPackets").addText(rxPackets == null ? "" : rxPackets);
		root.addElement("MeanDelay").addText(meanDelay == null ? "" : meanDelay);
		root.addElement("MeanJitter").addText(meanJitter == null ? "" : meanJitter);
		root.addElement("FractionLoss").addText(fractionLoss == null ? "" : fractionLoss);
		root.addElement("LocalIPAddress").addText(localIPAddress == null ? "" : localIPAddress);
		root.addElement("LocalUDPPort").addText(localUDPPort == null ? "" : localUDPPort);
		root.addElement("FarEndIPAddress").addText(farEndIPAddress == null ? "" : farEndIPAddress);
		root.addElement("FarEndUDPPort").addText(farEndUDPPort == null ? "" : farEndUDPPort);
		root.addElement("MosLq").addText(mosLq == null ? "" : mosLq);
		root.addElement("Codec").addText(codec == null ? "" : codec);
		return document.asXML();
	}

	
	public String getStatTime()
	{
		return statTime;
	}

	
	public void setStatTime(String statTime)
	{
		this.statTime = statTime;
	}

	
	public String getTxPackets()
	{
		return txPackets;
	}

	
	public void setTxPackets(String txPackets)
	{
		this.txPackets = txPackets;
	}

	
	public String getRxPackets()
	{
		return rxPackets;
	}

	
	public void setRxPackets(String rxPackets)
	{
		this.rxPackets = rxPackets;
	}

	
	public String getMeanDelay()
	{
		return meanDelay;
	}

	
	public void setMeanDelay(String meanDelay)
	{
		this.meanDelay = meanDelay;
	}

	
	public String getMeanJitter()
	{
		return meanJitter;
	}

	
	public void setMeanJitter(String meanJitter)
	{
		this.meanJitter = meanJitter;
	}

	
	public String getFractionLoss()
	{
		return fractionLoss;
	}

	
	public void setFractionLoss(String fractionLoss)
	{
		this.fractionLoss = fractionLoss;
	}

	
	public String getLocalIPAddress()
	{
		return localIPAddress;
	}

	
	public void setLocalIPAddress(String localIPAddress)
	{
		this.localIPAddress = localIPAddress;
	}

	
	public String getLocalUDPPort()
	{
		return localUDPPort;
	}

	
	public void setLocalUDPPort(String localUDPPort)
	{
		this.localUDPPort = localUDPPort;
	}

	
	public String getFarEndIPAddress()
	{
		return farEndIPAddress;
	}

	
	public void setFarEndIPAddress(String farEndIPAddress)
	{
		this.farEndIPAddress = farEndIPAddress;
	}

	
	public String getFarEndUDPPort()
	{
		return farEndUDPPort;
	}

	
	public void setFarEndUDPPort(String farEndUDPPort)
	{
		this.farEndUDPPort = farEndUDPPort;
	}

	
	public String getMosLq()
	{
		return mosLq;
	}

	
	public void setMosLq(String mosLq)
	{
		this.mosLq = mosLq;
	}

	
	public String getCodec()
	{
		return codec;
	}

	
	public void setCodec(String codec)
	{
		this.codec = codec;
	}

	
}
