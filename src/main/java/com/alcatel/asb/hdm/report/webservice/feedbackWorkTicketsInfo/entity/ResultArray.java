package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity;

import java.io.Serializable;
import java.util.List;

public class ResultArray
  implements Serializable
{
  private static final long serialVersionUID = 6934134093307114598L;
  private List<LogicIdResult> loid_result;

  public List<LogicIdResult> getLoid_result()
  {
    return this.loid_result;
  }

  public void setLoid_result(List<LogicIdResult> loid_result)
  {
    this.loid_result = loid_result;
  }
}