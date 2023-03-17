package com.hcxinan.sys.model;

import com.morph.annotation.IdType;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
*@Description 树表
*@Param
*@Return
*@Author liudk
*@DateTime 20-12-5 上午11:04
*/
@Data
@TableName("tree")
public class Tree implements Serializable {
    /*private static final long serialVersionUID = 6184948709971258311L;*/
    //树id
    @TableField(isPk = true,idType = IdType.DATETIME_ID)
    private String tree_id;
    //描述
    private String remark;
    //状态
    private Integer state;
    //创建人
    private String creator;
    //创建时间
    private Date cdate;
    //修改人
    private String updator;
    //修改时间
    private Date udate;
    //是否有效
    private Integer valid;
    //所有的节点
    @TableField(exist = false)
    private List<TreeNode> nodeList =new ArrayList<>();
    @TableField(exist = false)
    private final Map<Integer, TreeNode> nodeMap = Collections.synchronizedMap(new HashMap<>());
    /**
    *@Description 增加一个节点
    *@Param [node:节点]
    *@Return void
    *@Author liudk
    *@DateTime 20-12-5 上午11:32
    */
    public void addNode(TreeNode node){
        TreeNode treeNode = nodeMap.get(node.getTn_id());
        if(treeNode==null){
            node.setTree_id(tree_id);
            nodeList.add(node);
            nodeMap.put(node.getTn_id(),node);
        }
    }
    /**
    *@Description 删除指定id的节点
    *@Param [nodeId:节点id]
    *@Return com.hcxinan.sys.model.TreeNode
    *@Author liudk
    *@DateTime 20-12-5 上午11:32
    */
    public TreeNode removeNodeById(Integer nodeId) {
        TreeNode node = nodeMap.remove(nodeId);
        if (node == null) {
            return null;
        } else {
            nodeList.remove(node);
            return node;
        }
    }
}
