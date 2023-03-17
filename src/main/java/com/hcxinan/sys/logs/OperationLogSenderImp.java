package com.hcxinan.sys.logs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component("operationLogSenderImp2")
public class OperationLogSenderImp implements OperationLogSender {

	@Autowired
	private ApplicationEventPublisher publisher;
	
	//private static Logger logger = LoggerFactory.getLogger(OperationLogSenderImp.class);
	

    @PostConstruct
    public void setUp(){

    }
	
	@Override
	public void sendOperationLog(String user, String hostIP, String menuName, String operateContent,
                                 OperationType operationType, Date operateTime, HttpServletRequest request) {
		this.sendOperationLog(user, hostIP, menuName, operateContent, operationType.getName(), operateTime,request);
	}


	@Override
	public void sendOperationLog(String user, String hostIP, String menuName, String operateContent,
                                 String operationType, Date operateTime, HttpServletRequest request) {
		OprationLogEvent event = new OprationLogEvent(user,hostIP,operateTime,menuName,operationType,operateContent,request);
		this.publisher.publishEvent(event);
	}

}
