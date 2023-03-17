package com.hcxinan.sys.schedule;

import com.hcxinan.core.inte.system.ISchedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author liudk
 * @Description:
 * @date 21-9-24 下午12:19
 */
public class TestJob implements ISchedule {
    @Override
    public void run(Map<String, String> params) {
        SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("========"+fmt.format(new Date())+":调度执行了\n");
    }
}
