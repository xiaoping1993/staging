package com.jiping.staging.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jiping.staging.common.BusinessException;
import com.jiping.staging.common.CommonRes;
import com.jiping.staging.common.EmBusinessError;
import com.jiping.staging.model.UserModel;
import com.jiping.staging.service.UserService;

@Controller("/user")
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "test";
    }
    
    @RequestMapping("/index")
    public ModelAndView index(){
        String userName = "xiaoping";
        ModelAndView modelAndView = new ModelAndView("/index.html");
        modelAndView.addObject("name",userName);
        return modelAndView;
    }
    
    @RequestMapping("getUser")
    @ResponseBody
    public CommonRes getUser(@RequestParam(name="id")Integer id) throws BusinessException {
    	UserModel user =  userService.getUser(id);
    	if(user == null) {
    		//return CommonRes.create(new CommonError(EmBusinessError.NO_OBJECT_FOUND), "fail");
    		throw new BusinessException(EmBusinessError.NO_OBJECT_FOUND);
    	}
    	return CommonRes.create(user);
    }
}
