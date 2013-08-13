package com.weicheng.taipeiyoubikeoffline;

public class Station {
	private final String chineseName;
	private final String chineseDesc;
	private final String name;
	private final double lat;
	private final double lng;
	
	public Station(String chineseName, String chineseDesc, String name,double lat, double lng) 
	{
		this.chineseName = chineseName;
		this.chineseDesc = chineseDesc;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((chineseDesc == null) ? 0 : chineseDesc.hashCode());
		result = prime * result
				+ ((chineseName == null) ? 0 : chineseName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lng);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		if (chineseDesc == null) {
			if (other.chineseDesc != null)
				return false;
		} else if (!chineseDesc.equals(other.chineseDesc))
			return false;
		if (chineseName == null) {
			if (other.chineseName != null)
				return false;
		} else if (!chineseName.equals(other.chineseName))
			return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getChineseName() {
		return chineseName;
	}

	public String getChineseDesc() {
		return chineseDesc;
	}

	public String getName() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}
}
