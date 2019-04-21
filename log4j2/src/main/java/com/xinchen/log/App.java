package com.xinchen.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 */
public class App {

    private static final Logger log = LogManager.getLogger("OK");

    public static void main(String[] args) {

        for (int i = 0; i < 1000; i++) {
            log.info("Hello World!");
            log.debug("Hello World!");
            log.error("Hello World!");
            log.trace("Hello World!");
        }

    }
}
