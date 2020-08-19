package com.jiping.staging.controller;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jiping.staging.common.BusinessException;
import com.jiping.staging.common.CommonRes;
import com.jiping.staging.common.CommonUtil;
import com.jiping.staging.common.EmBusinessError;
import com.jiping.staging.model.UserModel;
import com.jiping.staging.request.LoginReq;
import com.jiping.staging.request.RegisterReq;
import com.jiping.staging.service.UserService;

@Controller("/user")
@RequestMapping("/user")
public class UserController {
	
	public static final String CURRENT_USER_SESSION = "currentUserSession";
	@Autowired
	private HttpServletRequest httpServletRequest;
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
    @RequestMapping("register")
    @ResponseBody
    public CommonRes register(@RequestBody @Valid RegisterReq registerReq,BindingResult bindingResult) throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException {
    	if(bindingResult.hasErrors()) {
    		throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,CommonUtil.processErrorString(bindingResult));
    	}
    	UserModel registerUser = new UserModel();
    	registerUser.setTelphone(registerReq.getTelphone());
        registerUser.setPassword(registerReq.getPassword());
        registerUser.setNickName(registerReq.getNickName());
        registerUser.setGender(registerReq.getGender());
        UserModel resUserModel = userService.register(registerUser);
		return CommonRes.create(resUserModel);
    }
    @RequestMapping("login")
    @ResponseBody
    public CommonRes login(@Valid @RequestBody LoginReq loginReq,BindingResult bindingResult) throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException {
    	if(bindingResult.hasErrors()) {
    		throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtil.processErrorString(bindingResult));
    	}
    	UserModel userModel = userService.login(loginReq.getTelphone(),loginReq.getPassword());
    	httpServletRequest.getSession().setAttribute(CURRENT_USER_SESSION, userModel);
    	return CommonRes.create(userModel);
    }
    //获取当前用户信息
    @RequestMapping("/getcurrentuser")
    @ResponseBody
    public CommonRes getCurrentUser(){
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION);
        return CommonRes.create(userModel);
    }
    @RequestMapping("/logout")
    @ResponseBody
    public CommonRes logout() throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        httpServletRequest.getSession().invalidate();
        return CommonRes.create(null);
    }
}
