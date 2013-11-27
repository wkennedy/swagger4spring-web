package com.knappsack.swagger4springweb.testController;

import com.wordnik.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/empty")
@Api(value = "Test ApiExcludes", basePath = "/api/v1/empty", description = "An empty controller")
public class EmptyTestController {

}
