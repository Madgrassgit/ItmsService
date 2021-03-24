package com.linkage.itms.dispatch.gsdx.beanObj;

import java.io.Serializable;

public class CPEMission implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int iOperRst;
	private int iMissionID;
	@Override
	public String toString() {
		return "CPEMission [iOperRst=" + iOperRst + ", iMissionID="
				+ iMissionID + "]";
	}
	public int getiOperRst() {
		return iOperRst;
	}
	public void setiOperRst(int iOperRst) {
		this.iOperRst = iOperRst;
	}
	public int getiMissionID() {
		return iMissionID;
	}
	public void setiMissionID(int iMissionID) {
		this.iMissionID = iMissionID;
	}
	

}
