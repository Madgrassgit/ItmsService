package com.linkage.itms.dispatch.sxdx.obj;

import com.linkage.commons.util.StringUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * 甘肃电信恢复出厂设置接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月14日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class CpeFactoryResetDealXML extends BaseDealXML {
	private static Logger logger = LoggerFactory.getLogger(CpeFactoryResetDealXML.class);
	SAXReader reader = new SAXReader();

	private String index = "";
	private String type = "";
	/**
	 * 1：表示只对终端恢复出厂操作，不修改此终端对应的数据库数据，包括工单状态以及终端状态。
	 * 2：表示终端恢复出厂之后，作废此终端对应的工单，并把其状态改为入网。执行了此操作后，该终端即可作为新的终端进行放号。
     * 3：表示终端恢复出厂之后，把此终端对应的非作废工单状态改为正在执行，并把其状态改为绑定。执行了此操作后，即可在不重新下发工单的情况下允许此终端重新注册。
	 */
	private String resetType = "";
	
	public CpeFactoryResetDealXML(String methodName){
		super(methodName);
	}

	public Document getXML(String inXml) {
		this.inXml = inXml;
		try 
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			resetType = StringUtil.getStringValue(inRoot.elementTextTrim("resetType"));
			
			/**
			 * 0：逻辑ID，即激活码
			   1：宽带帐号，即Order结构中的ad_account字段。
			   2：Device ID(OUI-SN)
			   3：Device ID(OUI-SN)
			 */
			if( StringUtil.IsEmpty(type))
			{
				this.result ="-99";
				this.errMsg ="查询类型type为空";
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return null;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type)){
				this.result ="-99";
				this.errMsg ="查询类型type范围非法";
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return null;
			}
			else if(StringUtil.IsEmpty(index)){
				this.result ="-99";
				this.errMsg ="查询值index为空";
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return null;
			}
			else if(StringUtil.IsEmpty(resetType) && !"1".equals(resetType) && !"2".equals(resetType) && !"3".equals(resetType)){
				this.result ="-99";
				this.errMsg ="恢复出厂设置类型type非法，not in（1，2，3）";
				logger.warn(methodName+"["+opId+"]恢复出厂设置类型type非法，not in（1，2，3）");
				return null;
			}
			
			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return null;
		}
	}

	public Document getXML1(String inXml) {
		this.inXml = inXml;
		try
		{
			logger.warn(methodName+"["+opId+"]入参校验开始");
			Document inDocument = reader.read(new StringReader(inXml));
			Element inRoot = inDocument.getRootElement();
			opId = StringUtil.getStringValue(inRoot.elementTextTrim("op_id"));
			type = StringUtil.getStringValue(inRoot.elementTextTrim("type"));
			index = StringUtil.getStringValue(inRoot.elementTextTrim("index"));
			resetType = StringUtil.getStringValue(inRoot.elementTextTrim("resetType"));

			/**
			 * 0：逻辑ID，即激活码
			 * 1：宽带帐号，即Order结构中的ad_account字段。
			 * 2：电话号码，即Order结构中的vector_argues里语音号码值。
			 * 3：Device ID(OUI-SN)。
			 * 4：CPE IP(如10.135.107.133格式)。
			 * 5:SN。
			 */
			if( StringUtil.IsEmpty(type))
			{
				this.result ="-99";
				this.errMsg ="查询类型type为空";
				logger.warn(methodName+"["+opId+"]查询类型type为空");
				return null;
			}
			else if(!"0".equals(type) && !"1".equals(type) && !"2".equals(type) && !"3".equals(type)){
				this.result ="-99";
				this.errMsg ="查询类型type范围非法";
				logger.warn(methodName+"["+opId+"]查询类型type范围非法：{}", type);
				return null;
			}
			else if(StringUtil.IsEmpty(index)){
				this.result ="-99";
				this.errMsg ="查询值index为空";
				logger.warn(methodName+"["+opId+"]查询值index为空");
				return null;
			}
			else if(StringUtil.IsEmpty(resetType) && !"1".equals(resetType) && !"2".equals(resetType) && !"3".equals(resetType) && !"4".equals(resetType) && !"5".equals(resetType)){
				this.result ="-99";
				this.errMsg ="恢复出厂设置类型type非法，not in（1，2，3）";
				logger.warn(methodName+"["+opId+"]恢复出厂设置类型type非法，not in（1，2，3）");
				return null;
			}

			return inDocument;
		} catch (Exception e) {
			logger.error(methodName+"["+opId+"] Excetion occured!", e);
			return null;
		}
	}

	public String getIndex()
	{
		return index;
	}

	public void setIndex(String index)
	{
		this.index = index;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getResetType()
	{
		return resetType;
	}
	
	public void setResetType(String resetType)
	{
		this.resetType = resetType;
	}

	
}
