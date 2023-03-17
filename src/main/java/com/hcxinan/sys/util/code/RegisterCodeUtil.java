package com.hcxinan.sys.util.code;

import com.hcxinan.sys.inte.ICodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * 离线主机管理,这里会有使用的问题，如果客户把主机的时间调整后，其实这个有效期没有意义。
 * <p>
 * 1： 注册
 *
 * @see #registerByCmd(String)
 * <p>
 * 2: 是否为有效的注册客户端
 * @see #isValidRegisterClient()
 * <p>
 * 3: 查询是否已经注册
 * @see #getSavedTokens()
 * <p>
 * 4: 生成加密令牌
 * @see #generateRegisterToken(long, boolean, String...)
 */
public class RegisterCodeUtil implements ICodeUtil {

    private static final Logger log = LoggerFactory.getLogger(RegisterCodeUtil.class);

    /**
     * 解释后的命令字符串
     * 目前知道已知的格式
     * token[0]: 最晚有效时间，长整形，原始的
     * token[1]: 如果为true 时 注册时使用 多 MAC 地址信息
     **/
    private String[] tokenArr;


    private String encryptKey = "UPATHS_KEY";

    /**
     * 系统使用 AES 算法 对 字符串进行加密
     */
    public static final String ALGORITHM = "AES";

    private  LongEncrypt encrypt = new LongEncrypt();

    public RegisterCodeUtil() {

    }

    /**
     * @param encryptKey 加密的密码,如果有需求，可以设置此密码来自数据库或配置
     */
    public RegisterCodeUtil(String encryptKey) {
        this.encryptKey = encryptKey;
    }


    /**
     * 是否为有效的令牌TOKEN
     * @param tokenArr 加密的令牌，使用 AES 加密，多个以[,;]分开，数组命令行格式，
     * @return
     * @see System#currentTimeMillis
     */
    public void setArgs(String[] tokenArr) {
        this.tokenArr = tokenArr;
    }

    /**
     * 直接使用加密的令牌初始化参数
     * @param encryptKey 加密的令牌
     */
    public void setArgs(String encryptKey) throws Exception {
          long value =   encrypt.decrypt(encryptKey);
            if(value > 0){
                this.setArgs(new String[]{value+""});
            }else{
                throw  new IllegalArgumentException("解密失败");
            }
    }


    /**
     * 一键运行注册，默认的注册行为
     * @param encryptKey 加密后的注册令牌
     * @return 是否注册成功
     * @throws Exception 如果令牌格式不对，或者是当前主机网络有问题，会出现异常
     */
    public boolean registerByCmd(String encryptKey) {
        if (encryptKey != null) {
            try {
                setArgs(encryptKey);
            } catch (Exception e) {
                return  false;
            }
            if (!isTokenOverdue()) {
                saveClientInfo();
                return true;
            }
        }
        return false;
    }


    /**
     * 生成注册令牌,原来的令牌太长，使用新的加密方式
     * @see  #generateRegisterToken(Calendar) 
     * @param overdue  过期的时间
     * @param multiMac 是把主机的所有mac信息写入到注册表中,否时，只使用 localMac
     * @param others   其它的字符串，如果有需要的话(任意字符)
     * @return
     */
    @Deprecated
    public String generateRegisterToken(long overdue, boolean multiMac, String... others) throws Exception {
        long overTime = overdue + System.currentTimeMillis();
        return encrypt.encrypt(overTime);
    }


    /**
     * 从当前开始，增加多少天
     * @param afterDays
     * @return
     */
    public String generateRegisterToken(int afterDays) {
        long overTime = 1000 * 60 * 60 * 24 * afterDays + System.currentTimeMillis();
        return encrypt.encrypt(overTime);
    }

    /**
     * 指定过期的时间
     * @param overdueDate
     * @return
     */
    public String generateRegisterToken(Calendar overdueDate) {
        long overTime = overdueDate.getTimeInMillis();
        return encrypt.encrypt(overTime);
    }

    /**
     * 注册信息是否已过期
     * @return
     */
    public boolean isTokenOverdue() {
        if (this.tokenArr != null) {
            String timeToken = this.tokenArr[0];
            try {
                long overDue = Long.parseLong(timeToken);
                return overDue <= System.currentTimeMillis();
            } catch (Exception e) {
                //不做任何处理
            }
        }
        return true;
    }

    /**
     * 是否为已经注册的客户端,
     * 建议是一次调用后，直接把结果缓存到调用者上面
     * @return 当前主机是否有效的注册，注册的mac 地址与当前运行的地址一致
     */
    public boolean isValidRegisterClient() {
        try {
            Preferences preferences = Preferences.userRoot();
            if (preferences.nodeExists("hcxa")) {
                preferences = preferences.node("hcxa");
                String tokenStr = preferences.get("mac", null);
                if (tokenStr != null) {
                    String bindMac = null;
                    try {
                        bindMac = this.decrypt(tokenStr);
                    } catch (Exception e) {
                       return  false;
                    }
                    String[] macArr = bindMac.split("[,;]");
                    String localMac = getLocalMac();
                    for (String mac : macArr) {
                        if (mac.equals(localMac)) {
                            return true;
                        }
                    }
                }
            }else{
                return  false;
            }
        } catch (BackingStoreException e) {
            //
            log.error("用户不支持存储",e);
            return true;
        }
        return false;
    }


    /**
     * 保存当前的令牌到主机
     * @throws Exception 如果网络不正常，没有网卡，或者是没有配置 localhost信息时，，可能会出异常
     */
    public void saveClientInfo(){
        if (tokenArr != null) {
            String saveMacInfo = null;
            if (isSaveMacList()) {
                saveMacInfo = String.join(";", getMacList());
            } else {
                saveMacInfo = getLocalMac();
            }
            String encryptMac = null;
            try {
                encryptMac = encrypt(saveMacInfo);
            } catch (Exception e) {
                log.error("加密失败",e);
            }
            String saveText = String.join(";", tokenArr);
            try{
                Preferences preferences = Preferences.userRoot();
                preferences = preferences.node("hcxa");
                preferences.put("offline", saveText);
                preferences.put("mac", encryptMac);
            }catch (Exception e){
                log.error("保存失败",e);
            }

        }
    }

    /**
     * 是否保存多MAC 地址
     * @return
     */
    private boolean isSaveMacList() {
        if (this.tokenArr != null && this.tokenArr.length > 1) {
            return "true".equalsIgnoreCase(tokenArr[1]);
        }
        return false;
    }

    /**
     * 清除注册信息
     */
    public void clearTokens() {
        try {
            Preferences root = Preferences.userRoot();
            root.node("hcxa").removeNode();
        } catch (BackingStoreException e) {
            log.error("清除离线端注册信息异常", e);
        }
    }

    /**
     * 获取保存的令牌,如果没有保存值 返回  NULL
     * @return
     */
    public String[] getSavedTokens() {
        try {
            Preferences preferences = Preferences.userRoot();
            if (preferences.nodeExists("hcxa")) {
                preferences = preferences.node("hcxa");
                String tokenStr = preferences.get("offline", null);
                if (tokenStr != null) {
                    return tokenStr.split("[;]");
                }
            }
        } catch (BackingStoreException e) {
            //
        }
        return null;
    }


    /**
     * 返回系统默认的私钥
     * @return
     * @throws NoSuchAlgorithmException
     */
    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        keyGen.init(128, random);
        SecretKey key = keyGen.generateKey();
        return key;
    }

    /**
     * 对传入的字符串使用系统默认的算法进行加密
     * @param source
     * @return
     * @throws Exception
     */
    String encrypt(String source) throws Exception {
        byte[] data = source.getBytes(StandardCharsets.UTF_8);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey());
        data = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 对传入的字符串使用系统的解密方法来解密，解密字符串必须是以前加过密的字符
     * @param source
     * @return
     * @throws Exception
     */
    String decrypt(String source) throws Exception {
        byte[] data = Base64.getDecoder().decode(source.getBytes());
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, generateSecretKey());
        data = cipher.doFinal(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    public String[] getTokenArr() {
        return tokenArr;
    }

    /******************************************
     *
     * 以下的方法，是因为使用者调用方便，暂时不抽取
     *
     * *****************************************************
     */

    /**
     * @return 返回当前主机的网卡的 localhost MAC 地址
     * @throws SocketException
     */
    String getLocalMac(){
        try{
            NetworkInterface interFace = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            List<InterfaceAddress> addr = interFace.getInterfaceAddresses();
            StringBuilder sb = new StringBuilder();
            byte[] mac = interFace.getHardwareAddress();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        }catch (Exception e){
            log.error("mac获取失败",e);
            return  error_mac;
        }
    }


    /**
     * 考虑到网络的变更因素低，使用缓存
     */
    static List<String> cacheMac = null;

    private synchronized List<String> getCachedMacList() {
        if (cacheMac == null) {
            try {
                cacheMac = getMacList();
            } catch (Exception e) {
                //
                cacheMac = Collections.emptyList();
            }
        }
        return cacheMac;
    }


    /***
     * 因为一台机器不一定只有一个网卡，所以返回的是mac地址集合
     *注意这个方法会花5秒左右的时间，因为网络查找的因素
     * ***/
    List<String> getMacList()  {
        try {
            return getPrefMacList();
        } catch (Exception e) {
            log.error("mac获取失败",e);
        }
        return Collections.singletonList(error_mac);
    }

    private  static String  error_mac = "00-00-00-00";


        private  List<String> getPrefMacList()throws Exception {
            java.util.Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            StringBuilder sb = new StringBuilder();
            ArrayList<String> tmpMacList = new ArrayList<>();
            while (en.hasMoreElements()) {
                NetworkInterface iface = en.nextElement();
                List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
                for (InterfaceAddress addr : addrs) {
                    InetAddress ip = addr.getAddress();
                    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                    if (network == null) {
                        continue;
                    }
                    byte[] mac = network.getHardwareAddress();
                    if (mac == null) {
                        continue;
                    }
                    sb.delete(0, sb.length());
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    tmpMacList.add(sb.toString());
                }
            }
            if (tmpMacList.size() <= 0) {
                return tmpMacList;
            }
            /***去重，同一个网卡的ipv4,ipv6得到的mac都是一样的，可能有重复***/
            List<String> unique = tmpMacList.stream().distinct().collect(Collectors.toList());
            return unique;
        }

    @Override
    public boolean verify(String code, Object obj) {
        if (StringUtils.isNotBlank(code)) {
            if (obj != null) {
                this.encryptKey = obj.toString().trim();
            }
            try {
                return registerByCmd(code);
            } catch (Exception e) {
                log.error(String.format("授权码解密异常，code：%s", code), e);
                //如果发生异常，直接返回 通过
                return  true;
            }
        }
        return false;
    }

    @Override
    public String getCode(Object obj) {
        return null;
    }
}
