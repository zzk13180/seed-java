package com.zhangzhankui.samples.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhangzhankui.samples.common.core.controller.ResponseMessage;

@RestController
@RequestMapping("/index")
public class IndexController {
  private final Log logger = LogFactory.getLog(IndexController.class);

  @GetMapping("/hello")
  public ResponseMessage<String> hello() {
    logger.info("hello world");
    return ResponseMessage.ok("hello world");
  }

}
