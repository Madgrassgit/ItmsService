package com.linkage.itms.dispatch.obj;

import java.util.ArrayList;

public class QueryRtnMsg
{
	String oui_sn = "";
	ArrayList<String> service_name = new ArrayList<String>();
	ArrayList<ArrayList<String>> service_parameters_result = new ArrayList<ArrayList<String>>();
	
	public String getOui_sn()
	{
		return oui_sn;
	}
	
	public void setOui_sn(String oui_sn)
	{
		this.oui_sn = oui_sn;
	}

	
	public ArrayList<String> getService_name()
	{
		return service_name;
	}

	
	public void setService_name(ArrayList<String> service_name)
	{
		this.service_name = service_name;
	}

	
	public ArrayList<ArrayList<String>> getService_parameters_result()
	{
		return service_parameters_result;
	}

	
	public void setService_parameters_result(
			ArrayList<ArrayList<String>> service_parameters_result)
	{
		this.service_parameters_result = service_parameters_result;
	}
	
}
