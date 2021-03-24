package com.linkage.itms.dispatch.sxdx.beanObj;

public class CpeFlow {
	/**
	 * 光猫发送字节数
	 */
	private String bytesSent;	
	/**
	 * 光猫接收字节数
	 */
	private String bytesReceived;
	/**
	 * 光猫发送包
	 */
	private String packetsSent;
	/**
	 * 光猫接收包
	 */
	private String packetsReceived;
	
	public String getBytesSent() {
		return bytesSent;
	}
	public void setBytesSent(String bytesSent) {
		this.bytesSent = bytesSent;
	}
	public String getBytesReceived() {
		return bytesReceived;
	}
	public void setBytesReceived(String bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
	public String getPacketsSent() {
		return packetsSent;
	}
	public void setPacketsSent(String packetsSent) {
		this.packetsSent = packetsSent;
	}
	public String getPacketsReceived() {
		return packetsReceived;
	}
	public void setPacketsReceived(String packetsReceived) {
		this.packetsReceived = packetsReceived;
	}
	
	@Override
	public String toString() {
		return "CpeFlow [bytesSent=" + bytesSent + ", bytesReceived=" + bytesReceived + ", packetsSent=" + packetsSent
				+ ", packetsReceived=" + packetsReceived + "]";
	}
	
}
