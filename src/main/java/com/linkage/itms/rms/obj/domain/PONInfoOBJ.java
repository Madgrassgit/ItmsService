
package com.linkage.itms.rms.obj.domain;

/**
 * @author 王森博
 */
public class PONInfoOBJ
{

	// pon口状态
	private String status = null;
	// PON口的发射光功率(单位是：毫瓦)
	private String txpower = null;
	// PON口的接收光功率(单位是：毫瓦)
	private String rxpower = null;
	// 光模块的工作温度，单位是（1/256度）
	private String transceiverTemperature = null;
	// 光模块的供电电压（单位：100微伏）
	private String supplyVottage = null;
	// 光发送机的偏置电流（单位：2微安）
	private String biasCurrent = null;
	// PON口发送的字节数
	private String bytesSent = null;
	// PON口接收的字节数
	private String bytesReceived = null;
	// PON口发送帧个数
	private String packetsSent = null;
	// PON口接收帧个数
	private String packetsReceived = null;
	// PON口发送单波帧数
	private String sunicastPackets = null;
	// PON口接收单波帧数
	private String runicastPackets = null;
	// PON口发送组波帧数
	private String smulticastPackets = null;
	// PON口接收组波帧数
	private String rmulticastPackets = null;
	// PON口发送广播波帧数
	private String sbroadcastPackets = null;
	// PON口接收广播波帧数
	private String rbroadcastPackets = null;
	// PON口接收的FEC错误帧数
	private String fecError = null;
	// PON口接收的HEC错误帧数
	private String hecError = null;
	// PON口发送方向丢帧数
	private String dropPackets = null;
	// PON口发送的PAUSE流控制帧数
	private String spausePackets = null;
	// PON口接收的PAUSE流控制帧数
	private String rpausePackets = null;

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the txpower
	 */
	public String getTxpower()
	{
		return txpower;
	}

	/**
	 * @param txpower
	 *            the txpower to set
	 */
	public void setTxpower(String txpower)
	{
		this.txpower = txpower;
	}

	/**
	 * @return the rxpower
	 */
	public String getRxpower()
	{
		return rxpower;
	}

	/**
	 * @param rxpower
	 *            the rxpower to set
	 */
	public void setRxpower(String rxpower)
	{
		this.rxpower = rxpower;
	}

	/**
	 * @return the transceiverTemperature
	 */
	public String getTransceiverTemperature()
	{
		return transceiverTemperature;
	}

	/**
	 * @param transceiverTemperature
	 *            the transceiverTemperature to set
	 */
	public void setTransceiverTemperature(String transceiverTemperature)
	{
		this.transceiverTemperature = transceiverTemperature;
	}

	/**
	 * @return the supplyVottage
	 */
	public String getSupplyVottage()
	{
		return supplyVottage;
	}

	/**
	 * @param supplyVottage
	 *            the supplyVottage to set
	 */
	public void setSupplyVottage(String supplyVottage)
	{
		this.supplyVottage = supplyVottage;
	}

	/**
	 * @return the biasCurrent
	 */
	public String getBiasCurrent()
	{
		return biasCurrent;
	}

	/**
	 * @param biasCurrent
	 *            the biasCurrent to set
	 */
	public void setBiasCurrent(String biasCurrent)
	{
		this.biasCurrent = biasCurrent;
	}

	/**
	 * @return the bytesSent
	 */
	public String getBytesSent()
	{
		return bytesSent;
	}

	/**
	 * @param bytesSent
	 *            the bytesSent to set
	 */
	public void setBytesSent(String bytesSent)
	{
		this.bytesSent = bytesSent;
	}

	/**
	 * @return the bytesReceived
	 */
	public String getBytesReceived()
	{
		return bytesReceived;
	}

	/**
	 * @param bytesReceived
	 *            the bytesReceived to set
	 */
	public void setBytesReceived(String bytesReceived)
	{
		this.bytesReceived = bytesReceived;
	}

	/**
	 * @return the packetsSent
	 */
	public String getPacketsSent()
	{
		return packetsSent;
	}

	/**
	 * @param packetsSent
	 *            the packetsSent to set
	 */
	public void setPacketsSent(String packetsSent)
	{
		this.packetsSent = packetsSent;
	}

	/**
	 * @return the packetsReceived
	 */
	public String getPacketsReceived()
	{
		return packetsReceived;
	}

	/**
	 * @param packetsReceived
	 *            the packetsReceived to set
	 */
	public void setPacketsReceived(String packetsReceived)
	{
		this.packetsReceived = packetsReceived;
	}

	/**
	 * @return the sunicastPackets
	 */
	public String getSunicastPackets()
	{
		return sunicastPackets;
	}

	/**
	 * @param sunicastPackets
	 *            the sunicastPackets to set
	 */
	public void setSunicastPackets(String sunicastPackets)
	{
		this.sunicastPackets = sunicastPackets;
	}

	/**
	 * @return the runicastPackets
	 */
	public String getRunicastPackets()
	{
		return runicastPackets;
	}

	/**
	 * @param runicastPackets
	 *            the runicastPackets to set
	 */
	public void setRunicastPackets(String runicastPackets)
	{
		this.runicastPackets = runicastPackets;
	}

	/**
	 * @return the smulticastPackets
	 */
	public String getSmulticastPackets()
	{
		return smulticastPackets;
	}

	/**
	 * @param smulticastPackets
	 *            the smulticastPackets to set
	 */
	public void setSmulticastPackets(String smulticastPackets)
	{
		this.smulticastPackets = smulticastPackets;
	}

	/**
	 * @return the rmulticastPackets
	 */
	public String getRmulticastPackets()
	{
		return rmulticastPackets;
	}

	/**
	 * @param rmulticastPackets
	 *            the rmulticastPackets to set
	 */
	public void setRmulticastPackets(String rmulticastPackets)
	{
		this.rmulticastPackets = rmulticastPackets;
	}

	/**
	 * @return the sbroadcastPackets
	 */
	public String getSbroadcastPackets()
	{
		return sbroadcastPackets;
	}

	/**
	 * @param sbroadcastPackets
	 *            the sbroadcastPackets to set
	 */
	public void setSbroadcastPackets(String sbroadcastPackets)
	{
		this.sbroadcastPackets = sbroadcastPackets;
	}

	/**
	 * @return the rbroadcastPackets
	 */
	public String getRbroadcastPackets()
	{
		return rbroadcastPackets;
	}

	/**
	 * @param rbroadcastPackets
	 *            the rbroadcastPackets to set
	 */
	public void setRbroadcastPackets(String rbroadcastPackets)
	{
		this.rbroadcastPackets = rbroadcastPackets;
	}

	/**
	 * @return the fecError
	 */
	public String getFecError()
	{
		return fecError;
	}

	/**
	 * @param fecError
	 *            the fecError to set
	 */
	public void setFecError(String fecError)
	{
		this.fecError = fecError;
	}

	/**
	 * @return the hecError
	 */
	public String getHecError()
	{
		return hecError;
	}

	/**
	 * @param hecError
	 *            the hecError to set
	 */
	public void setHecError(String hecError)
	{
		this.hecError = hecError;
	}

	/**
	 * @return the dropPackets
	 */
	public String getDropPackets()
	{
		return dropPackets;
	}

	/**
	 * @param dropPackets
	 *            the dropPackets to set
	 */
	public void setDropPackets(String dropPackets)
	{
		this.dropPackets = dropPackets;
	}

	/**
	 * @return the spausePackets
	 */
	public String getSpausePackets()
	{
		return spausePackets;
	}

	/**
	 * @param spausePackets
	 *            the spausePackets to set
	 */
	public void setSpausePackets(String spausePackets)
	{
		this.spausePackets = spausePackets;
	}

	/**
	 * @return the rpausePackets
	 */
	public String getRpausePackets()
	{
		return rpausePackets;
	}

	/**
	 * @param rpausePackets
	 *            the rpausePackets to set
	 */
	public void setRpausePackets(String rpausePackets)
	{
		this.rpausePackets = rpausePackets;
	}
}
