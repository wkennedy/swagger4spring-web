package com.knappsack.swagger4springweb.testController.category;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/api/v1/blue")
@Api(value = "/api/v1/blue", description = "Test Category, blue") // this will be ignored due to @ApiCategory
@DarkCategory
public class BlueController {

  @ApiOperation("Blue is sky")
  @RequestMapping(value = "/sky", method = GET)
  public String sky() {
    return "sky";
  }
}
