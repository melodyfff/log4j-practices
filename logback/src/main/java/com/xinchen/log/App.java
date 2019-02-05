package com.xinchen.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        for (int i = 0; i <1000 ; i++) {
            log.trace("Hello World!");
            log.info("Hello World!");
            log.debug("Hello World!");
            log.warn("Hello World!");
            log.error("Hello World!");
        }
    }
}
