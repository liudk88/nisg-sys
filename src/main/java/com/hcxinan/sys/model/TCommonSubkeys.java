/**
*/
package com.hcxinan.sys.model;


import com.baomidou.mybatisplus.annotation.TableName;
import com.hcxinan.sys.cache.inte.ICacheCode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Title:
 * @Author: Fly
 * @Date: 2021/7/20 - 19:07
 * @Description:
 */
@TableName("SYS_DICT_DATA")
public class TCommonSubkeys implements ICacheCode,Serializable {
	
	//字典键值 @PrimaryKey
	private String code;
	//代码子键 @PrimaryKey
	private String subcode;
	//子键ID 
	private String tid;
	//代码名称 
	private String cname;
	//是否有效、1 有效 
	private Integer valid;
	//排序号 
	private Integer seq;
	//参数1、扩展配置 
	private String params1;
	//参数2、扩展配置 
	private String params2;
	//备注说明 
	private String des;
	//创建时间 
	private Date cjsj;
	//创建人 
	private String cjr;
	//修改人
	private String xgr;
	// 修改时间 
	private Date xgsj;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getSubcode() {
		return subcode;
	}

	@Override
	public void setSubcode(String subcode) {
		this.subcode = subcode;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	@Override
	public String getCname() {
		return cname;
	}

	@Override
	public void setCname(String cname) {
		this.cname = cname;
	}

	@Override
	public Boolean getValid() {
		return valid==1;
	}

	@Override
	public void setValid(Integer valid) {
		this.valid = valid;
	}

	@Override
	public Integer getSeq() {
		return seq;
	}

	@Override
	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	@Override
	public String getParams1() {
		return params1;
	}

	@Override
	public void setParams1(String params1) {
		this.params1 = params1;
	}

	@Override
	public String getParams2() {
		return params2;
	}

	@Override
	public void setParams2(String params2) {
		this.params2 = params2;
	}

	@Override
	public String getDes() {
		return des;
	}

	@Override
	public void setDes(String des) {
		this.des = des;
	}

	public Date getCjsj() {
		return cjsj;
	}

	public void setCjsj(Date cjsj) {
		this.cjsj = cjsj;
	}

	public String getCjr() {
		return cjr;
	}

	public void setCjr(String cjr) {
		this.cjr = cjr;
	}

	public String getXgr() {
		return xgr;
	}

	public void setXgr(String xgr) {
		this.xgr = xgr;
	}

	public Date getXgsj() {
		return xgsj;
	}

	public void setXgsj(Date xgsj) {
		this.xgsj = xgsj;
	}

	@Override
	public String getKey() {
		return subcode;
	}

	@Override
	public String getVal() {
		return cname;
	}


}
