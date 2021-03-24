package com.linkage.itms.dispatch.util;

import com.linkage.commons.redis.XMemDataChildInterface;
import com.linkage.commons.redis.impl.XRedisDataChidUtil;
import com.linkage.itms.Global;
import com.linkage.itms.commom.StringUtil;
import com.linkage.itms.dispatch.util.memcached.MemcachedDataUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guankai (AILK No.300401)
 * @version 1.0
 * @category ailk-itms-ItmsService
 * @since 2020/10/9
 */
public class TimeCheckUtil {
	
    private static Logger logger = LoggerFactory.getLogger(TimeCheckUtil.class);
    
    private XMemDataChildInterface memcachedDataUtil = null;
    
    public TimeCheckUtil() {
    	if ("R".equals(Global.XMEM_TYPE)) {
			memcachedDataUtil = new XRedisDataChidUtil();
		}else {
			memcachedDataUtil = new MemcachedDataUtil();
		}
		memcachedDataUtil.setMemcachedName("ItmsService");
    }

    public boolean isInTimeCheck(String servName){
        int min = (int) (System.currentTimeMillis()/1000/60);
        String memKey =  DigestUtils.md5Hex((servName+min).getBytes());
        int useTime = StringUtil.getIntegerValue(memcachedDataUtil.get(memKey),-1);
        logger.warn("{}接口已调用次数：{},最大调用次数：{}",servName,useTime,Global.TEST_SPEED_TIME);
        if(useTime<0){
            logger.warn("{}分钟已调用次数不存在，新增key：{}",memKey,servName);
            memcachedDataUtil.add(memKey,86400,"0");
        }else if(useTime> Global.TEST_SPEED_TIME){
            return false;
        }
        memcachedDataUtil.incr(memKey,1);
        return true;
    }

}
