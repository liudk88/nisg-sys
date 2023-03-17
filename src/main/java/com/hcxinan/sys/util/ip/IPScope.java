package com.hcxinan.sys.util.ip;

import java.util.ArrayList;
import java.util.List;
@Deprecated //请使用core中对应的类，后面废除
public class IPScope {
	
	private String ipExp;
	
	private	int start;
	
	private	int end;
	
	public IPScope(String ipExp, int start, int end){
		this.ipExp = ipExp;
		this.start = start;
		this.end = end;
	}
	
	/**
	 * 是否在为指定参数的子范围
	 * @param other
	 * @return
	 */
	public boolean isInScope(IPScope other){
		if( other.getStart()  <= this.start  && this.end <= other.getEnd() ){
			return true;
		}
		return false;
	}
	/**
	 * 是否有相交的IP范围
	 * @param ipScope
	 * @return
	 */
	public boolean hasIntersectScope(IPScope ipScope){
		if( (start <= ipScope.getStart() && ipScope.getStart() <= end) 
				 || (ipScope.getStart() <= start && start <= ipScope.getEnd())
				   ){
					return true;
		}
		return false;
	}
	
	
	public boolean isSingleIp(){
		return this.start == this.end;
	}
	
	public List<String> getIpList(){
		List<String> list = new ArrayList<String>();
			for(int i = start ; i <= end ; i++){
				String ip = Ipv4Utils.intToIp(i);
				list.add(ip);
			}
		return list;
	}
	

	public String getIpExp() {
		return ipExp;
	}

	public int getStart() {
		return start;
	}

	
	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "IPScope [ipExp=" + ipExp + ", start=" + start + ", end=" + end + "]";
	}

}
