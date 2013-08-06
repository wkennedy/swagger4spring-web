package com.knappsack.swagger4springweb.testController;

import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.testModels.MockPojo;
import com.wordnik.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/exclude3")
@ApiExclude
@Api(value = "Test ApiExcludes", listingClass = "ExcludeClassTestController", basePath = "/api/v1/exclude3", description = "controller to exclude")
public class ExcludeClassTestController {

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> apiOperation1ToInclude() {
        return new ArrayList<MockPojo>();
    }
}
