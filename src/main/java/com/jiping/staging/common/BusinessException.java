package com.jiping.staging.common;

/**
 * 异常处理类
 * @author wangj01052
 *
 */
public class BusinessException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CommonError commonError;
	
	/**
	 * @return the commonError
	 */
	public CommonError getCommonError() {
		return commonError;
	}

	/**
	 * @param commonError the commonError to set
	 */
	public void setCommonError(CommonError commonError) {
		this.commonError = commonError;
	}

	public BusinessException(EmBusinessError emBusinessError) {
		 super();
		 this.commonError=new CommonError(emBusinessError);
	}

	public BusinessException(EmBusinessError emBusinessError, String errMsg) {
		super();
		this.commonError=new CommonError(emBusinessError);
		commonError.setErrMsg(errMsg);
	}
	
}
