package com.knappsack.swagger4springweb.testController.category;


import com.knappsack.swagger4springweb.annotation.ApiCategory;
import com.wordnik.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api/v1/nocolor")
@Api(value = "Test Category, this controller does not have ApiCategory annotation, thus will be mapped as earlier")
public class NoColorController {

  @RequestMapping(value = "/invisible", method = GET)
  public String invisible() {
    return "invisible";
  }
}
