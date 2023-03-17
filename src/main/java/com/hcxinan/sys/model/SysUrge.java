package com.hcxinan.sys.model;

import com.hcxinan.core.inte.system.IUrge;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("SYS_URGE")
public class SysUrge implements IUrge {
    /** 主键 */
    @TableField(isPk = true)
    private String urge_id;
    /** 业务分类标识 */
    private String btype;
    /** 关联业务表主键 */
    private String business_id;
    /** 权限用户（督办人） */
    private String puser;
    /** 被催办人 */
    private String urged_man;
    /** 催办人内容 */
    private String content;
    /** 催办时手机号 */
    private String phone;
    /** 催办时邮箱 */
    private String email;
    /** 催办方式（0：短信；1：邮件；2：系统），多个以英文逗号隔开 **/
    private String urge_type;
    /** 状态（0：催办失败；1：催办成功） */
    private int status;
    /** 扩展字段1 */
    private String param1;
    /** 扩展字段2 */
    private String param2;
    /** 扩展字段3 */
    private String param3;
    /** 创建人 */
    private String creator ;
    /** 创建时间 */
    private Date cdate ;
    /** 更新时间 */
    private Date udate ;
    /** 是否有效 */
    private Boolean valid ;
}
