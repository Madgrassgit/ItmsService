package com.linkage.itms.dispatch.cqdx.beanObj;

import java.io.Serializable;

/**
 * 
 * @author liyl10 (Ailk No.71496)
 * @version 1.0
 * @since 2017年11月18日
 *
 */
public class StartRebootDiagResponse implements Serializable{
	private static final long serialVersionUID = -1321473195434428182L;
	//操作流水号
	private long op_id;
	//用户loid
	private int result;
    //结果说明
    private String err_msg;
    
    @Override
	public String toString() {
		return "Order [op_id=" + op_id + ", result=" + result + ", err_msg=" + err_msg + "]";
	}
	
	public long getOp_id()
	{
		return op_id;
	}

	
	public void setOp_id(long op_id)
	{
		this.op_id = op_id;
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
