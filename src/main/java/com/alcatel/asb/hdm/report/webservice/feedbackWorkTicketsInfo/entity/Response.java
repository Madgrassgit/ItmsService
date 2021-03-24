package com.alcatel.asb.hdm.report.webservice.feedbackWorkTicketsInfo.entity;

import java.io.Serializable;

public class Response
  implements Serializable
{
  private static final long serialVersionUID = -4177546675006096092L;
  private String work_id;
  private ResultArray RESULT_ARRAY;

  public Response(){}
  public Response(String workId)
  {
    this.work_id = workId;
  }

  public String getWork_id()
  {
    return this.work_id;
  }

  public void setWork_id(String workId)
  {
    this.work_id = workId;
  }

  public ResultArray getRESULT_ARRAY()
  {
    return this.RESULT_ARRAY;
  }

  public void setRESULT_ARRAY(ResultArray rESULT_ARRAY)
  {
    this.RESULT_ARRAY = rESULT_ARRAY;
  }

  public String toString()
  {
    if ((this.RESULT_ARRAY != null) && (this.RESULT_ARRAY.getLoid_result() != null)) {
      StringBuilder sb = new StringBuilder();
      for (LogicIdResult r : this.RESULT_ARRAY.getLoid_result()) {
        sb.append(r.toString());
      }
      return new StringBuilder().append("Response [workId=").append(this.work_id).append(", RESULT_ARRAY=").append(sb.toString()).append("]").toString();
    }

    return new StringBuilder().append("Response [workId=").append(this.work_id).append(", RESULT_ARRAY=").append(this.RESULT_ARRAY).append("]").toString();
  }
}