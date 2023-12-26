package com.zhangzhankui.samples.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhangzhankui.samples.common.core.web.ResponseMessage;

@RestController
@RequestMapping("/index")
public class IndexController {
    private final Log logger = LogFactory.getLog(IndexController.class);

    @RequestMapping("/hello")
    public Object hello() {
        return ResponseMessage.ok("hello world");
    }

}
