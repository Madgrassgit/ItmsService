package com.linkage.itms.rms.obj.domain;

/**
 * @author Jason(3412)
 * @date 2009-6-15
 */
public class DeviceWireInfoObj {

	/** WanDevice实例 */
	private String wanDeviceInstance;
	/** 线路状态,DSL物理链路的状态 */
	private String wireStatus;
	/** 线路协议 */
	private String modulationType;
	/** 上行速率Kbps */
	private long upstreamMaxRate;
	/** 下行速率Kbps */
	private long downstreamMaxRate;
	/** 数据路径是快速的(低延迟)不是隔行的(低错误率)Fast,Interleaved */
	private String dataPath;
	/** 隔行深度(交织深度),该变量只有在DataPath=Interleaved时才使用 */
	private String interleaveDepth;
	/** 上行线路衰减表示为0.1dB */
	private float upstreamAttenuation;
	/** 下行线路衰减表示为0.1dB */
	private float downstreamAttenuation;
	/** 下行噪声裕量表示为0.1dB */
	private String downNoise;
	/** 上行噪声裕量表示为0.1dB */
	private String upNoise;
	
	public String getWanDeviceInstance() {
		return wanDeviceInstance;
	}

	public String getWireStatus() {
		return wireStatus;
	}

	public String getModulationType() {
		return modulationType;
	}

	public long getUpstreamMaxRate() {
		return upstreamMaxRate;
	}

	public long getDownstreamMaxRate() {
		return downstreamMaxRate;
	}

	public String getDataPath() {
		return dataPath;
	}

	public String getInterleaveDepth() {
		return interleaveDepth;
	}

	public float getUpstreamAttenuation() {
		return upstreamAttenuation;
	}

	public float getDownstreamAttenuation() {
		return downstreamAttenuation;
	}

	public void setWanDeviceInstance(String wanDeviceInstance) {
		this.wanDeviceInstance = wanDeviceInstance;
	}

	public void setWireStatus(String wireStatus) {
		this.wireStatus = wireStatus;
	}

	public void setModulationType(String modulationType) {
		this.modulationType = modulationType;
	}

	public void setUpstreamMaxRate(long upstreamMaxRate) {
		this.upstreamMaxRate = upstreamMaxRate;
	}

	public void setDownstreamMaxRate(long downstreamMaxRate) {
		this.downstreamMaxRate = downstreamMaxRate;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public void setInterleaveDepth(String interleaveDepth) {
		this.interleaveDepth = interleaveDepth;
	}

	public void setUpstreamAttenuation(float upstreamAttenuation) {
		this.upstreamAttenuation = upstreamAttenuation;
	}

	public void setDownstreamAttenuation(float downstreamAttenuation) {
		this.downstreamAttenuation = downstreamAttenuation;
	}

	public String getDownNoise() {
		return downNoise;
	}

	public void setDownNoise(String downNoise) {
		this.downNoise = downNoise;
	}

	public String getUpNoise() {
		return upNoise;
	}

	public void setUpNoise(String upNoise) {
		this.upNoise = upNoise;
	}

}
