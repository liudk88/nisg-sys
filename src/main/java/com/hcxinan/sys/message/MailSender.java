package com.hcxinan.sys.message;

import com.hcxinan.core.inte.message.IMailSender;
import com.hcxinan.sys.cache.CacheManager;
import com.hcxinan.sys.cache.inte.ICacheCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;

/**
 * @author liudk
 * @Description:
 * @date 21-9-8 下午5:43
 */
@Component("hcMailSender")
public class MailSender implements IMailSender {
    private static final Logger log = Logger.getLogger(MailSender.class);
    // 邮件父键
    private static final String MAIL = "MAIL";
    // 邮件服务器
    private static final String HOST = "HOST";
    // 默认用户名
    private static final String USERNAME = "USERNAME";
    // 默认密码
    private static final String PASSWORD = "PASSWORD";
    // 发件人邮箱.
    private static final String FROM = "FROM";
    // 发件人名称.
    private static final String FROMNAME = "FROMNAME";
    // 设置认证开关
    private static final String AUTH = "AUTH";
    // 设置调试开关
    private static final String DEBUG = "DEBUG";
    // 编码格式
    private static final String DEFAULTENCODING = "gb2312";

    private String from;

    private String fromName;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    private JavaMailSender javaMailSender;

    public void setJavaMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public boolean send(final List<String> toList, final String subject, final String text) {
        return send(toList, null, null, subject, text, null);
    }

    public boolean send(final List<String> toList, final List<String> ccList, final List<String> bccList,
                        final String subject, final String text) {
        return send(toList, ccList, bccList, subject, text, null);
    }


    public boolean send(final List<String> toList, final List<String> ccList, final List<String> bccList,
                        final String subject, final String text, final List<File> attachmentFileList) {
		/*if (javaMailSender != null) {
			// 初始化邮件服务

		}*/
        initMailSender();
        boolean ret = true;
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            mimeMessage.setSubject(subject);
        } catch (MessagingException e) {
            System.err.println("设置邮件主题发生错误！");
            e.printStackTrace();
        }
        try {
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(from));//给自己抄送一份

            if (toList != null && toList.size() > 0) {
                for (String to : toList) {
                    InternetAddress toStr = new InternetAddress(to);
                    mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toStr.toString()));
                }
            } else {
                return false;
            }
            BodyPart bp = new MimeBodyPart();
            bp.setContent("" + text, "text/html;charset=GBK");
            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(bp);
            mimeMessage.setContent(mp);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            ret = false;
            //由于邮箱有可能设置不对导致发送不了信息，而调度是非常的频繁，会打印大量日志，所有这里不输出详细信息了
            log.error("设置邮件发送方发生错误！");
//			log.error("设置邮件发送方发生错误！"+e.getMessage());
			/*System.err.println();
			e.printStackTrace();*/
        }

        return ret;
    }

    private void initMailSender() {
        javaMailSender = new JavaMailSenderImpl();
        List<?> list = CacheManager.getInstance().getCodes(MAIL);
        if (list == null) {
            throw new NullPointerException("邮件参数未设置");
        } else {
            ICacheCode code = CacheManager.getInstance().getCode(MAIL, HOST);

            // 邮件服务器
            if (code == null || StringUtils.isEmpty(code.getParams1())) {
                throw new RuntimeException("邮件服务器设置不正确");
            } else {
                ((JavaMailSenderImpl) javaMailSender).setHost(code.getParams1());
            }

            code = CacheManager.getInstance().getCode(MAIL, USERNAME);
            // 默认用户名
            if (code == null || StringUtils.isEmpty(code.getParams1())) {
                throw new RuntimeException("邮件服务器默认用户名设置不正确");
            } else {
                ((JavaMailSenderImpl) javaMailSender).setUsername(code.getParams1());
            }

            code = CacheManager.getInstance().getCode(MAIL, PASSWORD);
            // 默认密码
            if (code == null || StringUtils.isEmpty(code.getParams1())) {
                throw new RuntimeException("邮件服务器默认密码设置不正确");
            } else {
                ((JavaMailSenderImpl) javaMailSender).setPassword(code.getParams1());
            }

            code = CacheManager.getInstance().getCode(MAIL, AUTH);
            // 设置认证开关
            boolean auth = true;
            if (code != null && StringUtils.isEmpty(code.getParams1())) {
                auth = Boolean.parseBoolean(code.getParams1());
            }
            ((JavaMailSenderImpl) javaMailSender).getJavaMailProperties().put("mail.smtp.auth", auth);

            code = CacheManager.getInstance().getCode(MAIL, DEBUG);
            // 设置调试开关
            boolean debug = true;
            if (code != null && StringUtils.isEmpty(code.getParams1())) {
                debug = Boolean.parseBoolean(code.getParams1());
            }
            ((JavaMailSenderImpl) javaMailSender).getJavaMailProperties().put("mail.debug", debug);
            // 设置编码格式
            ((JavaMailSenderImpl) javaMailSender).setDefaultEncoding(DEFAULTENCODING);
            // 读取发件人邮箱及姓名
            code = CacheManager.getInstance().getCode(MAIL, FROM);
            setFrom(code.getParams1());
            code = CacheManager.getInstance().getCode(MAIL, FROMNAME);
            setFromName(code.getParams1());
        }

    }
}
