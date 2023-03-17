package com.hcxinan.sys.util.code;

import com.baomidou.mybatisplus.extension.api.R;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class LongEncryptTest {

    @Test
    public void encrypt() {
        LongEncrypt encrypt = new LongEncrypt();
        long l = System.currentTimeMillis();
        String d = encrypt.encrypt(l);
        System.out.println(l+"  "+d);
        System.out.println(encrypt.decrypt(d));
    }

    @Test
    public void encrypt2() {
        LongEncrypt encrypt = new LongEncrypt();
        long l = System.currentTimeMillis();

        Random random = new Random();
        for(int i = 0 ; i < 100 ; i++){
            long l2 =    Math.abs(random.nextInt()) * 1000 * 60 * 60 * 24+l;
            String d = encrypt.encrypt(l2);
            System.out.println(l2+"  "+d);
            Long dv =  encrypt.decrypt(d);
            assertNotNull(dv);
            assertEquals(dv,new Long(l2));

        }
    }
}