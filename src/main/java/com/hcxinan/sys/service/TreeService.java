package com.hcxinan.sys.service;

import com.hcxinan.sys.model.TreeNode;
import com.hcxinan.sys.model.TreePath;
import com.morph.cond.Cond;
import com.morph.db.IMoDao;
import com.morph.dml.SelectSql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("treeService")
public class TreeService {

    @Autowired
    private IMoDao dao;
    /**
    *@Description 删除一颗树节点
    *@Param [nodeIds]
    *@Return void
    *@Author liudk
    *@DateTime 21-5-23 下午11:28
    */
    public void delTreeNode(Integer... nodeIds){
        //删除路径
        dao.setBindTable(()->"TREE_PATH").delete(Cond.in("ANCESTOR",nodeIds)
                .or(Cond.in("DESCENDANT",nodeIds)));
        dao.setBindTable(()->"TREE_NODE").deleteByPk(nodeIds);
    }
    /**
    *@Description 在某个父节点下插入多个节点
    *@Param [treeId：树id, pid：父节点id, insertNodeFlag：是否需要插入节点，nodeNum：需要插入的节点树个数]
    *@Return void
    *@Author liudk
    *@DateTime 21-5-27 下午5:38
    */
    public Integer[] insertPathUnderNodeId(String treeId, Integer pid,Integer nodeNum){
        SelectSql dml=new SelectSql(TreeNode.class, Cond.eq("TREE_ID",treeId));
        dml.addQueryColumns("MAX(TN_ID) MAXID");
        List<Map> treeId_maxId_list=dao.queryList(dml);//找到树最大的节点id
        Integer[] resultNodeIds=new Integer[nodeNum];
        if(treeId_maxId_list.size()==1){
            Integer curMaxId= (Integer) treeId_maxId_list.get(0).get("MAXID");
            //找到父节点的所有祖先节点
            dml=new SelectSql(TreePath.class, Cond.eq("TREE_ID",treeId).and(Cond.eq("DESCENDANT",pid)));
            dml.addQueryColumns("ANCESTOR","DISTANCE");
            List<Map> ancestorList=dao.queryList(dml);
            /*
             * 当批量插入节点的时候，目前只考虑在某个节点下插入一个几点，不考虑在多个节点插入多颗子树的情况
             * */
            List<TreePath> paths=new ArrayList<>();
            TreePath path=null;
            for(int i=0;i<nodeNum;i++){
                resultNodeIds[i]=++curMaxId;
                /*构造树路径
                 * 1）插入当前几点，距离为0
                 * */
                for(Map ancestorMap:ancestorList){
                    path=new TreePath();
                    path.setTree_id(treeId);
                    path.setAncestor((Integer) ancestorMap.get("ANCESTOR"));
                    path.setDescendant(curMaxId);
                    path.setDistance((Integer) ancestorMap.get("DISTANCE")+1);
                    path.setCdate(new Date());
                    paths.add(path);
                }
                //把自己与自身的关系也加上
                path=new TreePath();
                path.setTree_id(treeId);
                path.setAncestor(curMaxId);
                path.setDescendant(curMaxId);
                path.setDistance(0);
                path.setCdate(new Date());
                paths.add(path);
            }
            dao.setBindTable(()->"TREE_PATH").insert(TreePath.class,paths);
        }
        return resultNodeIds;
    }
}
