package com.hcxinan.sys.model;

import java.util.Date;

import com.hcxinan.sys.power.PowerType;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;

import lombok.Data;

@Data
@TableName(value="sys_data_power")
public class SysDataPower  {
    /** 主键 */
    @TableField(isPk = true)
    private String sdp_id;
    /** 业务类型 */
    private String btype;
    /** 权限类型 */
    private PowerType power_type;
    /** 拥有者 */
    private String ower;
    /** 权限目标（可以为用户、单位或角色） */
    private String pobj;
    /** 权限值 */
    private Byte pval;
    /** 创建人 */
    private String creator ;
    /** 创建时间 */
    private Date cdate ;
}
