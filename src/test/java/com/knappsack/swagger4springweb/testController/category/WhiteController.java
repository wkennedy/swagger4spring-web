package com.knappsack.swagger4springweb.testController.category;

import com.knappsack.swagger4springweb.annotation.ApiCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api/v1/white")
@Api(value = "Test Category, white")
@ApiCategory("light")
public class WhiteController {

  @ApiOperation("White is fortune")
  @RequestMapping(value = "fortune", method = GET)
  public String fortune() {
    return "fortune";
  }
}
