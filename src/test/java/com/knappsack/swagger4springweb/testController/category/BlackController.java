package com.knappsack.swagger4springweb.testController.category;


import com.knappsack.swagger4springweb.annotation.ApiCategory;
import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api/v1/black")
@Api(value = "Test Category, black")
@ApiCategory("dark")
public class BlackController {

  @ApiOperation("Black is earth")
  @RequestMapping(value = "earth", method = GET)
  public String earth() {
    return "earth";
  }
}
