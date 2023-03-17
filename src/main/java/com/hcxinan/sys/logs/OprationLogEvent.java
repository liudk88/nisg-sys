package com.hcxinan.sys.logs;

import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 日志事件,在Spring的上下文事件进行发送
 * @author huangbin
 *
 */
public class OprationLogEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6107121925012855727L;

	
	/**
	 * 操作用户
	 */
	private String user;
	/**
	 * 客户访问IP
	 */
	private String hostIP;
	/**
	 * 操作时间
	 */
	private Date operateTime;
	/**
	 * 菜单名称,并非一定要系统的配置的菜单名称
	 */
	private String menuName;
	/**
	 * 操作类型
	 */
	private String operationType;
	/**
	 * 操作内容
	 */
	private  String operateContent;

	/**
	 *HttpServletRequest对象
	 */
	private HttpServletRequest request;
	
	
	public OprationLogEvent(String user, String hostIP, Date operateTime, String menuName, String operationType,
                            String operateContent, HttpServletRequest request) {
		super(user);
		this.user = user;
		this.hostIP = hostIP;
		this.operateTime = operateTime;
		this.menuName = menuName;
		this.operationType = operationType;
		this.operateContent = operateContent;
		this.request = request;
	}

	public OprationLogEvent(String user, String hostIP, Date operateTime, String menuName, String operationType,
                            String operateContent) {
		super(user);
		this.user = user;
		this.hostIP = hostIP;
		this.operateTime = operateTime;
		this.menuName = menuName;
		this.operationType = operationType;
		this.operateContent = operateContent;
	}


	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHostIP() {
		return hostIP;
	}


	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}


	public Date getOperateTime() {
		return operateTime;
	}


	public void setOperateTime(Date operateTime) {
		this.operateTime = operateTime;
	}


	public String getMenuName() {
		return menuName;
	}


	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}


	public String getOperationType() {
		return operationType;
	}


	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}


	public String getOperateContent() {
		return operateContent;
	}


	public void setOperateContent(String operateContent) {
		this.operateContent = operateContent;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return " menuName="
				+ menuName + ", operationType=" + operationType + ", operateContent=" + operateContent + "]";
	}
	
	
}
