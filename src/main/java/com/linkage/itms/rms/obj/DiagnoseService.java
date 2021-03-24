
package com.linkage.itms.rms.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.cao.SuperGatherCorba;
import com.linkage.itms.rms.obj.dao.DiagnoseDao;
import com.linkage.itms.rms.obj.domain.DeviceWireInfoObj;
import com.linkage.itms.rms.obj.domain.PONInfoOBJ;
import com.linkage.itms.rms.obj.domain.VoiceServiceProfileObj;

/**
 * @author Reno (Ailk NO.)
 * @version 1.0
 * @since 2015年3月23日
 * @category com.linkage.itms.rms.obj
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class DiagnoseService
{

	private static Logger logger = LoggerFactory.getLogger(DiagnoseService.class);

	/**
	 * 根据用户的业务账号查询用户信息 移植自:com.linkage.itms.dao.UserDeviceDAO.queryUserInfo(int, String)
	 * 
	 * @param userType
	 *            :用户信息类型 username:业务号码
	 * @author Jason(3412)
	 * @date 2010-6-22
	 * @return ArrayList<HashMap<String,String>>
	 */
	public List<HashMap<String, String>> queryUserInfo(int userType, String username)
	{
		logger.debug("queryUserInfo({})", username);
		List<HashMap<String, String>> resultList = null;
		if (StringUtil.IsEmpty(username))
		{
			logger.error("username is Empty");
			return null;
		}
		// String table_customer = "tab_hgwcustomer";
		// String table_serv_info = "hgwcust_serv_info";
		// String table_voip = "tab_voip_serv_param";
		StringBuffer psql = new StringBuffer();
		// b.username 宽带账号
		// b.vlanid pvc/vlan配置
		// b.bind_port 绑定端口
		// voip_port 语音端口
		psql.append("select a.user_id,a.username loid,b.username,a.device_id,a.device_serialnumber,a.city_id,b.vlanid,b.bind_port,c.voip_port,b.serv_type_id ");
		psql.append("from tab_hgwcustomer a left join hgwcust_serv_info b on a.user_id = b.user_id ");
		psql.append("left join tab_voip_serv_param c on b.user_id=c.user_id ");
		switch (userType)
		{
		// 宽带账号
			case 1:
				psql.append("where b.username='" + username + "'");
				break;
			// 设备SN
			case 6:
				psql.append("where a.device_serialnumber like '%" + username + "'");
				break;
			// VOIP 电话
			case 4:
				psql.append("where c.voip_phone='" + username + "'");
				// 逻辑SN(LOID)
			case 2:
				psql.append("where a.username='" + username + "'");
				break;
		}
		PrepareSQL sql = new PrepareSQL(psql.toString());
		return DBOperation.getRecords(sql.getSQL());
	}

	/**
	 * 通过设备id, 查询厂商/软件版本/硬件版本
	 * 
	 * @param deviceId
	 *            设备id
	 * @return 查询厂商/软件版本/硬件版本
	 */
	public Map<String, String> queryDeviceInfoByDeviceId(String deviceId)
	{
		String sql = "select b.vendor_name, c.softwareversion, c.hardwareversion "
				+ "from tab_gw_device a, tab_vendor b, tab_devicetype_info c "
				+ "where a.devicetype_id = c.devicetype_id and b.vendor_id = c.vendor_id and a.device_id = '"
				+ deviceId + "'";
		PrepareSQL psql = new PrepareSQL(sql);
		return DBOperation.getRecord(psql.getSQL());
	}

	public Map<String, Object> gatherByDeviceId(String deviceId)
	{
		int rsint = new SuperGatherCorba().getCpeParams(deviceId, 0, 3);
		// 采集失败
		if (rsint != 1)
		{
			return null;
		}
		// 采集成功获取需要的信息
		else
		{
			return this.getAllChannel(deviceId);
		}
	}

	public Map<String, Object> getAllChannel(String device_id)
	{
		DiagnoseDao dao = new DiagnoseDao();
		List<HashMap<String, String>> list = dao.getAllChannel(device_id);
		List<HashMap<String, String>> internet_list = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> iptv_list = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> voip_list = new ArrayList<HashMap<String, String>>();
		List<HashMap<String, String>> tr069_list = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = null;
		String servList = null;
		for (int i = 0; i < list.size(); i++)
		{
			map = list.get(i);
			if (map == null || map.size() == 0)
				continue;
			servList = (String) map.get("serv_list");
			if (StringUtil.IsEmpty(servList))
				continue;
			else
			{
				if (servList.toUpperCase().indexOf("INTERNET") != -1)
				{
					internet_list.add(map);
				}
				// iptv
				else if (servList.toUpperCase().indexOf("IPTV") != -1)
				{
					iptv_list.add(map);
				}
				else if (servList.toUpperCase().indexOf("VOIP") != -1
						|| servList.toUpperCase().indexOf("VOICE") != -1)
				{
					voip_list.add(map);
				}
				else if (servList.toUpperCase().indexOf("TR069") != -1)
				{
					tr069_list.add(map);
				}
			}
		}
		// Map<String, List<Map>> data = new HashMap<String, List<Map>>();
		// data.put("INTERNET", internet_list);
		// data.put("IPTV", iptv_list);
		// data.put("TR069", tr069_list);
		// data.put("VOIP", voip_list);
		// 宽带
		List<WanConnSessObj> wideNetInfoList = null;
		if (internet_list != null && !internet_list.isEmpty())
		{
			for (HashMap<String, String> internet : internet_list)
			{
				WanConnSessObj[] wanArray = dao.queryDevWanConnSession(internet);
				wideNetInfoList = Arrays.asList(wanArray);
			}
		}
		// iptv 忽略
		// voip
		List<Map> voipInfoList = null;
		if (voip_list != null && !voip_list.isEmpty())
		{
			for (HashMap<String, String> voip : voip_list)
			{
				WanConnSessObj[] voipArray = dao.queryDevWanConnSession(voip);
				voipInfoList = this.getVoipInfoIR(voipArray, voip);
			}
		}
		// 线路信息
		DeviceWireInfoObj[] wireInfoObjArr = null;
		PONInfoOBJ[] ponInfoOBJArr = null;
		String accessType = String.valueOf(dao.getAccessType(device_id));
		logger.warn("accessType is :" + accessType);
		if ("1".equals(accessType))
		{// ADSL
			wireInfoObjArr = dao.queryDevWireInfo(device_id);
		}
		else if ("2".equals(accessType))
		{// LAN
			wireInfoObjArr = dao.queryDevWireInfo(device_id);
		}
		else if ("3".equals(accessType))
		{// EPON
			ponInfoOBJArr = dao.queryPONInfo(device_id);
			// pon_type = "EPON";
		}
		else if ("4".equals(accessType))
		{// GPON
			ponInfoOBJArr = dao.queryPONInfo(device_id);
			// pon_type = "GPON";
		}
		else
		{
			// 线路
			wireInfoObjArr = dao.queryDevWireInfo(device_id);
		}
		// lan
		List lanInfoList = dao.queryLanEth(device_id);
		List wlanInfoList = dao.getData(device_id);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("wideNetInfoList", wideNetInfoList);
		data.put("voipInfoList", voipInfoList);
		data.put("lanInfoList", lanInfoList);
		data.put("wireInfoObjArr", wireInfoObjArr);
		data.put("ponInfoOBJArr", ponInfoOBJArr);
		data.put("wlanInfoList", wlanInfoList);
		return data;
	}

	public List<Map> getVoipInfoIR(WanConnSessObj[] wanConnSess, Map map)
	{
		// 返回值
		List<Map> rs = new ArrayList<Map>();
		DiagnoseDao dao = new DiagnoseDao();
		VoiceServiceProfileObj[] voipProfJ = dao.getVoipProf(map);
		// VoiceServiceProfileObj[] voipProfJ =
		// deviceInfoDAO.getVoipProf(StringUtil.getStringValue(map.get("device_id")));
		if (null != voipProfJ)
		{
			for (int i = 0; i < voipProfJ.length; i++)
			{
				logger.debug("voipProfJ[i].getDeviceId({})", voipProfJ[i].getDeviceId());
				logger.debug("voipProfJ[i].getProfId({})", voipProfJ[i].getProfId());
				VoiceServiceProfileLineObj[] voipProfLine = dao
						.getVoipProfLine(voipProfJ[i]);
				if (null != voipProfLine)
				{
					for (int j = 0; j < voipProfLine.length; j++)
					{
						Map<String, String> oneVoip = new HashMap<String, String>();
						if (null != wanConnSess && 0 < wanConnSess.length)
						{
							oneVoip.put("pvc", wanConnSess[0].getPvc());
							oneVoip.put("vlanid", wanConnSess[0].getVlanid());
							// oneVoip.put("connType",Global.G_Src_Key_Map.get("5").get(wanConnSess[0].getConnType()));
							// oneVoip.put("status",Global.G_Src_Key_Map.get("6").get(wanConnSess[0].getStatus()));
							oneVoip.put("ip", wanConnSess[0].getIp());
							oneVoip.put("status", wanConnSess[0].getStatus());
						}
						oneVoip.put("prox_serv", voipProfJ[i].getProxServ());
						oneVoip.put("prox_serv_2", voipProfJ[i].getProxServ2());
						oneVoip.put("username", voipProfLine[j].getUsername());
						oneVoip.put("password", voipProfLine[j].getPassword());
						oneVoip.put("regist_status", voipProfLine[j].getStatus());
						oneVoip.put("line", voipProfLine[j].getLineId());
						rs.add(oneVoip);
					}
				}
				else
				{
					Map<String, String> oneVoip = new HashMap<String, String>();
					if (null != wanConnSess)
					{
						if (0 < wanConnSess.length)
						{
							oneVoip.put("pvc", wanConnSess[0].getPvc());
							oneVoip.put("vlanid", wanConnSess[0].getVlanid());
							// oneVoip.put("connType",Global.G_Src_Key_Map.get("5").get(wanConnSess[0].getConnType()));
							// oneVoip.put("status",Global.G_Src_Key_Map.get("6").get(wanConnSess[0].getStatus()));
						}
					}
					oneVoip.put("prox_serv", voipProfJ[i].getProxServ());
					oneVoip.put("prox_serv_2", voipProfJ[i].getProxServ2());
					rs.add(oneVoip);
				}
			}
		}
		return rs;
	}
}
