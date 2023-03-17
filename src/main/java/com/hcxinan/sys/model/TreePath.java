package com.hcxinan.sys.model;

import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
*@Description 树节点关系表
*@Param
*@Return
*@Author liudk
*@DateTime 20-12-5 上午11:04
*/
@Data
@TableName("tree_path")
public class TreePath implements Serializable {
    //祖先节点
    @TableField(isPk = true)
    private Integer ancestor;
    //后代节点
    @TableField(isPk = true)
    private Integer descendant;
    //祖先距离后代的距离
    private Integer distance;
    //树id
    @TableField(isPk = true)
    private String tree_id;
    //排序号(在同一层中数字越大越优先)
    private Integer seq;
    //创建时间
    private Date cdate;
}
