
package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class UnbindWorkTicketResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//执行结果
	private int result;
	//错误原因
    private String err_msg;
    
    @Override
	public String toString() {
		return "UnbindWorkTicketResponse [result=" + result + ", err_msg=" + err_msg + "]";
	}

	public int getResult()
	{
		return result;
	}


	
	public void setResult(int result)
	{
		this.result = result;
	}


	public String getErr_msg()
	{
		return err_msg;
	}

	
	public void setErr_msg(String err_msg)
	{
		this.err_msg = err_msg;
	}
    
}