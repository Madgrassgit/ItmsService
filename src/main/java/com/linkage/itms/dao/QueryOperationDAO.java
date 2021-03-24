package com.linkage.itms.dao;

import java.util.HashMap;
import java.util.List;

import com.linkage.commons.db.DBOperation;
import com.linkage.commons.db.PrepareSQL;
import com.linkage.itms.dispatch.obj.QueryOperationChecker;

/**
 * @author songxq
 * @date 2021/1/14
 */

public class QueryOperationDAO {

    private static int two = 2;


    public List<HashMap<String, String>> queryRouteAndBridgeModify(QueryOperationChecker checker) {
        PrepareSQL psql = new PrepareSQL();
        if(two == checker.getUserInfoType())
        {
            psql.append("select loid,username,oper_action,oper_origon,oper_staff, add_time,oper_result,result_desc " +
                    " from bridge_route_oper_log where loid = ? order by add_time desc ");
        }
        else
        {
            psql.append("select loid,username,oper_action,oper_origon,oper_staff, add_time,oper_result,result_desc " +
                    " from bridge_route_oper_log where username = ? order by add_time desc ");
        }
        psql.setString(1,checker.getUserInfo());
        return DBOperation.getRecords(psql.getSQL());
    }
}
