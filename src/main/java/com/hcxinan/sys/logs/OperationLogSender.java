package com.hcxinan.sys.logs;


import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 操作日志的发送者
 * @author huangbin
 *
 */
public interface OperationLogSender {
	
	/**
	 * 发送一个操作日志，具体的实现由后面的侦听器进行实现，如存放 到数据库，或者发送给第三方日志接收系统
	 * @param user  操作人
	 * @param hostIP  访问请求的IP地址
	 * @param menuName 菜单名称（不一定是系统的菜单的名称）
	 * @param operateContent  操作内容，尽量以这种格式编写      用户名XXX<对>系统XXX菜单XXX<进行>操作XXX
	 * @param operationType 操作类型
	 * @param operateTime 操作时间
	 */
	public void sendOperationLog(String user, String hostIP, String menuName, String operateContent, OperationType operationType, Date operateTime, HttpServletRequest request);
	
	/**
	 * 操作类型可以使用字符串定义
	 * @param user
	 * @param hostIP
	 * @param menuName
	 * @param operateContent
	 * @param operationType
	 * @param operateTime
	 */
	public void sendOperationLog(String user, String hostIP, String menuName, String operateContent, String operationType, Date operateTime, HttpServletRequest request);
	
	
}
