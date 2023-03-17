package com.hcxinan.sys.logs;


import com.hcxinan.sys.util.PlatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogMethodIInterceptor implements HandlerInterceptor {

    private static final long serialVersionUID = 7541955454448863410L;


    private OperationLogSender sender;

    private LogConfigs logConfigs;

    private ExpressionParser parser = new SpelExpressionParser();

    private ParserContext parserContext = new TemplateParserContext();

    Map<String,String> fvMap = new HashMap<>();

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            this.logConfigs = new LogConfigs("classpath:config/oplogs/*.properties");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("加载操作日志配置文件出错",e);
        }
        initMap();
        //从容器中获取
        sender = (OperationLogSenderImp)PlatUtil.getApplicationContext().getBean("operationLogSenderImp2");

        try {
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Method currentMethod = handlerMethod.getMethod();
            String methodName = currentMethod.getName();
            OperationLog logTag = null;
            if ((logTag = currentMethod.getAnnotation(OperationLog.class)) != null) {
                String httpMethod = request.getMethod();
                String configKey = logTag.value();
                /*
                 * 使用注解头来配置
                 */
                if(StringUtils.isEmpty(configKey)){
                    //拼接controller + methodName
                    String s = currentMethod.getDeclaringClass().getName().toString();
                    String[] str = s.split("\\.");
                    String className = str[str.length -1 ];
                    configKey = className  + "." + methodName;
                }

                String template = logConfigs.getConfig(configKey);
                if(StringUtils.isEmpty(template)){
                    //logger.warn(" 日志拦截："+currentMethod+" @OperationLog : 属性键 "+configKey+" 未加载到值");
                }else{
                    String user = request.getParameter("user");
                    String host = request.getRemoteAddr();
                    //使用代理的IP地址
                    if(request.getHeader("x-forwarded-for") != null){
                        host = request.getRemoteAddr();
                    }
                    if(true){
                        this.sendLogs(user, logTag, template, httpMethod, methodName,host,request);
                    }
                    //如果没用用户，在这里默认不进行记录
                }
            }
        } catch (Exception e) {
            //logger.error("记录操作日志失败", e);
            e.printStackTrace();
        }

    }

    private void sendLogs(String user, OperationLog config, String template, String httpMethod, String callMethodName, String host, HttpServletRequest request){
        boolean isExpTemplate = template.indexOf("#{") > -1;
        String parseText = template;
        if(isExpTemplate == true){
            EvaluationContext context = new StandardEvaluationContext();

            context.setVariable("user", user);
            //当前调用的 action 实例，如果有值需要传入到日志模板中，可以增加值
            context.setVariable("method", callMethodName);
            context.setVariable("isPost", "POST".equalsIgnoreCase(httpMethod));



            //应急事件特殊处理
            String descr = null;
            String parameter = request.getParameter("type");
            if (!("".equals(parameter)) && parameter != null){
                for (Map.Entry<String,String> entry : fvMap.entrySet()){
                    if (entry.getKey().equals(parameter)){
                        descr = entry.getValue();
                        break;
                    }
                }
                context.setVariable("result",descr);
            }

            //安全制度特殊处理
            String rState = request.getParameter("state");
            if (!("".equals(rState)) && rState != null){
                if (rState.equals("1")){
                    context.setVariable("rState",true);
                    context.setVariable("kState",true);
                }else {
                    context.setVariable("rState",false);
                    context.setVariable("kState",false);
                }
            }
            //风险评估的特殊处理
            String dealType = request.getParameter("dealType");
            if (!("".equals(dealType)) && dealType != null){
                if (dealType.equals("0")){
                    context.setVariable("dealType",true);
                }else {
                    context.setVariable("dealType",false);
                }
            }


            try{
                Expression expression = parser.parseExpression(template,parserContext);
                parseText =  expression.getValue(context, String.class);
            }catch(Exception e){
                //logger.error("解释SEPL模板出错："+template,e);
                return;
            }
        }
        String[] args = parseText.split("[|]");
        if(args.length <= 2 || (config.type() == OperationType.undefine && args.length < 3)){
            //logger.error(action.getClass().getName()+"::"+callMethodName+"::"+config+"\n错误的日志配置格式  菜单名称|操作内容[|操作类型] ，必须|分隔："+parseText);
        }else{

            String menuName = args[0].trim();
            String operateContent = args[1].trim();
            if (args[2].trim() != null){
                request.setAttribute("logLevel",args[2].trim());
            }
            String operationType = config.type() == OperationType.undefine ? args[2].trim() : config.type().getName();
           try {
               sender.sendOperationLog(user, host, menuName, operateContent, operationType, new Date(),request);
           }catch (Exception e){
               e.printStackTrace();
           }
        }
    }

    public void initMap(){
        fvMap.put("appear","上报");
        fvMap.put("submit","提交");
        fvMap.put("archive","归档");
    }
}
