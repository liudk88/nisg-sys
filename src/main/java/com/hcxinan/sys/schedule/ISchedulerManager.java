package com.hcxinan.sys.schedule;

import com.hcxinan.core.inte.system.ISchedule;

import java.util.List;

/**
 * @author liudk 
 * @Description: 任务调度管理接口。
 * @date 21-9-21 上午8:20
 */
public interface ISchedulerManager {

//    SysSchedule selectSchedule(String scheduleId);

    /**
     * 运行一个任务
     *
     * @throws Exception
     */
    void run(String scheduleId) throws Exception;

    /**
     * 停止定时任务。
     *
     * @throws Exception
     */
    void stop(String scheduleId) throws Exception;

    /**
     * 启动定时任务。
     *
     * @throws Exception
     */
    void start(String scheduleId) throws Exception;

    /**
     * 添加一个任务。
     * @return
     * @throws Exception
     */
    boolean addJob(String scheduleId) throws Exception;


    void updateCron(String scheduleId,String cronStr);

    /**
     * 取得当前正在执行的任务实例。
     *
     * @throws Exception
     */

//    public List getCurrentlyExecutingJobs() throws Exception;

    /**
     * 取得任务名称列表。
     *
     * @return
     * @throws Exception
     */
//    public List<String> getJobNameList() throws Exception;



    /**
     * 删除指定名称的任务。
     *
     * @param jobName
     *            任务名称
     * @return
     * @throws Exception
     */
//    public boolean deleteJob(String jobName) throws Exception;

    /**
     * 暂停指定名称的任务。
     *
     * @param jobName
     *            任务名称
     * @throws Exception
     */
//    public void pauseJob(String jobName) throws Exception;

    /**
     * 重新运行暂停的任务。
     *
     * @param jobName
     *            任务名称
     * @throws Exception
     */
//    public void resumeJob(String jobName) throws Exception;

    /**
     * 用新的策略替换已有的任务。
     * @throws Exception
     */
//    public void replaceJob(SysSchedule scheduling) throws Exception;

    /**
     * 用新的策略替换已有的任务。
     *
     */
//    public boolean isJobExist(String jobName) throws Exception;

    /**
     * 方法描述:判断Job当前状态
     *
     */
//    public boolean isJobRuning(String jobName) throws Exception;

//    public int getJobState(String jobName) throws Exception;
}
