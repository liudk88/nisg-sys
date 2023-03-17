package com.hcxinan.sys.util.ip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 匹配IP 段，转换成单个IP进行比较
 * 
 * @author huangbin
 *
 */
@Deprecated //请使用core中对应的类，后面废除
public class IpCompareImp {

	private final List<String> exprs;

	private List<IPScope> ipScopes;
	/**
	 * 192.168.1.1
	 */
	public static final String IP_SIGLE_REG = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])";

	/**
	 * 192.168.1.*
	 */
	public static final String IP_SIGLE_STAR_REG = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.\\*";

	/**
	 * 192.168.1.1-10
	 */
	public static final String IP_SCOPE_SIMPLE_REG = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])[\\-](\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])";

	/**
	 * 子网掩码 192.168.1.0/24
	 */
	public static final String IP_SCOPE_SIMPLE_MASK = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])[\\/]([1-9]|[1-2][0-9]|3[0-1])";

	/**
	 * 192.168.1.1-192.168.1.10
	 */
	public static final String IP_SCOPE_FULL_REG = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\-\\1\\.\\2\\.\\3.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])";

	public static final Pattern single_pattern = Pattern.compile(IpCompareImp.IP_SIGLE_REG);
	public static final Pattern star_pattern = Pattern.compile(IpCompareImp.IP_SIGLE_STAR_REG);
	public static final Pattern line_simple_pattern = Pattern.compile(IpCompareImp.IP_SCOPE_SIMPLE_REG);
	public static final Pattern line_full_pattern = Pattern.compile(IpCompareImp.IP_SCOPE_FULL_REG);
	public static final Pattern mask_pattern = Pattern.compile(IpCompareImp.IP_SCOPE_SIMPLE_MASK);

	/**
	 * 支持以下几种格式的IP地址 192.168.1.1 192.168.1.0/24 192.168.1.1-192.168.1.10
	 * 192.168.1.1-10 192.168.1.*
	 * 
	 * @param exprs
	 */
	public IpCompareImp(List<String> exprs) {
		this.exprs = exprs;
		init();
	}

	void init() {
		ipScopes = new ArrayList<>();
		for (String ipExp : this.exprs) {
			IPScope scope = compileIpExp(ipExp);
			if (scope != null) {
				ipScopes.add(scope);
			}
		}
	}

	public static IPScope compileIpExp(String ipExp) {
		IPScope ipScopeObj = null;
		if (single_pattern.matcher(ipExp).matches()) {
			int ipValue = Ipv4Utils.ipToInt(ipExp);
			ipScopeObj = new IPScope(ipExp, ipValue, ipValue);
		} else if (star_pattern.matcher(ipExp).matches()) {
			int lastIndex = ipExp.lastIndexOf(".");
			String prefix = ipExp.substring(0, lastIndex);
			int ipValue = Ipv4Utils.ipToInt(prefix + ".0");
			int ipEnd = Ipv4Utils.ipToInt(prefix + ".255");
			ipScopeObj = new IPScope(ipExp, ipValue, ipEnd);
		} else if (line_simple_pattern.matcher(ipExp).matches()) {
			int lastIndex = ipExp.lastIndexOf(".");
			String prefix = ipExp.substring(0, lastIndex);
			String ipScope = ipExp.substring(lastIndex + 1);
			String[] dotIp = ipScope.split("[\\-]");
			if (dotIp.length == 2) {
				int ipValue = Ipv4Utils.ipToInt(prefix + "." + dotIp[0]);
				int ipEnd = Ipv4Utils.ipToInt(prefix + "." + dotIp[1]);
				ipScopeObj = new IPScope(ipExp, ipValue, ipEnd);
			}
		} else if (line_full_pattern.matcher(ipExp).matches()) {
			String[] dotIp = ipExp.split("[-]");
			String firstIp = dotIp[0];
			String sencondIp = dotIp[1];
			int ipValue = Ipv4Utils.ipToInt(firstIp);
			int ipEnd = Ipv4Utils.ipToInt(sencondIp);
			ipScopeObj = new IPScope(ipExp, ipValue, ipEnd);
		} else if (mask_pattern.matcher(ipExp).matches()) {
			int[] ips = Ipv4Utils.getIPIntScope(ipExp);
			ipScopeObj = new IPScope(ipExp, ips[0], ips[1]);
		}
		return ipScopeObj;
	}

	public boolean matching(String ip) {
		if (this.exprs.contains(ip)) {
			return true;
		}
		/**
		 * 如果是正确的IP地址
		 */
		if (single_pattern.matcher(ip).matches()) {
			int ipValue = Ipv4Utils.ipToInt(ip);
			for (IPScope ipScope : this.ipScopes) {
				if (ipScope.getStart() <= ipValue && ipValue <= ipScope.getEnd()) {
					return true;
				}

			}
		}
		return false;
	}

	/**
	 * 是否相交匹配命中，指定的IPExp是否和此比较器里面的IP有相交的地址
	 * 
	 * @param ipExp
	 * @return
	 */
	public boolean intersectMatching(String ipExp) {
		IPScope ipScope = compileIpExp(ipExp);
		if (ipScope != null) {
			for (IPScope ipCmp : this.ipScopes) {
				if (ipScope.hasIntersectScope(ipCmp)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ipExp 是否 被包含在 此匹配器中
	 * 
	 * @param ipExp
	 * @return
	 */
	public boolean containMatching(String ipExp) {
		IPScope ipScope = compileIpExp(ipExp);
		if (ipScope != null) {
			for (IPScope ipCmp : this.ipScopes) {
				if (ipScope.isInScope(ipCmp)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否此比较器里面有相交的IP地址
	 * 
	 * @return
	 */
	public boolean hasIntersectIp() {
		for (IPScope ipScope : this.ipScopes) {
			for (IPScope ipCmp : this.ipScopes) {
				if (ipScope != ipCmp) {
					if (ipScope.hasIntersectScope(ipCmp)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获取命中的匹配器,如果没有命中则返回 empty
	 * 
	 * @param ip
	 * @return
	 */
	public List<IPScope> matchingScopes(String ip) {
		List<IPScope> matchedScopes = null;
		if (single_pattern.matcher(ip).matches()) {
			int ipValue = Ipv4Utils.ipToInt(ip);
			for (IPScope ipScope : this.ipScopes) {
				if (ipScope.getStart() <= ipValue && ipValue <= ipScope.getEnd()) {
					if (matchedScopes == null) {
						matchedScopes = new ArrayList<>();
					}
					matchedScopes.add(ipScope);
				}

			}
		}
		return matchedScopes == null ? Collections.emptyList() : matchedScopes;
	}

	/**
	 * 获取最小命中的范围,输入的表达式，若经过相交范围的处理，是不会存在这样的情况出现的，也都是命中一个
	 * 表达式原则上面不允许有相交的IP出现,所以处理者应在前面对输入的（保存的）表达式进行相交判断
	 * 
	 * @param ip
	 * @return
	 */
	public Optional<IPScope> matchingMinScope(String ip) {
		List<IPScope> matched = this.matchingScopes(ip);
		if (matched.isEmpty() == false) {
			if (matched.size() == 1) {
				return Optional.of(matched.get(0)) ;
			} else {
				IPScope minScope = matched.get(0);
				for (IPScope compare : matched) {
					if (compare.isInScope(minScope)) {
						minScope = compare;
					}
				}
				return Optional.of(minScope);
			}
		}
		return Optional.empty();
	}

	public List<IPScope> getIpScopes() {
		return ipScopes;
	}
	
	
	public List<String> getAllIpList(){
		if(ipScopes != null){
			List<String> list = new ArrayList<>();
			for(IPScope scope : ipScopes){
				list.addAll(scope.getIpList());
			}
			return list;
		}
		return Collections.emptyList();
	}

	public List<String> getExprs() {
		return exprs;
	}
	
	

}
