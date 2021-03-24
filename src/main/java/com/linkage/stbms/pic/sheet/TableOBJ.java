/**
 * LINKAGE TECHNOLOGY (NANJING) CO.,LTD.<BR>
 * Copyright 2007-2010. All right reserved.
 */
package com.linkage.stbms.pic.sheet;


/**
 * global table
 * 
 * @author alex(yanhj@)
 * @version
 */
public class TableOBJ {

	/** user table name */
	public static String UserTabName = "";

	/** user table name */
	public static String VendorTabName = "";
	
	public static String DeviceTabName = "";
	
	public static String ModelTabName = "";
	
	public static String DeviceTypeTabName = "";

	static
	{
		UserTabName = "stb_tab_customer";
		VendorTabName = "stb_tab_vendor";
		DeviceTabName = "stb_tab_gw_device";
		ModelTabName = "stb_gw_device_model";
		DeviceTypeTabName = "stb_tab_devicetype_info";
	}

	/**
	 * init name
	 */
	public static void initTab() {

		if (SysType.IsITMS) {
			UserTabName = "tab_hgwcustomer";
			VendorTabName = "tab_vendor";
			DeviceTabName = "tab_gw_device";
		} else if (SysType.IsBBMS) {
			UserTabName = "tab_egwcustomer";
			VendorTabName = "tab_vendor";
			DeviceTabName = "tab_gw_device";
		} else if (SysType.IsSTBMS) {
			UserTabName = "tab_customer";
			VendorTabName = "tab_vendor";
			DeviceTabName = "tab_gw_device";
			ModelTabName = "gw_device_model";
			DeviceTypeTabName = "tab_devicetype_info";
		} else if (SysType.IsRHSTBMS) {
			UserTabName = "stb_tab_customer";
			VendorTabName = "stb_tab_vendor";
			DeviceTabName = "stb_tab_gw_device";
			ModelTabName = "stb_gw_device_model";
			DeviceTypeTabName = "stb_tab_devicetype_info";
		}
	}
}
