package com.knappsack.swagger4springweb.testController;


import com.knappsack.swagger4springweb.testModels.TestPojo;
import com.wordnik.swagger.annotations.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/test")
@Api(value = "Test operations", listingClass = "TestController", basePath = "/api/v1/test", description = "Operations for all tests")
public class TestController {

    @ApiOperation(value = "Find all test pojos", notes = "Get all test pojos for this test.", httpMethod = "GET", responseClass = "TestPojo", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<TestPojo> getTestPojos(HttpServletRequest request, @ApiParam(name = "testVariable", value = "String") @PathVariable String testVariable) {
        List<TestPojo> testPojos = new ArrayList<TestPojo>();
        TestPojo testPojo = new TestPojo();
        testPojo.setId(1);
        testPojo.setName("Test Pojo");
        testPojo.setDescription("This is a test pojo for testing purposes.");
        testPojos.add(testPojo);

        return testPojos;
    }

    @RequestMapping(value = "/testPojosNoSwaggerAnnotations", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<TestPojo> getTestPojosNoSwaggerAnnotations(HttpServletRequest request, @PathVariable String testVariable) {
        List<TestPojo> testPojos = new ArrayList<TestPojo>();
        TestPojo testPojo = new TestPojo();
        testPojo.setId(1);
        testPojo.setName("Test Pojo");
        testPojo.setDescription("This is a test pojo for testing purposes.");
        testPojos.add(testPojo);

        return testPojos;
    }

}
