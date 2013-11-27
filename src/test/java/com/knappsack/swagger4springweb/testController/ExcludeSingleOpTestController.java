package com.knappsack.swagger4springweb.testController;

import com.knappsack.swagger4springweb.AbstractTest;
import com.knappsack.swagger4springweb.annotation.ApiExclude;
import com.knappsack.swagger4springweb.testModels.MockPojo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/exclude2")
@Api(value = "Test ApiExcludes", basePath = "/api/v1/exclude2", description = "Some operations to exclude")
public class ExcludeSingleOpTestController {

    @ApiExclude
    @ApiOperation(value = AbstractTest.EXCLUDE_LABEL)
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> apiOperation1ToInclude() {
        return new ArrayList<MockPojo>();
    }
}
