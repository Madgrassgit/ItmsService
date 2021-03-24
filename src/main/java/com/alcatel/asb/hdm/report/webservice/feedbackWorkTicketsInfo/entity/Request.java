package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity;

import java.io.Serializable;

public class Request
  implements Serializable
{
  private static final long serialVersionUID = -7148363779727131818L;
  private String work_id;
  private LogicIdArray LOID_ARRAY;

  public String getWork_id()
  {
    return this.work_id;
  }

  public void setWork_id(String work_id)
  {
    this.work_id = work_id;
  }

  public LogicIdArray getLOID_ARRAY()
  {
    return this.LOID_ARRAY;
  }

  public void setLOID_ARRAY(LogicIdArray lOID_ARRAY)
  {
    this.LOID_ARRAY = lOID_ARRAY;
  }

  public String toString()
  {
    return "Request [work_id=" + this.work_id + ", LOID_ARRAY=" + this.LOID_ARRAY + "]";
  }
}