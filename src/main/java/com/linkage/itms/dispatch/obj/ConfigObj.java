
package com.linkage.itms.dispatch.obj;

import java.util.ArrayList;

public class ConfigObj
{

	String oui_sn = "";
	String service_object = "";
	ArrayList<String> service_name = new ArrayList<String>();
	ArrayList<ArrayList<Node>> service_parameters = new ArrayList<ArrayList<Node>>();

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

	public ArrayList<ArrayList<Node>> getService_parameters()
	{
		return service_parameters;
	}

	public void setService_parameters(ArrayList<ArrayList<Node>> service_parameters)
	{
		this.service_parameters = service_parameters;
	}

	public String getService_object()
	{
		return service_object;
	}

	public void setService_object(String service_object)
	{
		this.service_object = service_object;
	}
}
