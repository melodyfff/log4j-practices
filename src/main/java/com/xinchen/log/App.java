package com.xinchen.log;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * Hello world!
 */
public class App {

    private static final Logger logger = Logger.getLogger("com1");

    public static void main(String[] args) {
        MDC.put("Placeholder", "HELLO");
        logger.setLevel(Level.TRACE);
        for (int i =0;i<1000;i++){
            logger.info("ok");
            logger.debug("ok");
            logger.warn("ok");
            logger.error("ok");
            logger.fatal("ok");
            logger.trace("ok");
        }
    }
}
