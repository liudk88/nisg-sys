package com.hcxinan.sys.util.code;

import org.junit.Test;

import java.util.Base64;
import java.util.Calendar;

import static org.junit.Assert.*;

public class RegisterCodeUtilTest {

    @Test
    public void generateRegisterToken() throws Exception {
        RegisterCodeUtil registerCode = new RegisterCodeUtil();
        registerCode.clearTokens();
        String code = registerCode.generateRegisterToken(1000 * 60 * 60 * 24 * 10, false);
        boolean value = registerCode.isValidRegisterClient();
        assertFalse(value);
        value = registerCode.registerByCmd(code);
        assertTrue(value);
        registerCode.clearTokens();
        code = registerCode.generateRegisterToken(-1, false);
        value = registerCode.registerByCmd(code);
        assertFalse(value);
    }


    @Test
    public  void  testOverdue(){
        RegisterCodeUtil registerCode = new RegisterCodeUtil();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,1);
        String code = registerCode.generateRegisterToken(calendar);
        registerCode.clearTokens();;
        boolean value =  registerCode.registerByCmd(code);
        assertTrue(value);
        assertTrue(registerCode.isValidRegisterClient());
        String[] tokens = registerCode.getSavedTokens();
        assert  tokens != null;
        long time = Long.parseLong(tokens[0]);
        assertEquals(time,calendar.getTimeInMillis());
    }


    //OL4JVDN8FK
    @Test
    public void testRegister(){
        RegisterCodeUtil registerCode = new RegisterCodeUtil();
        registerCode.clearTokens();
        boolean value = registerCode.registerByCmd("8OL4JVDN8FK");
        assertFalse(value);
        registerCode.clearTokens();
        value = registerCode.registerByCmd("OL4KVDN8FK");
        assertFalse(value);
        registerCode.clearTokens();
        value = registerCode.registerByCmd("OL4JVDN8FK");
        assertTrue(value);
        value = registerCode.registerByCmd("OL4JVDN9FK");
        assertFalse(value);
        registerCode.clearTokens();
        value = registerCode.registerByCmd("OL4JVXN8FK");
        assertFalse(value);
        registerCode.clearTokens();
        value = registerCode.registerByCmd("OL4000999K");
        assertFalse(value);
    }
}