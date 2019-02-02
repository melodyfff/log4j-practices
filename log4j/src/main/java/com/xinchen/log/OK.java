package com.xinchen.log;

/**
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/2/2 1:00
 */
public class OK {
    public static void main(String[] args) {
        String s = "customer-server.20190202.info.001.log";
        System.out.println(s.replaceAll("\\.(([0-9]{1,3})(\\.log$))",".002.log"));
        System.out.println(s);
        System.out.println(Integer.parseInt("003"));
    }
}
