package com.catfish.controller;

import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by A on 2017/3/30.
 */
@Controller
public class TestController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/test")
    @ResponseBody
    public String test(@RequestParam(defaultValue = "0") Integer time) {
        if (time>0){
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "1111";
    }

    @RequestMapping("/testCookie")
    @ResponseBody
    public String testCookie(@CookieValue String name) {
        return name;
    }

    @RequestMapping("/test2")
    @ResponseBody
    public String test2(String name) {
        logger.debug(name);
        return "222";
    }

    @RequestMapping("/test3")
    @ResponseBody
    public String test3(String name) {
        logger.debug(name);
        return "333";
    }

    @RequestMapping("/testjson")
    @ResponseBody
    public String testjson(HttpServletRequest request) {
        JSONObject jsonObject = null;
        try {
            String json = IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
            logger.debug(json);
            jsonObject = JSONObject.fromObject(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject.getString("name");
    }


}
