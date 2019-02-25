package com.xinchen.log.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 对log4j在进行一层封装
 * @author xinchen
 * @version 1.0
 * @date 23/02/2019 16:00
 */
public class CustomLogger {
    private final Logger log;

    /**
     *  log4j把传递进来的callerFQCN在堆栈中一一比较，相等后，再往上一层即认为是用户的调用类
     *  主要解决自定义Log信息输出后,方法名和行号打印不对的问题
     *  @see org.apache.log4j.spi.LocationInfo
     * */
    private final static String FQCN = CustomLogger.class.getName();

    public CustomLogger(String name) {
        this.log = Logger.getLogger(name);
    }

    public void info(Object o) {
        log.info(o);
    }

    /**
     * 自定义传入FQCN
     * @param o o
     */
    public void infoCustomer(Object o) {
        log.log(FQCN, Level.INFO, o, (Throwable) null);
    }
}
