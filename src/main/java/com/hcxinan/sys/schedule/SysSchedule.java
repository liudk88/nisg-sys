package com.hcxinan.sys.schedule;

import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("SYS_SCHEDULE")
@Data
public class SysSchedule{
    //任务ID
    @TableField(isPk = true)
    private String taskid;
    //任务名称
    private String taskname;
    //0：使用spring加载；1：权限定加载
    private Integer class_loader_type;
    //spring类名或类全限定名
    private String jobclass;
    //cron表达式
    private String cron;
    //json字符串参数
    private String json_param;
    //备注说明
    private String des;
    //已运行次数
    private Integer runtimes;
    //任务状态(0:禁用，1：启用)
    private Boolean enabled;
    //排序号
    private Integer seq;
    //创建人
    private String creator;
    //创建时间
    private Date cdate;
    //是否有效
    private Boolean valid;
}
