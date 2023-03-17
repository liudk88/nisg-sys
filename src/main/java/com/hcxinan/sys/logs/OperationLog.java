package com.hcxinan.sys.logs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationLog {

	/**
	 *属性文件中的配置属性，环境支持SPEL表达式进行配置
	 * 
	 * 绑定对象
	 *  user ->登陆的用户
	 *  params -> request.getParameter()
	 *  attrs -> request.getAttributes()
	 *  其它的环境变量，在后面设置
	 * @return
	 */
	String value() default "";
	/**
	 * 操作类型，可以不使用，在后面的表达式中自定义
	 * @return
	 */
	OperationType type() default OperationType.undefine;
	
	
}
