package com.hcxinan.sys.logs;
/**
 * 操作类型日志
 * @author huangbin
 *
 */
public enum OperationType {
	
	
	undefine("未定义")
	,login("登陆")
	,visit("访问")
	,logout("退出")
	,delete("删除")
	,create("新建")
	,upload("上传")
	,reporting("上报")
	,_import("导入")
	,export("导出")
	,enabel("启用")
	,disable("停用")
	,approve("审批")
	,assessment("研判")
	,modify("修改")
	,download("下载")
	,edit("编辑")
	,save("保存")
	;
	private String name;
	
	
	OperationType(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	};

}
