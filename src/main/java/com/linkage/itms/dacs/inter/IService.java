package com.linkage.itms.dacs.inter;


/**
 * @author Jason(3412)
 * @date 2009-5-25
 */
public interface IService {

	/*** DACS调用xml字符串 */
	String callXmlStr = "<?xml version=\"1.0\"  encoding=\"GBK\"?><root><functionCode>configQos</functionCode><paramDoc><commandId>800</commandId><param>[1]$$[2]</param></paramDoc></root>";
	
	/*** ITMS回复结果 */
	String returnXmlStr = "<?xml version=\"1.0\" encoding=\"GBK\" ?><root><returnCode>resultcoode</returnCode><returnMessage>errmessage</returnMessage></root>";
	
	/*** ITMS与DACS接口文档定义*/
	String configQosCall (String strParamXML);
}
