package com.linkage.itms.ct.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.itms.ct.service.CtBridge2RoutedService;
import com.linkage.itms.ct.service.CtInfoQueryService;
import com.linkage.itms.ct.service.CtRoutedQueryService;
import com.linkage.itms.ct.service.IService;

/**
 * 向网厅提供webservice服务的服务类
 * 
 * @author Jason(3412)
 * @date 2010-7-13
 */
public class CtService {

	private static Logger logger = LoggerFactory.getLogger(CtService.class);

	/**
	 * 2.3. 路由下发支持情况查询
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return String
	 */
	public String ctInfoQuery(String param) {
		logger.info("ctInfoQuery({})", param);
		
		IService serice = new CtInfoQueryService();
		
		return serice.ctWorkService(param);
	}

	/**
	 * 2.4. 桥改路由业务下发
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return String
	 */
	public String ctBridge2Routed(String param) {
		logger.info("ctBridge2Routed({})", param);

		IService serice = new CtBridge2RoutedService();

		return serice.ctWorkService(param);
	}

	/**
	 * 2.5. 桥改路由下发情况查询
	 * 
	 * @param
	 * @author Jason(3412)
	 * @date 2010-7-13
	 * @return String
	 */
	public String ctRoutedQuery(String param) {
		logger.info("ctRoutedQuery({})", param);
		
		IService serice = new CtRoutedQueryService();
		
		return serice.ctWorkService(param);
	}
}
