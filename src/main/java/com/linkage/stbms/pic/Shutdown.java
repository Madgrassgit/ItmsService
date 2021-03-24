/**
 * Base.java 1.00 2007-3-13
 *
 * Copyright 2006 联创网络科技.版权所有
 */
package com.linkage.stbms.pic;

/**
 * 所有模块统一接口，必须实现shutdown接口
 * 
 * @author yuhaiteng
 * @version 1.00, 2007-3-13
 * @since CoreManager 1.0
 */
public interface Shutdown {
	/**
	 * 关闭本程序接口，释放程序资源
	 * 
	 */
	public void shutdown(int cause);
}
