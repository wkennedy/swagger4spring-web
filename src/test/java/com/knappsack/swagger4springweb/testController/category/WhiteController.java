package com.knappsack.swagger4springweb.testController.category;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/api/v1/white")
@Api(value = "/api/v1/white",description = "Test Category, white")
@LightCategory
public class WhiteController {

  @ApiOperation("White is fortune")
  @RequestMapping(value = "/fortune", method = GET)
  public String fortune() {
    return "fortune";
  }
}
