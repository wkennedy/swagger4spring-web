package com.knappsack.swagger4springweb.testController.category;


import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/api/v1/nocolor")
@Api(value = "/api/v1/nocolor", description = "Test Category, this controller does not have ApiCategory annotation, thus will be mapped as earlier")
public class NoColorController {

  @RequestMapping(value = "/invisible", method = GET)
  @ApiOperation("I am invisible")
  public String invisible() {
    return "invisible";
  }
}
