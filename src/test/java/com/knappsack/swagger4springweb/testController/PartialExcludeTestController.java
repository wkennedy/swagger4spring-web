package com.knappsack.swagger4springweb.testController;

import com.knappsack.swagger4springweb.AbstractTest;
import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.testModels.TestPojo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/partialExclude")
@Api(value = "Test ApiExcludes", listingClass = "PartialExcludeTestController", basePath = "/api/v1/partialExclude", description = "Some operations to exclude")
public class PartialExcludeTestController {

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<TestPojo> apiOperation1ToInclude() {
        return new ArrayList<TestPojo>();
    }

    @ApiOperation(value = AbstractTest.EXCLUDE_LABEL)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiExclude
    public
    @ResponseBody
    List<TestPojo> apiOperation2ToExclude() {
        return new ArrayList<TestPojo>();
    }

    @RequestMapping(value = "/resource1", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    TestPojo apiOperation3ToInclude() {
        return new TestPojo();
    }

    @ApiOperation(value = AbstractTest.EXCLUDE_LABEL)
    @RequestMapping(value = "/resource2", method = RequestMethod.GET, produces = "application/json")
    @ApiExclude
    public
    @ResponseBody
    TestPojo apiOperation4ToExclude() {
        return new TestPojo();
    }
}
