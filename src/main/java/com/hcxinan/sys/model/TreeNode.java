package com.hcxinan.sys.model;

import com.morph.annotation.IdType;
import com.morph.annotation.TableField;
import com.morph.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
*@Description 树节点表
*@Param
*@Return
*@Author liudk
*@DateTime 20-12-5 上午11:04
*/
@Data
@TableName("tree_node")
public class TreeNode implements Serializable {
    //节点id
    @TableField(isPk = true)
    private Integer tn_id;
    //树id
    private String tree_id;
    //节点名
    private String node_name;
    //节点类型
    private String node_type;
    //数据来源表名
    private String soure_table;
    //数据来源主键
    private String source_pk_val;
    //创建时间
    private Date cdate;

    //父节点id，用来辅助某个节点是在那个父节点下新增
    @TableField(exist = false)
    private Integer pid;
    //所有的节点关系
    @TableField(exist = false)
    private List<TreePath> treePaths;
    //所有的节点
    @TableField(exist = false)
    private List<TreePath> pathList=new ArrayList<>();
    @TableField(exist = false)
    private final Map<String, TreePath> pathMap = Collections.synchronizedMap(new HashMap<>());
    /**
    *@Description 增加路径
    *@Param [path]
    *@Return void
    *@Author liudk
    *@DateTime 20-12-5 上午11:49
    */
    public void addPath(TreePath path){
        if(path.getTree_id().equals(tree_id)){//只有同一颗树下的才会加载
            TreePath nodePath = pathMap.get(getPathId(path));
            if(nodePath==null){
                pathList.add(path);
                pathMap.put(getPathId(path),path);
            }
        }
    }

    /*public List<TreePath> getPathsByNodeId(){

    }*/

    /*public TreeNode removePathById(Integer nodeId) {
        TreeNode node = nodeMap.remove(nodeId);
        if (node == null) {
            return null;
        } else {
            nodeList.remove(node);
            return node;
        }
    }*/

    /**
    *@Description 给路径设定一个主键,以祖先id+"|"+当前节点作为主键
    *@Param [path]
    *@Return java.lang.String
    *@Author liudk
    *@DateTime 20-12-5 上午11:47
    */
    private String getPathId(TreePath path){
        return path.getAncestor()+"|"+path.getDescendant();
    }
}
