package com.saas.adapter.entity;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {


    @GetMapping("/test")
    public String s(){
      log.info("CESHIASFDQWEFTIWETJHFGWOIGEJWOI");
        return "CESHIASFDQWEFTIWETJHFGWOIGEJWOI";
    }

}
