package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity;

import java.io.Serializable;

public class LogicIdResult
  implements Serializable
{
  private static final long serialVersionUID = 504968335003580592L;
  private String loid = "";

  private String serial_number = "";

  private String error_msg = "";

  private int service_status = 2;

  private String service_list = "";

  public LogicIdResult(){}
  public LogicIdResult(String loid) {
    this.loid = loid;
  }

  public String getLoid()
  {
    return this.loid;
  }

  public String getSerial_number()
  {
    return this.serial_number;
  }

  public String getError_msg()
  {
    return this.error_msg;
  }

  public int getService_status()
  {
    return this.service_status;
  }

  public String getService_list()
  {
    return this.service_list;
  }

  public void setLoid(String loid)
  {
    this.loid = loid;
  }

  public void setSerial_number(String serial_number)
  {
    this.serial_number = serial_number;
  }

  public void setError_msg(String error_msg)
  {
    this.error_msg = error_msg;
  }

  public void setService_status(int service_status)
  {
    this.service_status = service_status;
  }

  public void setService_list(String service_list)
  {
    this.service_list = service_list;
  }

  public String toString()
  {
    return "LogicIdResult [loid=" + this.loid + ", serial_mumber=" + this.serial_number + ", error_msg=" + this.error_msg + ", service_status=" + this.service_status + ", service_list=" + this.service_list + "]";
  }
}