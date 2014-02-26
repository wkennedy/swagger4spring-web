package com.knappsack.swagger4springweb.testController;

import com.knappsack.swagger4springweb.testModels.MockPojo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class NoClassLevelMappingController {

	@RequestMapping(value = "/api/v1/noClassLevelMapping", method = RequestMethod.GET, produces = "application/json")
	public
	@ResponseBody
	List<MockPojo> noRoot() {
		return new ArrayList<MockPojo>();
	}
}
