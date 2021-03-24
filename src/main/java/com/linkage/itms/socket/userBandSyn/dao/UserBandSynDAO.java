package com.linkage.itms.socket.userBandSyn.dao;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.socket.userBandSyn.bean.UserBandSynBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * create by lingmin on 2019/11/07
 */
public class UserBandSynDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserBandSynDAO.class);

    public Map<String, String> queryNetServ(String account)
    {
        String sql = "select user_id from tab_net_serv_param where username=? and serv_type_id=10";
        PrepareSQL pSql = new PrepareSQL(sql);
        pSql.setString(0, account);
        return DBOperation.getRecord(pSql.getSQL());
    }

    public int updateNetServBand(UserBandSynBean bandSynBean){
        String sql = "update tab_net_serv_param set up_bandwidth='" +
                bandSynBean.getUpBandwidth() +
                "',down_bandwidth='" +
                bandSynBean.getDownBandwidth() +
                "' where username='" +
                bandSynBean.getAccount() +
                "'";
        PrepareSQL pSql = new PrepareSQL(sql);
        int rows = DBOperation.executeUpdate(pSql.getSQL());
        LOGGER.warn("updateNetServBand with username:{},rows:{}",bandSynBean.getAccount(),rows);
        return rows;
    }
}
