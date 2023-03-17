package com.hcxinan.sys.logs;

import java.lang.annotation.*;

/**
 * 标识日志的主类别目录字符
 * @author huangbin
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogCatalog {
	
	String value();
	
}
