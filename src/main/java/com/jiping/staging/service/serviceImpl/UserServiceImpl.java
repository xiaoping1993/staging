package com.jiping.staging.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiping.staging.dal.UserModelMapper;
import com.jiping.staging.model.UserModel;
import com.jiping.staging.service.UserService;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserModelMapper userModelMapper;
	@Override
	public UserModel getUser(Integer id) {
		return userModelMapper.selectByPrimaryKey(id);
	}

}
