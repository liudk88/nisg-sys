package com.hcxinan.sys.power;
/**
 * 权限范围
 * @author huangbin
 *
 */
public enum PowerScope {
	self(0,"仅本人")
	,org(1,"仅本单位")
	,all(2,"全部单位")
	,custom(3,"自定义");
	
	private String name;
	
	private int statu;
	


	public int getStatu() {
		return statu;
	}

	public void setStatu(int statu) {
		this.statu = statu;
	}

	PowerScope(int statu,String name){
		this.name = name;
		this.statu = statu;
	}
	
	public String getName(){
		return this.name;
	};
}
