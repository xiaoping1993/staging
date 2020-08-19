package com.jiping.staging.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.jiping.staging.common.BusinessException;
import com.jiping.staging.model.UserModel;

public interface UserService {
	
	UserModel getUser(Integer Id);
	
	UserModel register(UserModel registerUser) throws NoSuchAlgorithmException, UnsupportedEncodingException, BusinessException;
	
	UserModel login(String telphone, String password) throws BusinessException, NoSuchAlgorithmException, UnsupportedEncodingException;
	Integer countAllUser();

}
