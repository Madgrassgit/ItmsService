package com.linkage.itms.dispatch.cqdx.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alcatel.asb.hdm.report.webservice.OUIMager.entity.IParam;
import com.alcatel.asb.hdm.report.webservice.OUIMager.entity.OUIRes;
import com.alcatel.asb.hdm.report.webservice.OUIMager.entity.OperType;
import com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Request;
import com.alcatel.asb.hdm.report.webservice.OUIMager.entity.Response;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.cqdx.dao.OUIoperateDao;
import com.linkage.itms.dispatch.cqdx.dao.PublicDAO;
import com.linkage.itms.dispatch.cqdx.obj.FeedbackWorkTicketsInfoDealXML;

/**
 * 
 * @author yaoli (Ailk No.)
 * @version 1.0
 * @since 2019年6月25日
 * @category com.linkage.itms.dispatch.cqdx.service
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 *
 */
public class OperateOuiService
{
	private static Logger logger = LoggerFactory.getLogger(OperateOuiService.class);
	
	public Response managerOuiSrevice(Request request){
		
		logger.warn("OperateOuiService ==> managerOuiSrevice 入参{}",new Object[]{request.toString()});
		
		Response response = new Response(-99, "");
		if(null == request || null == request.getInterface_param() || StringUtil.IsEmpty(request.getInterface_param().getOperation_type())
				|| StringUtil.IsEmpty(request.getInterface_param().getOui()) || StringUtil.IsEmpty(request.getInterface_param().getDevice_model())){
			logger.warn("OperateOuiService ==> managerOuiSrevice{参数校验出错}");
			response.setResult_desc("入口参数为空,校验失败");
			return response;
		}
		
		OUIRes ouiInfo = new OUIRes(request.getInterface_param().getOui(), request.getInterface_param().getVendor_name(), request.getInterface_param().getDevice_model(), request.getInterface_param().getName());
		response.setOui_info(ouiInfo);
		
		IParam param = request.getInterface_param();
		String oui = param.getOui();
		OUIoperateDao dao = new OUIoperateDao();
		int currDate = (int)(new Date().getTime()/1000);
		if(OperType.OPERATOR_ADD.getIndex().equals(param.getOperation_type())){
			
			if(StringUtil.IsEmpty(param.getName()) || StringUtil.IsEmpty(param.getVendor_name())){
				response.setResult_desc("新增时参数校验失败");
				return response;
			}
			
			if(dao.chekOuiIn(param.getDevice_model(), oui)){
				response.setResult_desc("当前oui和型号已存在");
				return response;
			}
			String id = dao.getId();
			int num = 1;
			boolean isNum = id.matches("[0-9]+");
			if(isNum){
				num = Integer.parseInt(id)+1;
			}
			int res = dao.addOUI(num, oui, param.getName(), param.getVendor_name(), "", Integer.valueOf(currDate).toString(), param.getDevice_model());
			if(res > 0){
				response.setResult_code(0);
				response.setResult_desc("新增oui成功");
			}else{
				response.setResult_code(-1);
				response.setResult_desc("新增oui入库失败");
			}
		}else
			if(OperType.OPERATOR_DEL.getIndex().equals(param.getOperation_type())){
				int res = dao.delOUI(oui,param.getName(),param.getDevice_model(),param.getVendor_name());
				if(res > 0){
					response.setResult_code(0);
					response.setResult_desc("删除oui成功");
				}else{
					response.setResult_code(-1);
					response.setResult_desc("删除oui失败");
				}
			}else 
				if(OperType.OPERATOR_QUERY.getIndex().equals(param.getOperation_type())){
					//查询
					List<HashMap<String,String>> ouiList = dao.qryOUI(oui, param.getDevice_model(), param.getVendor_name(), param.getName());
					if(null == ouiList || ouiList.isEmpty() || ouiList.size() < 1){
						response.setResult_code(0);
						response.setResult_desc("不存在指定条件下的数据");
					}else{
						if(ouiList.size() > 1){
							response.setResult_desc("查询数据大于1");
						}else{
							HashMap<String,String> ouiHash = ouiList.get(0);
							ouiInfo = new OUIRes(oui, ouiHash.get("vendor_name"), ouiHash.get("device_model"), ouiHash.get("vendor_add"));
							response.setOui_info(ouiInfo);
							response.setResult_code(0);
							response.setResult_desc("查询成功");
						}
					}
				}else{
					response.setResult_desc("不存在的操作类型["+param.getOperation_type()+"]");
				}
		
		//入记录表
		FeedbackWorkTicketsInfoDealXML deal = new FeedbackWorkTicketsInfoDealXML();
		deal.recordLog("OperateOuiService", "", "", request.toString(), response.toString());
		logger.warn("OperateOuiService ==> managerOuiSrevice 回参{}",new Object[]{response.toString()});
		return response;
	}
}
