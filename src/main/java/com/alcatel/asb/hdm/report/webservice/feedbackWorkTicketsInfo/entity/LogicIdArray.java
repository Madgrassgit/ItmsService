package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity;

import java.io.Serializable;
import java.util.List;

public class LogicIdArray
  implements Serializable
{
  private static final long serialVersionUID = -7511053857734794707L;
  private List<String> loid;

  public List<String> getLoid()
  {
    return this.loid;
  }

  public void setLoid(List<String> loid)
  {
    this.loid = loid;
  }

  public String toString()
  {
    return "LogicIdArray [loid=" + this.loid + "]";
  }
}