package com.hcxinan.sys.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcxinan.sys.model.NisgAlllogs;
import com.hcxinan.sys.model.NisgMenu;
import com.hcxinan.sys.service.MenuService;
import com.hcxinan.sys.service.NisgAlllogsService;
import com.hcxinan.sys.util.PlatUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("OperationLogListener3")
public class OperationLogListener {

	private final static Logger logger = Logger.getLogger(OperationLogListener.class);
	/**
	 * 懒加载，如果没有前面没有使用，就不启线程池
	 */
	private static ExecutorService executor = null;

	private String mName = null;

//	@Autowired
	private MenuService menuService;

//	@Autowired
	private NisgAlllogsService nisgAlllogsService;


	/**
	 * 异步消息 异步注解无效
	 * @param event
	 */
	@EventListener(OprationLogEvent.class)
	public void processOperationLogListEvent(OprationLogEvent event){
		System.out.println("系统监听器3监听中。。。。。。。。。。");
		NisgAlllogs bean = new NisgAlllogs();
		initLogBean(bean,event);

		if(executor == null){
			executor = Executors.newFixedThreadPool(5);
		}
		executor.submit(()->{
			try {
				nisgAlllogsService.insertLog(bean);
			}catch (Exception e){
				e.printStackTrace();
			}
		});
	}

	//初始化日志实体类
	private void initLogBean(NisgAlllogs bean, OprationLogEvent event){
		HttpServletRequest request = event.getRequest();
		if(bean != null){
			Map params = null;
			if(request.getParameterMap() != null){
				params = request.getParameterMap();
			}
			if(params != null){
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonString = null;
				try {
					jsonString = objectMapper.writeValueAsString(params);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				bean.setContents(jsonString);
			}
			String user = event.getUser();
			if(user != null){
				bean.setAccount(user);
			}

			String remoteIp = getIPAddress(request);

			InetAddress address = null;
			try {
				address = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			if (address != null){
				bean.setWorker(address.getHostName());
				bean.setWorkip(address.getHostAddress());
			}
			bean.setLogid(PlatUtil.getId());
			String uri = request.getRequestURI();
			bean.setSid("sid");
			bean.setPlatid("platid");
			bean.setCjsj(new Date());
			bean.setAddr(remoteIp);
			bean.setMac("mac");
			bean.setLogtime(new Date());
			bean.setUrl(uri);
			bean.setParams(request.getQueryString());
			bean.setStatus(1);
			//bean.setErrors("ERRORS");
			//默认操作成功
			bean.setOpresult(1);
			bean.setSfyx(1);
			bean.setXgsj(new Date());
			//操作描述

			bean.setDescr(event.getMenuName());
			if (request.getAttribute("logLevel") != null){
				//Integer logLevel = (Integer) request.getAttribute("logLevel");
				Integer logLevel = Integer.parseInt(request.getAttribute("logLevel").toString());
				bean.setLog_level(logLevel);
			}
			//业务路径
			String input = bean.getUrl();
			if (input != null){
				String contents = bean.getContents();
				if(contents != null){
					try{
						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jsonNode = objectMapper.readTree(contents).get("menuId");
						String menuId = null;
						if (jsonNode.isArray()){
							JsonNode jn = jsonNode.get(0);
							menuId = jn.asText();
						}else {
							menuId = jsonNode.asText();
						}
						NisgMenu menu = menuService.getMenuName(menuId);
						mName = menu.getCdmc();
						getMenuName(menu.getPcdid());

						bean.setMenu(mName);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	//迭代循环
	private void getMenuName(String menuId){
		NisgMenu menu = menuService.getMenuName(menuId);
		mName = mName + ">" + menu.getCdmc();
		if (menu.getPcdid() != null){
			getMenuName(menu.getPcdid());
		}
	}

	//获取访问IP
	public String getIPAddress(HttpServletRequest request) {
		String ip = null;

		//X-Forwarded-For：Squid 服务代理
		String ipAddresses = request.getHeader("X-Forwarded-For");

		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			//Proxy-Client-IP：apache 服务代理
			ipAddresses = request.getHeader("Proxy-Client-IP");
		}

		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			//WL-Proxy-Client-IP：weblogic 服务代理
			ipAddresses = request.getHeader("WL-Proxy-Client-IP");
		}

		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			//HTTP_CLIENT_IP：有些代理服务器
			ipAddresses = request.getHeader("HTTP_CLIENT_IP");
		}

		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			//X-Real-IP：nginx服务代理
			ipAddresses = request.getHeader("X-Real-IP");
		}

		//有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
		if (ipAddresses != null && ipAddresses.length() != 0) {
			ip = ipAddresses.split(",")[0];
		}

		//还是不能获取到，最后再通过request.getRemoteAddr();获取
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
