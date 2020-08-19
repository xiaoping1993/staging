package com.jiping.staging.service;


import java.util.List;

import com.jiping.staging.common.BusinessException;
import com.jiping.staging.model.SellerModel;

public interface SellerService {

    SellerModel create(SellerModel sellerModel);
    SellerModel get(Integer id);
    List<SellerModel> selectAll();
    SellerModel changeStatus(Integer id,Integer disabledFlag) throws BusinessException;
    Integer countAllSeller();	
}
