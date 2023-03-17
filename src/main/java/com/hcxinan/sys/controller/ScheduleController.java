package com.hcxinan.sys.controller;

import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.schedule.SchedulerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liudk
 * @Description:
 * @date 21-9-29 下午6:17
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private SchedulerManager schedulerManager;

    @GetMapping("/{scheduleId}/run")
    public JsonResult run(@PathVariable("scheduleId") String scheduleId){
        try {
            schedulerManager.run(scheduleId);
            return JsonResult.success();
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.error();
    }

    @GetMapping("/stop/{scheduleIds}")
    public JsonResult disabled(@PathVariable("scheduleIds") String... scheduleIds){
        try {
            for(String scheduleId:scheduleIds){
                schedulerManager.stop(scheduleId);
            }
            return JsonResult.success();
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.error();
    }
    @GetMapping("/start/{scheduleIds}")
    public JsonResult enabled(@PathVariable("scheduleIds") String... scheduleIds){
        try {
            for(String scheduleId:scheduleIds){
                schedulerManager.start(scheduleId);
            }
            return JsonResult.success();
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.error();
    }
}
