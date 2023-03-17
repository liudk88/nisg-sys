package com.hcxinan.sys.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_rms_alllogs")
public class NisgAlllogs {
    //用户ID @PrimaryKey
    @TableId
    private String logid;
    //会话ID值
    private String sid;
    //登陆系统ID
    private String platid;
    //工作计算机
    private String worker;
    //计算机IP
    private String workip;
    //计算机MAC
    private String mac;
    //登陆帐号
    private String account;
    //操作结果(1:为操作成功)
    private Integer opresult;
    //异常描述
    private String errors;
    //消息内容
    private String contents;
    //最近登陆时间
    private Date logtime;
    //访问IP
    private String addr;
    //访问路径
    private String url;
    //查询参数
    private String params;
    //是否有效
    private Integer sfyx;
    //日志处理状态(1为已经处理，2:为丢弃,0为未解释)
    private Integer status;
    //创建时间
    private Date cjsj;

    @TableField("log_level")
    private Integer log_level;

    private Date xgsj;

    private String descr;

    private String menu;
}
