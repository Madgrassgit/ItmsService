package com.linkage.itms.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dao.TerminalVersionAuditDAO;
import com.linkage.itms.dispatch.obj.TerminalVersionAuditChecker;

/**
 * JSDX_ITMS-REQ-20170224-WJY-001（终端审核版本信息同步接口Service)
 * 
 * @author fanjm(35572)
 * @date 2017-3-7
 */
public class TerminalVersionAuditService implements IService {

	// 日志记录
	private static Logger logger = LoggerFactory
			.getLogger(TerminalVersionAuditService.class);

	@Override
	public String work(String inXml) {
		TerminalVersionAuditChecker checker = new TerminalVersionAuditChecker(inXml);
		if (false == checker.check()) {
			logger.error(
					"servicename[TerminalVersionAuditChecker]cmdId[{}]ReturnXml[{}]验证未通过，返回：{}",
					new Object[] { checker.getCmdId(), checker.getReturnXml() });
			return checker.getReturnXml();
		}
		logger.warn(
				"servicename[TerminalVersionAuditChecker]cmdId[{}]inXml[{}]参数校验通过，入参为：{}",
				new Object[] { checker.getCmdId(), inXml });
		
		// 全部类型参数是否校验通过
		boolean allCheck = true;
		TerminalVersionAuditDAO dao = new TerminalVersionAuditDAO();
		// 1.厂商是否存在
		String vendor_id = dao.qryVendor(checker.getVendor_name());
		if(StringUtil.IsEmpty(vendor_id)){
			checker.setMyresult(appendStr(checker.getMyresult(),"1001"));
			checker.setResultDesc(appendStr(checker.getResultDesc(), "查不到对应的厂商信息"));
			allCheck = false;
		}
		
		// 2.型号是否存在
		String device_model_id = dao.qryModel(checker.getDevice_model());
		if(StringUtil.IsEmpty(device_model_id)){
			checker.setMyresult(appendStr(checker.getMyresult(),"1002"));
			checker.setResultDesc(appendStr(checker.getResultDesc(), "查不到对应的型号信息"));
			allCheck = false;
		}
		
		// 3.软件版本是否存在
		if(!dao.isSoftwareversionExist(checker.getSoftwareversion())){
			checker.setMyresult(appendStr(checker.getMyresult(),"1003"));
			checker.setResultDesc(appendStr(checker.getResultDesc(), "查不到对应的软件版本信息"));
			allCheck = false;
		}
		
		// 4.定版原因
		if(checker.getReason().length()>1000){
			checker.setMyresult(appendStr(checker.getMyresult(),"1004"));
			checker.setResultDesc(appendStr(checker.getResultDesc(), "定版原因长度过长"));
			allCheck = false;
		}
		
		// 5.规格是否存在
		/*String spec_id = dao.qrySpec(checker.getSpec_name());
		if(StringUtil.IsEmpty(spec_id)){
			checker.setMyresult(appendStr(checker.getMyresult(),"1000"));
			checker.setResultDesc(appendStr(checker.getResultDesc(), "查不到对应的规格信息"));
			allCheck = false;
		}
		else{
			checker.setSpec_id(spec_id);
		}*/
		
		
		if(allCheck){
			/*boolean res = dao.isDeviceTypeExist(checker.getVendor_id(),checker.getDevice_model_id(), checker.getSoftwareversion(),checker.getHardwareversion());
			if(!res){
				checker.setMyresult("1000");
				checker.setResultDesc("数据库中无对应设备版本资源，请核对厂家、型号、软硬件版本");
			}
			else{*/
				dao.insertDeviceTypeTask(checker);
				checker.setMyresult("0");
				checker.setResultDesc("成功");
			/*}*/
		}
	
		
		// 接口回复XML
		String returnXml = checker.getReturnXml();

		logger.warn(
				"servicename[TerminalVersionAuditChecker]cmdId[{}]returnXml[{}]]处理结束，返回响应信息:{}",
				new Object[] { checker.getCmdId(), returnXml});
		// 回单
		return returnXml;
	}
	
	/**
	 * 拼接错误码或描述，用分号衔接
	 * @param ori 原描述/错误码
	 * @param str 要追加的描述/错误码
	 * @return	拼接结果：描述/错误码
	 */
	private String appendStr(String ori,String str){
		if(StringUtil.IsEmpty(ori)){
			logger.warn("appendStr="+str);
			return str;
		}
		else{
			logger.warn("appendStr="+ori+";"+str);
			return ori+";"+str;
		}
		
	}
	
}
