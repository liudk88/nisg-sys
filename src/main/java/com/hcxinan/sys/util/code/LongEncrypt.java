package com.hcxinan.sys.util.code;

public class LongEncrypt {

   private int radix = Character.MAX_RADIX;
    private   int firstFix = 14;
    private  int lastFix  = 11;

    LongEncrypt(){

    }

    /**
     * 必须是正 数 时间
     * @param value
     * @return
     */
      String encrypt(long value){
        String testValue = value+"";
        int length = testValue.length();
        //取正负第一位加权
        int first0 = Integer.parseInt(testValue.substring(0,1));
        int last0 =  Integer.parseInt(testValue.substring(length-1));
        //绝对小于36 数字
        int token0 = first0 + last0 + firstFix;

        //第正负第二位加权
        int first1 = Integer.parseInt(testValue.substring(1,2));
        int last1 =  Integer.parseInt(testValue.substring(length-2,length-1));
        int token1 = first1 + last1 + lastFix;
        String t0 = Integer.toString(token0,radix);
        String t1 = Integer.toString(token1,radix);
        String str = Long.toString(value,radix);
        return  String.format("%s%s%s",t0,str,t1).toUpperCase();
    }


     long decrypt(String decryptText){
        if(decryptText != null ){
            String  decryptValue  = decryptText.toUpperCase().trim();
            if(decryptValue.length() > 6){
                String first = decryptValue.substring(0,1);
                String last = decryptValue.substring(decryptValue.length()-1);
                int s0 = Integer.parseInt(first,radix);
                int s1 = Integer.parseInt(last,radix);
                long value = -1;
                try{
                    value = Long.parseLong(decryptValue.substring(1,decryptValue.length()-1),radix);
                }catch (Exception e){
                    return  -1;
                }

                String testValue = value+"";
                int length = testValue.length();
                //取正负第一位加权
                int first0 = Integer.parseInt(testValue.substring(0,1));
                int last0 =  Integer.parseInt(testValue.substring(length-1));
                //绝对小于36 数字
                int token0 = first0 + last0 + firstFix ;

                //第正负第二位加权
                int first1 = Integer.parseInt(testValue.substring(1,2));
                int last1 =  Integer.parseInt(testValue.substring(length-2,length-1));
                int token1 = first1 + last1 + lastFix;
                if(s0 == token0 && s1 == token1){
                    return  value;
                }
            }
        }
        return  -1L;
    }

}
