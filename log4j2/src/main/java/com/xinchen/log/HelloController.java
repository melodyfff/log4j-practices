package com.xinchen.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/2/5 23:13
 */
@Controller
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger("helloWorld");

    @RequestMapping("/test")
    @ResponseBody
    public String test(){

        for (int i =0;i<10;i++){
            log.info("Hello World!");
            log.debug("Hello World!");
            log.error("Hello World!");
            log.trace("Hello World!");
        }
        return "Hello World!";
    }
}
