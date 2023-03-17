package com.hcxinan.sys.model;

import com.morph.annotation.IdType;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("form_extend_col")
public class FormExtendCol {
    //业务主键 @PrimaryKey
    @TableField(isPk = true,idType= IdType.DATETIME_ID)
    private String fec_id;
    //简单转义1
    private String simple_convert1;
    //简单转义2
    private String simple_convert2;
    //简单转义3
    private String simple_convert3;
    //简单转义4
    private String simple_convert4;
    //简单转义5
    private String simple_convert5;
    //32位文本1
    private String short_text1;
    //32位文本2
    private String short_text2;
    //32位文本3
    private String short_text3;
    //32位文本4
    private String short_text4;
    //32位文本5
    private String short_text5;
    //150位文本1
    private String med_text1;
    //150位文本2
    private String med_text2;
    //150位文本3
    private String med_text3;
    //150位文本4
    private String med_text4;
    //150位文本5
    private String med_text5;
    //时间日期1
    private Date datet1;
    //时间日期2
    private Date datet2;
    //时间日期3
    private Date datet3;
    //时间日期4
    private Date datet4;
    //时间日期5
    private Date datet5;
    //整数1
    private Integer inte1;
    //整数2
    private Integer inte2;
    //整数3
    private Integer inte3;
    //整数4
    private Integer inte4;
    //整数5
    private Integer inte5;
    //整数6
    private Integer inte6;
    //整数7
    private Integer inte7;
    //整数8
    private Integer inte8;
    //整数9
    private Integer inte9;
    //整数10
    private Integer inte10;
    //布尔1
    private Boolean bool1;
    //布尔2
    private Boolean bool2;
    //布尔3
    private Boolean bool3;
    //浮点数1
    private Double doub1;
    //浮点数2
    private Double doub2;
    //浮点数3
    private Double doub3;
    //浮点数4
    private Double doub4;
    //浮点数5
    private Double doub5;
    //创建人
    private String creator;
    //创建时间
    private Date cdate;
}
