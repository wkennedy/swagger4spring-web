package com.knappsack.swagger4springweb.testController.category;

import com.knappsack.swagger4springweb.annotation.ApiCategory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api/v1/blue")
@Api(value = "Test Category, blue")
@ApiCategory("dark")
public class BlueController {

  @ApiOperation("Blue is sky")
  @RequestMapping(value = "/sky", method = GET)
  public String sky() {
    return "sky";
  }
}
