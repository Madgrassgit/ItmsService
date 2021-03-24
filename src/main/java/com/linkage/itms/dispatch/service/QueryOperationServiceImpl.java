package com.linkage.itms.dispatch.service;

import com.linkage.itms.dao.QueryOperationDAO;
import com.linkage.itms.dispatch.obj.QueryOperationChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
/**
 * @author songxq
 * @date 2021/1/14
 */
public class QueryOperationServiceImpl implements IService {
    private static Logger logger = LoggerFactory.getLogger(QueryOperationServiceImpl.class);


    @Override
    public String work(String inXml) {
        logger.warn("QueryOperationService==>inXml({})",inXml);
        QueryOperationChecker checker = new QueryOperationChecker(inXml);
        if (!checker.check())
        {
            logger.warn("江西电信查询宽带业务路由桥接修改记录接口入参验证失败，UserInfoType=[{}]，UserInfo=[{}]",
                     checker.getUserInfoType(),checker.getUserInfo() );
            logger.warn("QueryOperationService==>retParam={}", checker.getReturnXml());
            return checker.getReturnXml();
        }

        QueryOperationDAO dao = new QueryOperationDAO();
        List<HashMap<String,String>> resultList = dao.queryRouteAndBridgeModify(checker);
        if(null == resultList || resultList.isEmpty())
        {
            checker.setResult(1002);
            checker.setResultDesc("查无此客户");
        }
        else
        {
            checker.setResult(0);
            checker.setResultDesc("成功");
            checker.setResultList(resultList);
        }
        return checker.getReturnXml();
    }
}
