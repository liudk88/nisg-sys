package com.hcxinan.sys.schedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author liudk
 * @Description:
 * @date 21-9-20 上午9:26
 */
public class TestQuartz {

    private static Scheduler scheduler;

    public static void main(String[] args) throws SchedulerException, InterruptedException {
        String jobName = "job1";
        String group= "group1";

        addTask(jobName,group,"0/9 * * * * ? ");

        TimeUnit.SECONDS.sleep(10);
        updateTask(jobName,group,"0/5 * * * * ? ");
//        updateTask(jobName+"33",group,"0/5 * * * * ? ");//测试新增一个没有的

        /*// 获取Scheduler实例
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.run();
        System.out.println("scheduler.run");
        System.out.println("===========新增任务");


        //触发时间点. (每5秒执行1次.)
        *//*SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(5).repeatForever();*//*

        long time = System.currentTimeMillis() + 60*1000L;
        Date startTime = new Date(time);
        //定义一个Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withDescription("")
                .withIdentity(jobName, group)//定义name/group
//                .startAt(startTime)//加入scheduler后，在指定时间启动 (测试的时候发现这个是周期，会和cron重复，导致执行多次)
                //使用CronTrigger
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();

        //具体任务.
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(jobName,group).build();
        jobDetail.getJobDataMap().put("scheduleJob", new TestJob());//把调度任务实现类作为参数传入（后面获取再重新调用其任务方法）
        jobDetail.getJobDataMap().put("name","ldk");

        // 交由Scheduler安排触发
        scheduler.scheduleJob(jobDetail,trigger);*/

        /*TimeUnit.SECONDS.sleep(1000);

        //更新一个任务
        System.out.println("===========更新任务频率");
        if (!scheduler.checkExists(jobDetail.getKey())) {
//            addScheduleJob(jobName, group, cronStr, clazz);//不包含，重新执行新增
        } else {//已经存在了
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, group);
            CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            String cronStr="0/5 * * * * ?";//改成5秒一次
            if (Objects.isNull(trigger) || !cronStr.equals(cronTrigger.getCronExpression())) {
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr)
//                        .withMisfireHandlingInstructionDoNothing()
//                        .withMisfireHandlingInstructionIgnoreMisfires()
                        .withMisfireHandlingInstructionFireAndProceed()
                        ;
                //创建任务触发器
                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, group).
                        withSchedule(scheduleBuilder).build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        }
        TimeUnit.SECONDS.sleep(15);



        System.out.println("===========暂停任务(16ms)");
        scheduler.pauseJob(JobKey.jobKey(jobName, group));
        TimeUnit.SECONDS.sleep(16);

        System.out.println("===========重启任务");
        //重启任务会把之前暂停错过的任务一次过全部执行的
        scheduler.resumeJob(JobKey.jobKey(jobName, group));

        TimeUnit.SECONDS.sleep(15);

        System.out.println("===========删除任务");
        //关闭定时任务调度器.
        *//*scheduler.shutdown();
        System.out.println("scheduler.shutdown");*//*
        //移除任务.
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, group);
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(JobKey.jobKey(jobName, group));
        System.out.println("已移除任务");*/



    }

    private static void addTask(String jobName,String group,String cronStr) throws SchedulerException {
        System.out.println("add scheduler.run");
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        //触发时间点. (每5秒执行1次.)
        /*SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(5).repeatForever();*/

        long time = System.currentTimeMillis() + 60*1000L;
        Date startTime = new Date(time);

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr)
//                        .withMisfireHandlingInstructionDoNothing()
//                        .withMisfireHandlingInstructionIgnoreMisfires()
//                        .withMisfireHandlingInstructionFireAndProceed()
                ;

        //定义一个Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withDescription("")
                .withIdentity(jobName, group)//定义name/group
//                .startAt(startTime)//加入scheduler后，在指定时间启动 (测试的时候发现这个是周期，会和cron重复，导致执行多次)
                //使用CronTrigger
                .withSchedule(scheduleBuilder)
                .build();

        //具体任务.
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(jobName,group).build();
        jobDetail.getJobDataMap().put("scheduleJob", new TestJob());//把调度任务实现类作为参数传入（后面获取再重新调用其任务方法）
        jobDetail.getJobDataMap().put("name","ldk");

        // 交由Scheduler安排触发
        scheduler.scheduleJob(jobDetail,trigger);
    }

    private static void updateTask(String jobName,String group,String cronStr) throws SchedulerException {
        //更新一个任务
        JobKey key=new JobKey(jobName,group);

        System.out.println("===========更新任务频率");
        if (!scheduler.checkExists(key)) {//是个新的调度，重新执行新增
            addTask(jobName,group,cronStr);
        } else {//已经存在了
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, group);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr)
//                        .withMisfireHandlingInstructionDoNothing()
//                        .withMisfireHandlingInstructionIgnoreMisfires()
//                        .withMisfireHandlingInstructionFireAndProceed()
                    ;
            //创建任务触发器
            Trigger newTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, group).
                    withSchedule(scheduleBuilder).build();
            if(scheduler.checkExists(triggerKey)){//已经存在
                scheduler.rescheduleJob(triggerKey, newTrigger);
            }else{//不存在触发器增加
                JobDetail jobDetail=scheduler.getJobDetail(key);
                // 交由Scheduler安排触发
                scheduler.scheduleJob(jobDetail,newTrigger);
            }
        }
    }
}
