package com.xinchen.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 */
public class App {

    private static final Logger log = LogManager.getLogger("ok");

    public static void main(String[] args) {
        log.info("Hello World!");
    }
}
