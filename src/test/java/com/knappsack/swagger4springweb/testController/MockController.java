package com.knappsack.swagger4springweb.testController;


import com.knappsack.swagger4springweb.testModels.MockPojo;
import com.wordnik.swagger.annotations.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/api/v1/test")
@Api(value = "Test operations", listingClass = "TestController", basePath = "/api/v1/test", description = "Operations for all tests")
public class MockController {

    @ApiOperation(value = "Find all test pojos", notes = "Get all test pojos for this test.", httpMethod = "GET", responseClass = "MockPojo", multiValueResponse = true)
    @ApiError(code = 500, reason = "Process error")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> getTestPojos(HttpServletRequest request,
                                @ApiParam(name = "testVariable", value = "String")
                                @PathVariable String testVariable) {
        List<MockPojo> mockPojos = new ArrayList<MockPojo>();
        MockPojo mockPojo = new MockPojo();
        mockPojo.setId(1);
        mockPojo.setName("Test Pojo");
        mockPojo.setDescription("This is a test pojo for testing purposes.");
        mockPojos.add(mockPojo);

        return mockPojos;
    }

    @RequestMapping(value = "/testPojosNoSwaggerAnnotations", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> getTestPojosNoSwaggerAnnotations(HttpServletRequest request,
                                                    @PathVariable String testVariable) {
        List<MockPojo> mockPojos = new ArrayList<MockPojo>();
        MockPojo mockPojo = new MockPojo();
        mockPojo.setId(1);
        mockPojo.setName("Test Pojo");
        mockPojo.setDescription("This is a test pojo for testing purposes.");
        mockPojos.add(mockPojo);

        return mockPojos;
    }

    @RequestMapping(value = "/testPojoRequestParams", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> getTestPojoRequestParams(@RequestParam String testVariable,
                                            @RequestParam Boolean booleanVariable,
                                            @RequestParam Byte byteVariable,
                                            @RequestParam Long longVariable,
                                            @RequestParam Integer integerVariable,
                                            @RequestParam Float floatVariable,
                                            @RequestParam Double doubleVariable,
                                            @RequestParam Date date) {
        List<MockPojo> mockPojos = new ArrayList<MockPojo>();

        if (testVariable != null && !testVariable.isEmpty()) {
            MockPojo mockPojo = new MockPojo();
            mockPojo.setId(1);
            mockPojo.setName("Test Pojo");
            mockPojo.setDescription("This is a test pojo for testing purposes.");
            mockPojos.add(mockPojo);
        }

        return mockPojos;
    }

    @RequestMapping(value = "/testPojoRequestParams", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    List<MockPojo> getTestPojoRequestHeader(HttpServletRequest request,
                                            @RequestHeader(("Accept-Encoding")) String encoding) {
        List<MockPojo> mockPojos = new ArrayList<MockPojo>();

        MockPojo mockPojo = new MockPojo();
        mockPojo.setId(1);
        mockPojo.setName("Test Pojo");
        mockPojo.setDescription("This is a test pojo for testing purposes.");
        mockPojos.add(mockPojo);

        return mockPojos;
    }

}
