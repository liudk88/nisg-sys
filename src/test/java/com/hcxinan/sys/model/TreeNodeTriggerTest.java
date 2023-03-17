/* TODO
 * @description:当前是集成测试,以后重新构建maven目录后,要迁移到集成测试对应的包中
 * @author:liudk
 * @cdate:上午11:18 20-12-9
 * @developers:liudk
 * @finishVersion:1.0.0
 */
package com.hcxinan.sys.model;

import com.morph.cond.Cond;
import com.morph.db.DyanDao;
import com.morph.db.DynaDaoFactory;
import com.morph.dml.SelectSql;
import com.morph.model.OrderBy;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:applicationContext.xml"})
/*本测试类所有方法开启事务,并设置事务管理器,如果事务不回滚,那么当测试整个类的时候,每个测试方法对
* 数据的操作会互相影响,从而达很难控制预期效果
* */
@Transactional(transactionManager = "transactionManager")
/*事务设置为回滚*/
@Rollback(true)
public class TreeNodeTriggerTest {
    private static final Logger logger=Logger.getLogger(TreeNodeTriggerTest.class);

    private DynaDaoFactory daoFactory;

    /*@Autowired
    public void setDaoFactory(DynaDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.daoFactory.putTrigger("tree_node",new TreeNodeTrigger());
    }*/

    private static final String TREE_ID = "2012057ncpmoshy83809";

    @Test
    public void insertNode(){
        logger.info("测试在树下新增一个节点,TreeNodeTrigger会同步维护单位树!");
        DyanDao<TreeNode> dyanDao=daoFactory.getDynaDao(TreeNode.class);
        TreeNodeTrigger trigger=new TreeNodeTrigger();
        String tname="测试的单位";
        TreeNode treeNode=new TreeNode();
        treeNode.setTn_id(100);
        treeNode.setTree_id(TREE_ID);
        treeNode.setNode_name(tname);
        treeNode.setNode_type("1");
        treeNode.setSoure_table("t_rms_deptment");
        treeNode.setSource_pk_val("ORGID001");
        treeNode.setCdate(new Date());
        treeNode.setPid(24);//父节点是"广东市网信办"

        dyanDao.insert(treeNode);

        //1.树节点会增加一个
        TreeNode treeNode1=dyanDao.selectBean(treeNode.getTn_id());
        //这里不负责测试新增的功能(那是对morph框架的测试),只保证数据库有插入记录即可(触发器没影响到数据正常新增)
        assertThat(treeNode1.getNode_name()).isEqualTo(tname);
        assertThat(treeNode1.getTn_id()).isEqualTo(26);//上面设置100,这里测试主键是由触发器自己管理,不能由外部设置
        //2.节点关系
        DyanDao<TreePath> treePathDyanDao=daoFactory.getDynaDao(TreePath.class);
        List<TreePath> treePathList=treePathDyanDao.queryBeanList(Cond.eq("descendant",treeNode.getTn_id()),
                new OrderBy().asc("ancestor"),null);
        /*
        * 1.测试尽量要覆盖全面,符合我们预期的地方,每一处都要测试到
        * 2.预言尽量通过简单的方式校验(减少不必要的代码逻辑,避免因逻辑错误导致有问题)
        * */
        //按初始化的数据,直接配对我们想要的结果
        assertThat(treePathList.size()).isEqualTo(4);//依次的节点是广东省->广州市->广东市网信办->测试的单位
        //测试和广东省节点的关系
        assertThat(treePathList.get(0).getAncestor()).isEqualTo(1);
        assertThat(treePathList.get(0).getDescendant()).isEqualTo(26);
        assertThat(treePathList.get(0).getDistance()).isEqualTo(3);
        //测试和广州市节点的关系
        assertThat(treePathList.get(1).getAncestor()).isEqualTo(2);
        assertThat(treePathList.get(1).getDescendant()).isEqualTo(26);
        assertThat(treePathList.get(1).getDistance()).isEqualTo(2);
        //测试和广东市网信办节点的关系
        assertThat(treePathList.get(2).getAncestor()).isEqualTo(24);
        assertThat(treePathList.get(2).getDescendant()).isEqualTo(26);
        assertThat(treePathList.get(2).getDistance()).isEqualTo(1);
        //测试和自己的关系
        assertThat(treePathList.get(3).getAncestor()).isEqualTo(26);
        assertThat(treePathList.get(3).getDescendant()).isEqualTo(26);
        assertThat(treePathList.get(3).getDistance()).isEqualTo(0);
    }
    
    /* TODO
     * @description:以后是否有一次增加多个节点的情况,如果有,则需要补充测试
     * @author:liudk
     * @cdate:下午12:04 20-12-10
     * @developers:liudk
     * @finishVersion:xxx
     */
    //---------------------------------------- end ----------------------------------------

    @Test
    public void deleteNode(){
        logger.info("测试在树下删除两个节点,TreeNodeTrigger会同步维护单位树!");
        DyanDao<TreeNode> dyanDao=daoFactory.getDynaDao(TreeNode.class);
        /*
        * 节点数量减少2个不在本次测试范围,本次只测试树,减少的数量应该是:
        * 1.距离为0的2个(节点本身)
        * 2.距离为1的两个(两个节点都有父节点)
        * 3.距离为2的广东省教育厅是0,广东市网信办是1
        * 总计:2+2+1=5个
        * */
        SelectSql dml=new SelectSql(TreePath.class);
        dml.addQueryColumns("count(1) num");
        List<Map> numList=dyanDao.queryList(dml);
        Long num1= (Long) numList.get(0).get("NUM");
        dyanDao.deleteById(24,25);//删除广东市网信办 广东省教育厅
        numList=dyanDao.queryList(dml);
        Long num2= (Long) numList.get(0).get("NUM");
        assertThat(num1-num2).isEqualTo(5);
    }
}
