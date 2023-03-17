package com.hcxinan.sys.util.ip;

import java.net.InetAddress;

/**
 * 子网掩码的计算
 * 
 * @author huangbin
 *
 */
@Deprecated //请使用core中对应的类，后面废除
public class Ipv4Utils {

	/**
	 * 根据掩码位数计算掩码
	 * 
	 * @param maskIndex
	 *            掩码位
	 * @return 子网掩码
	 */
	public static String getNetMask(int inetMask) {
		StringBuilder mask = new StringBuilder();
		if (inetMask > 32 || inetMask <= 0) {
			return null;
		}
		// 子网掩码为1占了几个字节
		int num1 = inetMask / 8;
		// 子网掩码的补位位数
		int num2 = inetMask % 8;
		int array[] = new int[4];
		for (int i = 0; i < num1; i++) {
			array[i] = 255;
		}
		for (int i = num1; i < 4; i++) {
			array[i] = 0;
		}
		for (int i = 0; i < num2; num2--) {
			array[num1] += 1 << 8 - num2;
		}
		for (int i = 0; i < 4; i++) {
			if (i == 3) {
				mask.append(array[i]);
			} else {
				mask.append(array[i] + ".");
			}
		}
		return mask.toString();
	}

	/**
	 * 把IP地址转化为字节数组
	 * 
	 * @param ipAddr
	 * @return byte[]
	 */
	public static byte[] ipToBytesByInet(String ipAddr) {
		try {
			return InetAddress.getByName(ipAddr).getAddress();
		} catch (Exception e) {
			throw new IllegalArgumentException(ipAddr + " is invalid IP");
		}
	}

	/**
	 * 把IP地址转化为int
	 * 
	 * @param ipAddr
	 * @return int
	 */
	public static byte[] ipToBytesByReg(String ipAddr) {
		byte[] ret = new byte[4];
		try {
			String[] ipArr = ipAddr.split("\\.");
			ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
			ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
			ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
			ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
			return ret;
		} catch (Exception e) {
			throw new IllegalArgumentException(ipAddr + " is invalid IP");
		}
	}

	/**
	 * 字节数组转化为IP
	 * 
	 * @param bytes
	 * @return int
	 */
	public static String bytesToIp(byte[] bytes) {
		return new StringBuffer().append(bytes[0] & 0xFF).append('.').append(bytes[1] & 0xFF).append('.')
				.append(bytes[2] & 0xFF).append('.').append(bytes[3] & 0xFF).toString();
	}

	/**
	 * 根据位运算把 byte[] -> int
	 * 
	 * @param bytes
	 * @return int
	 */
	public static int bytesToInt(byte[] bytes) {
		int addr = bytes[3] & 0xFF;
		addr |= ((bytes[2] << 8) & 0xFF00);
		addr |= ((bytes[1] << 16) & 0xFF0000);
		addr |= ((bytes[0] << 24) & 0xFF000000);
		return addr;
	}

	/**
	 * 把IP地址转化为int
	 * 
	 * @param ipAddr
	 * @return int
	 */
	public static int ipToInt(String ipAddr) {
		try {
			return bytesToInt(ipToBytesByInet(ipAddr));
		} catch (Exception e) {
			throw new IllegalArgumentException(ipAddr + " is invalid IP");
		}
	}

	public static int[] getIPIntScope(String ipAndMask) {
		String[] ipArr = ipAndMask.split("/");
		if (ipArr.length != 2) {
			throw new IllegalArgumentException("invalid ipAndMask with: " + ipAndMask);
		}
		int netMask = Integer.valueOf(ipArr[1].trim());
		if (netMask < 0 || netMask > 31) {
			throw new IllegalArgumentException("invalid ipAndMask with: " + ipAndMask);
		}
		int ipInt = ipToInt(ipArr[0]);
		int netIP = ipInt & (0xFFFFFFFF << (32 - netMask));
		int hostScope = (0xFFFFFFFF >>> netMask);
		return new int[] { netIP, netIP + hostScope };
	}

	/**
	 * 把int->ip地址
	 * 
	 * @param ipInt
	 * @return String
	 */
	public static String intToIp(int ipInt) {
		return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.').append((ipInt >> 16) & 0xff).append('.')
				.append((ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff)).toString();
	}

	/**
	 * 把192.168.1.1/24 转化为IP数组范围
	 * 
	 * @param ipAndMask
	 * @return String[]
	 */
	public static String[] getIPAddrScope(String ipAndMask) {
		int[] ipIntArr = getIPIntScope(ipAndMask);
		return new String[] { intToIp(ipIntArr[0]), intToIp(ipIntArr[1]) };
	}

	/**
	 * 根据IP 子网掩码（192.168.1.1 255.255.255.0）转化为IP段
	 * 
	 * @param ipAddr
	 *            ipAddr
	 * @param mask
	 *            mask
	 * @return int[]
	 */
	public static int[] getIPIntScope(String ipAddr, String mask) {
		int ipInt;
		int netMaskInt = 0, ipcount = 0;
		try {
			ipInt = ipToInt(ipAddr);
			if (null == mask || "".equals(mask)) {
				return new int[] { ipInt, ipInt };
			}
			netMaskInt = ipToInt(mask);
			ipcount = ipToInt("255.255.255.255") - netMaskInt;
			int netIP = ipInt & netMaskInt;
			int hostScope = netIP + ipcount;
			return new int[] { netIP, hostScope };
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid ip scope express  ip:" + ipAddr + "  mask:" + mask);
		}
	}

	/**
	 * 根据IP 子网掩码（192.168.1.1 255.255.255.0）转化为IP段
	 * 
	 * @param ipAddr
	 *            ipAddr
	 * @param mask
	 *            mask
	 * @return String[]
	 */
	public static String[] getIPStrScope(String ipAddr, String mask) {
		int[] ipIntArr = getIPIntScope(ipAddr, mask);
		return new String[] { intToIp(ipIntArr[0]), intToIp(ipIntArr[0]) };
	}

}
