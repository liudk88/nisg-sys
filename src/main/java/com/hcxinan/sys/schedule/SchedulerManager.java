package com.hcxinan.sys.schedule;

import com.alibaba.fastjson.JSON;
import com.hcxinan.core.inte.system.ISchedule;
import com.hcxinan.sys.util.PlatUtil;
import com.morph.cond.Cond;
import com.morph.db.IMoDao;

import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liudk
 * @Description: 任务调度管理类
 * @date 21-9-21 上午8:22
 */
@Component
public class SchedulerManager implements ISchedulerManager {
    private static final Logger log = LoggerFactory.getLogger(SchedulerManager.class);

    private Scheduler scheduler;
    @Autowired
    private PlatUtil platUtil;

    @Autowired
    private IMoDao dao;
    /**
     *@Description 初始化加载系统调度任务,对于使用cron表达式的调度来说，如果设为每个多少秒跑一次的话，如9秒(0/9 * * * * ?)，
     * 那么依次跑的点为 9s,18s,27s,36s,45s,54s,00s,quartz会把一分钟内最后满足不到9s的部分跑一次，它的含义是每分钟内跑间隔几秒
     * 跑一次，最后不满间隔的再跑一次，而不是说把时间当成是连续的，间隔多少秒跑一次
     *@Param []
     *@Return void
     *@Author liudk
     *@DateTime 21-9-30 上午11:29
    */
    @PostConstruct
    public void loadAll() throws SchedulerException {
        log.info("初始化加载系统调度任务：");
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        //查询所有的调度
        List<SysSchedule> scheduleList= dao.setBindTable(()->"SYS_SCHEDULE").queryBeanList(SysSchedule.class,
                Cond.eq("VALID",1).and(Cond.eq("ENABLED",1)),null,null);
        scheduleList.forEach(sc-> {
            try {
                addJob(sc);
            } catch (SchedulerException e) {
                log.error("加载调度"+sc.getTaskname()+"("+sc.getTaskid()+")失败！");
            }
        });
        scheduler.start();
    }

    /*@Override
    public SysSchedule selectSchedule(String scheduleId) {
        return dao.setBindTable(()->"SYS_SCHEDULE").select(SysSchedule.class,scheduleId);
    }*/

    @Override
    public void run(String scheduleId) {
        SysSchedule schedule= dao.setBindTable(()->"SYS_SCHEDULE").select(SysSchedule.class,scheduleId);
        if(schedule!=null){
            ISchedule task=getScheduleTask(schedule);
            String jsonStr=schedule.getJson_param();
            Map param=null;
            if(StringUtils.isNotBlank(jsonStr)){
                try{
                    param= JSON.parseObject(jsonStr,Map.class);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            task.run(param);
            Map<String,Object> updateMap=new HashMap<>();
            updateMap.put("TASKID",scheduleId);
            if(schedule.getRuntimes()==null){
                updateMap.put("RUNTIMES",1);
            }else{
                updateMap.put("RUNTIMES",schedule.getRuntimes()+1);
            }
            dao.updateByPk(updateMap);
        }
    }

    @Override
    public void stop(String scheduleId) throws Exception {
        log.info("禁用调度："+scheduleId);
        Map<String,Object> updateMap=new HashMap<>();
        updateMap.put("TASKID",scheduleId);
        updateMap.put("ENABLED",0);
        dao.updateByPk(updateMap);

        /*
        * 通过移除的方式更符合我们的业务认知，如果采用quartz的停止和重启的话会存在一个问题，
        * 就是重启后它会把暂停期间所有没有执行的次数一次过再执行
        * */
        TriggerKey triggerKey = getTriggerKey(scheduleId);
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(getJobKey(scheduleId));
    }

    @Override
    public void start(String scheduleId) throws Exception {
        log.info("开启调度："+scheduleId);
        Map<String,Object> updateMap=new HashMap<>();
        updateMap.put("TASKID",scheduleId);
        updateMap.put("ENABLED",1);
        dao.updateByPk(updateMap);

        addJob(scheduleId);
    }

    @Override
    public void updateCron(String scheduleId,String cronStr) {
        //更新一个任务
        JobKey key=getJobKey(scheduleId);
        log.info("更新调度:scheduleId="+scheduleId+",cronStr="+cronStr);
        try {
            if (!scheduler.checkExists(key)) {//是个新的调度，重新执行新增
                SysSchedule schedule= dao.setBindTable(()->"SYS_SCHEDULE").select(SysSchedule.class,scheduleId);
                log.info("原来没有，新增！");
                addJob(schedule);
            } else {//已经存在了
                log.info("原来有，更新！");
                TriggerKey triggerKey = getTriggerKey(scheduleId);

                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr)
    //                        .withMisfireHandlingInstructionDoNothing()
    //                        .withMisfireHandlingInstructionIgnoreMisfires()
    //                        .withMisfireHandlingInstructionFireAndProceed()
                        ;
                //创建任务触发器
                Trigger newTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).
                        withSchedule(scheduleBuilder).build();
                if(scheduler.checkExists(triggerKey)){//已经存在
                    scheduler.rescheduleJob(triggerKey, newTrigger);
                }else{//不存在触发器增加
                    JobDetail jobDetail=scheduler.getJobDetail(key);
                    // 交由Scheduler安排触发
                    scheduler.scheduleJob(jobDetail,newTrigger);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean addJob(String scheduleId) throws Exception {
        SysSchedule schedule=dao.setBindTable(()->"SYS_SCHEDULE").select(SysSchedule.class,scheduleId);
        return addJob(schedule);
    }

    public boolean addJob(SysSchedule schedue) throws SchedulerException {
        /*long time = System.currentTimeMillis() + 60*1000L;
        Date startTime = new Date(time);*/

        Trigger trigger = TriggerBuilder.newTrigger()
                .withDescription("")
                .withIdentity(getTriggerKey(schedue.getTaskid()))//定义name/group
//                .startAt(startTime)//加入scheduler后，在指定时间启动
                //使用CronTrigger
                .withSchedule(CronScheduleBuilder.cronSchedule(schedue.getCron()))
//                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(getJobKey(schedue.getTaskid())).build();
        /*String jsonStr=schedue.getJson_param();
        Map param=null;
        if(StringUtils.isNotBlank(jsonStr)){
            try{
                param= JSON.parseObject(jsonStr,Map.class);
                if(param!=null){
                    jobDetail.getJobDataMap().putAll(param);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
//        jobDetail.getJobDataMap().put("scheduleJob", getScheduleTask(schedue));//把调度任务实现类作为参数传入（后面获取再重新调用其任务方法）
//        jobDetail.getJobDataMap().put("schedue", schedue);//把原来色调度信息传入
        jobDetail.getJobDataMap().put("scheduleId", schedue.getTaskid());//把原来色调度id传入
        // 交由Scheduler安排触发
        scheduler.scheduleJob(jobDetail,trigger);
//        test();
        return true;
    }

    private ISchedule getScheduleTask(SysSchedule schedue){
        ISchedule schedule=null;
        if(schedue.getClass_loader_type()==0){
            schedule= (ISchedule) platUtil.getBean(schedue.getJobclass());
        }else if(schedue.getClass_loader_type()==1){
            try {
                Class clazz = Class.forName(schedue.getJobclass());
                schedule = (ISchedule) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(schedule==null){
            throw new NullPointerException(schedue.getTaskname()+"("+schedue.getTaskid()+")没有找到可运行的调度");
        }
        return schedule;
    }

    private TriggerKey getTriggerKey(String taskId){
        return new TriggerKey(taskId, taskId+"Group");
    }

    private JobKey getJobKey(String taskId){
        return new JobKey(taskId, taskId+"Group");
    }

    /*@Override
    public boolean deleteJob(String jobName) throws Exception {
        return false;
    }

    @Override
    public void pauseJob(String jobName) throws Exception {

    }

    @Override
    public void resumeJob(String jobName) throws Exception {

    }

    @Override
    public void replaceJob(SysSchedule scheduling) throws Exception {

    }

    @Override
    public boolean isJobExist(String jobName) throws Exception {
        return false;
    }

    @Override
    public boolean isJobRuning(String jobName) throws Exception {
        return false;
    }

    @Override
    public int getJobState(String jobName) throws Exception {
        return 0;
    }*/

    //    @Override
//    public List getCurrentlyExecutingJobs() throws Exception {
//        return null;
//    }
//
//    @Override
//    public List<String> getJobNameList() throws Exception {
//        return null;
//    }
}
