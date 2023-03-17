package com.hcxinan.sys.logs;


import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 加载日志的配置信息
 * 
 * @author huangbin
 *
 */
public class LogConfigs {

	private final String configPath;

	private Properties properties = new Properties();

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private boolean isDebugMode = false;
	/**
	 * 文件的修改信息
	 */
	private Map<String, Long> fileModifers = new HashMap<>();

	//private static Logger logger = LoggerFactory.getLogger(LogConfigs.class);

	public LogConfigs(String configPath) throws IOException {
		this.configPath = configPath;
		isDebugMode = isDebug();
		initLoad();
	}

	private void initLoad() throws IOException {
		Resource[] resources = resourcePatternResolver.getResources(configPath);
		for (Resource resource : resources) {
			long modified = resource.lastModified();
			String idStr = resource.getFilename();
			boolean contained = fileModifers.containsKey(idStr);
			if (!contained || (contained && fileModifers.get(idStr) < modified)) {
				fileModifers.put(idStr, modified);
				InputStream is = resource.getInputStream();
				try {
					properties.load(new InputStreamReader(is, Charset.forName("UTF-8")));
				} finally {
					is.close();
				}
			}
		}
	}
/**
 * 判断是否在DEBUG模式下面，一般是开发人员，如果在调试模式下面可以加载一些资源
 * @return
 */
	public static boolean isDebug() {
		List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
		for (String arg : args) {
			if (arg.startsWith("-Xrunjdwp") || arg.startsWith("-agentlib:jdwp")) {
				return true;
			}
		}
		return false;
	}

	public String getConfig(String key) {
		/**
		 * 开发环境在调试情况下面，自动检查日志的更新情况
		 */
		if (isDebugMode) {
			try {
				initLoad();
			} catch (IOException e) {
				//logger.error("调试模式下加载资源文件失败 :" + this.configPath, e);
			}
		}
		return properties.getProperty(key);
	}

	public int getSize() {
		return this.properties.size();
	}

	public boolean isDebugMode() {
		return isDebugMode;
	}

}
