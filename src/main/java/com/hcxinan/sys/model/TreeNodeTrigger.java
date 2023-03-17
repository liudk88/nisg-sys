package com.hcxinan.sys.model;

import com.morph.cond.Cond;
import com.morph.db.IMoDao;
import com.morph.db.ITrigger;
import com.morph.dml.SelectSql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class TreeNodeTrigger implements ITrigger<Map<String,Object>> {

    @Autowired
    private IMoDao moDao;

    @Override
    public Object doBeforeInsert(List<Map<String, Object>> treeNodes) {
        //插入之前，设置主键
        Integer pid=null;//父节点
        String treeId=null;//树id
        int pid_nums=1;
        int treeid_nums=1;
        TreeNode[] nodes=new TreeNode[treeNodes.size()];
        int i=0;
        for(Object obj:treeNodes){
            TreeNode treeNode= (TreeNode) obj;
            nodes[i++]=treeNode;
            if(pid==null){
                pid=treeNode.getPid();
            }else if(pid!=treeNode.getPid()){
                pid_nums++;
            }
            if(treeId==null){
                treeId=treeNode.getTree_id();
            }else if(!treeId.equals(treeNode.getTree_id())){
                treeid_nums++;
            }
        }
        if(treeId==null){
            throw new NullPointerException("必须指定挂在那颗树！");
        }
        if(treeid_nums>1){
            throw new RuntimeException("只能在一颗树，但当前发现树为："+ treeid_nums);
        }
        if(pid==null){
            throw new NullPointerException("必须指定新增挂在那个父节点下！");
        }
        if(pid_nums>1){
            throw new RuntimeException("只能在一个节点下新增多个节点，但当前发现父节点数为："+ pid_nums);
        }
        Integer[] resultNodeIds=this.insertPathUnderNodeId(treeId,pid, nodes.length);
        i=0;
        for(Object obj:treeNodes){
            TreeNode treeNode= (TreeNode) obj;
            treeNode.setTn_id(resultNodeIds[i++]);
        }
        return null;
    }

    @Override
    public Object doAfterInsert(List<Map<String, Object>> datas) {
        return null;
    }

    @Override
    public Object doBeforeUpdate(List<Map<String, Object>> datas) {
        return null;
    }

    public Integer[] insertPathUnderNodeId(String treeId, Integer pid,Integer nodeNum){
        moDao.setBindTable(()->"TREE_NODE");
        SelectSql dml=new SelectSql(TreeNode.class, Cond.eq("TREE_ID",treeId));
        dml.addQueryColumns("MAX(TN_ID) MAXID");
        List<Map> treeId_maxId_list=moDao.queryList(dml);//找到树最大的节点id
        Integer[] resultNodeIds=new Integer[nodeNum];
        if(treeId_maxId_list.size()==1){
            Integer curMaxId= (Integer) treeId_maxId_list.get(0).get("MAXID");
            //找到父节点的所有祖先节点
            dml=new SelectSql(TreePath.class, Cond.eq("TREE_ID",treeId).and(Cond.eq("DESCENDANT",pid)));
            dml.addQueryColumns("ANCESTOR","DISTANCE");
            List<Map> ancestorList=moDao.queryList(dml);
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
            moDao.setBindTable(()->"TREE_PATH").insert(TreePath.class,paths);
        }
        return resultNodeIds;
    }

    @Override
    public Object doAfterUpdate(List<Map<String, Object>> datas) {
        return null;
    }

    @Override
    public Object doBeforeDelete(Serializable... serializables) {
        //需要同时删除所有树路径关系
        moDao.setBindTable(()->"tree_path");
        moDao.delete(Cond.in("ANCESTOR",serializables).or(Cond.in("DESCENDANT",serializables)));
        return null;
    }

    @Override
    public Object doAfterDelete(Serializable... serializables) {
        return null;
    }

    @Override
    public Object doBeforeSelect(Serializable id) {
        return null;
    }

    @Override
    public Object doAfterSelect(Map<String, Object> data) {
        return null;
    }

    @Override
    public Function<String, Object> doBeforeQuery(List<String> selfQueryCols) {
        return null;
    }

    @Override
    public Object doAfterQuery(List<Map> datas) {
        return null;
    }
}
