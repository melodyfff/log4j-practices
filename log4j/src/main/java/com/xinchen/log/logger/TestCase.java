package com.xinchen.log.logger;

/**
 * @author xinchen
 * @version 1.0
 * @date 23/02/2019 16:24
 */
public class TestCase {

    private final static CustomLogger log = new CustomLogger("test.costomer.logger");


    public static void main(String[] args) {
        log.info("hello");
        // [2019-02-23 16:32:02:010][com.xinchen.log.logger.CustomLogger][INFO][com.xinchen.log.logger.CustomLogger.info(CustomLogger.java:21):21] - hello


        log.infoCustomer("hello");
        // [2019-02-23 16:32:02:025][com.xinchen.log.logger.TestCase][INFO][com.xinchen.log.logger.TestCase.main(TestCase.java:18):18] - hello
    }


}
