/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.stbms.pic.sheet;

import com.linkage.stbms.pic.Global;

/**
 * type of system
 * 
 * @author alex(yanhj@)
 * @version
 */
public class SysType {

	/** is itms */
	public static boolean IsITMS = false;

	/** is bbms */
	public static boolean IsBBMS = false;

	/** is stbms */
	public static boolean IsSTBMS = false;
	
	/** is �ں�stbms */
	public static boolean IsRHSTBMS = false;

	static {
		switch (Global.G_SystemType) {
		case 1:
			IsITMS = true;

			break;

		case 2:

		case 3:
			IsBBMS = true;

			break;

		case 4:
			IsSTBMS = true;

			break;
			
		case 5:
			IsRHSTBMS = true;

			break;

		default:
			break;
		}
	}

}
