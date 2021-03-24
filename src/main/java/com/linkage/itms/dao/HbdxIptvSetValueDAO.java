package com.linkage.itms.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.commons.util.StringUtil;
import com.linkage.itms.dispatch.obj.HbdxIptvSetValueChecker;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import org.slf4j.Logger;

public class HbdxIptvSetValueDAO {
    private  static final Logger logger  = LoggerFactory.getLogger(HbdxIptvSetValueDAO.class);

    public boolean isExistLoid(String loid) {

        PrepareSQL psql = new PrepareSQL();
        psql.append("select user_id from tab_hgwcustomer where username = ? ");
        psql.setString(1,loid);
        HashMap<String, String> resultMap = (HashMap<String, String>) DBOperation.getRecord(psql.getSQL());
        if(null != resultMap && resultMap.size() > 0)
        {
            logger.warn("[{}] is exist 。",loid);
            return true;
        }
        return false;
    }


    public HashMap<String, String> getItvServInfo(String loid) {
        PrepareSQL psql = new PrepareSQL();
        psql.append("select b.user_id,b.vlanid from tab_hgwcustomer a,hgwcust_serv_info b where a.user_id = b.user_id " +
                " and a.username = ? and b.serv_type_id = 11 and b.open_status = 1 ");
        psql.setString(1,loid);
        return (HashMap<String, String>) DBOperation.getRecord(psql.getSQL());
    }

    public boolean isBindDevice(HbdxIptvSetValueChecker checker) {
        String loid = checker.getUserInfo();
        PrepareSQL psql = new PrepareSQL();
        psql.append("select b.device_id from tab_hgwcustomer a,tab_gw_device b where a.device_id = b.device_id " +
                " and a.username = ? ");
        psql.setString(1,loid);
        HashMap<String, String> resultMap = (HashMap<String, String>) DBOperation.getRecord(psql.getSQL());
        if(null != resultMap && resultMap.size() > 0)
        {
            String device_id = StringUtil.getStringValue(resultMap.get("device_id"));
            logger.warn("[{}] 已绑定设备 [{}]",loid,device_id);
            checker.setDeviceId(device_id);
            return true;
        }
        return false;
    }
}
