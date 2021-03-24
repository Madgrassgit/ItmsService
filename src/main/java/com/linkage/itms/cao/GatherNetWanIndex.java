
package com.linkage.itms.cao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.util.StringUtil;
import com.linkage.itms.obj.ParameValueOBJ;

/**
 * 快速采集公共类
 * 
 * @author banyr (Ailk No.)
 * @version 1.0
 * @since 2018-8-7
 * @category com.linkage.itms.cao
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class GatherNetWanIndex
{

	private static final Logger logger = LoggerFactory.getLogger(GatherNetWanIndex.class);
	/** 快速采集服务类型INTERNET */
	private static final String GETHER_INTERNET = "internet";
	/** 快速采集服务类型OTHER */
	private static final String GETHER_OTHER = "other";
	/** 快速采集服务类型VOIP */
	private static final String GETHER_VOIP = "voiP";
	/** 快速采集服务类型TRZSN  */
	private static final String GETHER_TRZSN = "tr069";
	
	/**
	 * 快速采集格式:
	 * "1.1;DHCP_Routed;45;TR069","3.1;Bridged;43;OTHER","4.1;DHCP_Routed;42;VOIP","5.1;PPPoE_Routed;312;INTERNET"
	 * 
	 * WAN Index实例举例(“1.2; Bridged;46;VOIP”, “2.1; PPPoE_Routed;1001;VOIP”)
	 * 示例说明:示例中包含两条WAN连接索引信息。
	 *   索引信息中的第一个字段”1.2”表示WAN连接的实例号；
	 *   第二个字段为接口类型，可选范围为Bridged，PPPoE_Routed，DHCP_Routed，STATIC_Routed；
	 *   通过第一个和第二个字段可以判断出WAN连接路径为InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.2
	 *   第三个参数为PVC号或VLAN ID
	 *   第四个参数为servicelist，可选范围为”TR069”,”INTERNET”,”VOIP”,”OTHER” ，有多个时用逗号分割
	 * 
	 * 返回类型为    Map: <"internet", List<i + "##" + j>> 
	 * 			     <"other", List<i + "##" + j>> 
	 * 			     <"voiP", List<i + "##" + j>> 
	 * 			     <"tr069", List<i + "##" + j>> 
	 * 
	 * i,j即为WAN连接路径中InternetGatewayDevice.WANDevice.1.WANConnectionDevice.i.WANPPPConnection.j
	 * 
	 * @param deviceId
	 * @return
	 */
	
	public static Map<String, List<String>> gatherNetIJList(String deviceId)
	{
		HashMap<String, List<String>> ijMap = new HashMap<String, List<String>>();
		List<String> ijInternetList = new ArrayList<String>();
		List<String> ijVoipList = new ArrayList<String>();
		List<String> ijTrzsnList = new ArrayList<String>();
		List<String> ijOtherList = new ArrayList<String>();
		String wan_index = "InternetGatewayDevice.WANDevice.1.X_CT-COM_WANIndex";
		ArrayList<ParameValueOBJ> wan_index_result_list = null;
		logger.warn("[{}]获取wan连接索引", deviceId);
		wan_index_result_list = new ACSCorba().getValue(deviceId, wan_index);
		if (!wan_index_result_list.isEmpty())
		{
			String wan_index_result = "";
			wan_index_result = wan_index_result_list.get(0).getValue()
					.replaceAll("\\(", "").replaceAll("\\)", "");
			if (!StringUtil.IsEmpty(wan_index_result))
			{
				String wan[] = wan_index_result.replace("\"", "").split(",");
				for (String wanPa : wan)
				{
					if (wanPa.endsWith("INTERNET") || wanPa.endsWith("internet"))
					{
						if (wanPa.contains(".") && wanPa.contains(";"))
						{
							String i = wanPa.split(";")[0].split("\\.")[0];
							String j = wanPa.split(";")[0].split("\\.")[1];
							ijInternetList.add((i + "##" + j));
						}
					}
					if (wanPa.endsWith("OTHER") || wanPa.endsWith("other"))
					{
						if (wanPa.contains(".") && wanPa.contains(";"))
						{
							String i = wanPa.split(";")[0].split("\\.")[0];
							String j = wanPa.split(";")[0].split("\\.")[1];
							ijOtherList.add((i + "##" + j));
						}
					}
					if (wanPa.endsWith("VOIP") || wanPa.endsWith("voip"))
					{
						if (wanPa.contains(".") && wanPa.contains(";"))
						{
							String i = wanPa.split(";")[0].split("\\.")[0];
							String j = wanPa.split(";")[0].split("\\.")[1];
							ijVoipList.add((i + "##" + j));
						}
					}
					if (wanPa.endsWith("TR069") || wanPa.endsWith("tr069"))
					{
						if (wanPa.contains(".") && wanPa.contains(";"))
						{
							String i = wanPa.split(";")[0].split("\\.")[0];
							String j = wanPa.split(";")[0].split("\\.")[1];
							ijTrzsnList.add((i + "##" + j));
						}
					}
				}
			}
		}
		ijMap.put(GETHER_INTERNET, ijInternetList);
		ijMap.put(GETHER_OTHER, ijOtherList);
		ijMap.put(GETHER_VOIP, ijVoipList);
		ijMap.put(GETHER_TRZSN, ijTrzsnList);
		return ijMap;
	}
	
}
