package com.xinchen.log;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Xin Chen (xinchenmelody@gmail.com)
 * @version 1.0
 * @date Created In 2019/2/1 23:50
 */
@Controller
public class HelloController {

    @RequestMapping("/test")
    @ResponseBody
    public Object test() {
        return "Hello World!";
    }
}
