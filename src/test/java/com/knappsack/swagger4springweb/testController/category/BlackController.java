package com.knappsack.swagger4springweb.testController.category;


import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wordnik.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/api/v1/black")
@DarkCategory
public class BlackController {

  @ApiOperation("This is for testing ordering")
  @RequestMapping(value = "/x-planet", method = GET)
  public String xPlanet() {
    return "x-planet";
  }

  @ApiOperation("Black is earth")
  @RequestMapping(value = "/earth", method = GET)
  public String earth() {
    return "earth";
  }

  @ApiOperation("Wildcard list")
  @RequestMapping(value = "/wildcard", method = GET)
  public List<? extends Number> genericType() {
    return asList(6L, 4L);
  }

}
