package com.hcxinan.sys.message;

import com.hcxinan.core.inte.message.IMailSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author liudk
 * @Description:
 * @date 21-9-8 下午6:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration({"classpath*:applicationContext.xml"})
@Transactional(transactionManager = "transactionManager")
@Rollback(true)
public class MailSenderTest {
    @Autowired
    private IMailSender mailSender;

//    @Test
    public void send() {
        mailSender.send(Arrays.asList("liudk@hcxinan.com"),"测试的邮件","测试的邮件内容");
    }
}