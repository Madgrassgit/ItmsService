package com.linkage.itms.dispatch.sxdx.service;

import com.linkage.itms.cao.ACSCorba;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.sxdx.beanObj.NorthQueryParaResult;
import com.linkage.itms.dispatch.sxdx.beanObj.Para;
import com.linkage.itms.dispatch.sxdx.dao.PublicDAO;
import com.linkage.itms.dispatch.sxdx.obj.NorthQueryCPEParaDealXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * 甘肃电信北向查询终端 OUI-SN等信息接口
 * @author fanjm 35572
 * @version 1.0
 * @since 2019年6月11日
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class NorthQueryCPEParaService extends ServiceFather {
	public NorthQueryCPEParaService(String methodName)
	{
		super(methodName);
	}

	private static Logger logger = LoggerFactory.getLogger(NorthQueryCPEParaService.class);
	private ACSCorba corba = new ACSCorba();
	private NorthQueryParaResult result = new NorthQueryParaResult();
	private NorthQueryCPEParaDealXML dealXML;
	
	public NorthQueryParaResult work(String inXml) {
		logger.warn(methodName+"执行，入参为：{}",inXml);
		
		dealXML = new NorthQueryCPEParaDealXML(methodName);
		// 验证入参
		if (null == dealXML.getXML(inXml)) {
			logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证没通过[{}]", dealXML.returnXML());
			result.setIOpRst(-1);
			return result;
		}
		logger.warn(methodName+"["+dealXML.getOpId()+"]入参验证通过.");
		
		PublicDAO dao = new PublicDAO();

		ArrayList<Para> paraList = dao.queryUserDevByOui_sn(StringUtil.getIntegerValue(dealXML.getType()), dealXML.getIndex());
		logger.warn(methodName+"["+dealXML.getOpId()+"],根据条件查询结果{}",paraList.toString());
		
		if(null == paraList || paraList.size()==0){
			result.setIOpRst(0);
		}else{
			result.setIOpRst(1);
			Para[] array = (Para[])paraList.toArray(new Para[paraList.size()]);
			result.setParaList(array);
		}
		
		return result;
	}

	
}
