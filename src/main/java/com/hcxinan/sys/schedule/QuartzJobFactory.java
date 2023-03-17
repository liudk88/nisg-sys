package com.hcxinan.sys.schedule;

import com.hcxinan.core.inte.system.ISchedule;
import com.hcxinan.sys.util.PlatUtil;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author liudk
 * @Description: 任务调度。继承Job，在定时任务中执行的是其调度参数里面的ISchedule实现类。
 * 因为JobDetail中需要Job的实现类的class作为参数，而不是具体的javabean，所以这样就会导致无法用spirngbean，
 * 但如果在外封装一层（即QuartzJobFactory），而把spirngbean或javabean作为参数传入，调用其run方法
 * @date 21-9-21 上午8:32
 */
//@DisallowConcurrentExecution
public class QuartzJobFactory implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzJobFactory.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("执行调度任务 -- 开始！");
        Map paramMap=new HashMap(context.getMergedJobDataMap());
        String scheduleId= (String) paramMap.get("scheduleId");

        ISchedulerManager schedulerManager= (ISchedulerManager) PlatUtil.getBean("schedulerManager");
        if(schedulerManager!=null){
            try {
                schedulerManager.run(scheduleId);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                log.info("执行调度任务 -- 结束！");
            }
        }

      /*Map paramMap=new HashMap(context.getMergedJobDataMap());
        ISchedule schedule = (ISchedule) paramMap.get("scheduleJob");
        Objects.requireNonNull(schedule,"没有找到调度任务！");
        paramMap.remove("scheduleJob");//把自己去掉
        try{
            schedule.run(paramMap);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            log.info("执行调度任务 -- 结束！");
        }*/
    }
}
