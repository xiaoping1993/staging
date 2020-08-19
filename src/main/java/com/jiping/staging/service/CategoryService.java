package com.jiping.staging.service;

import com.jiping.staging.common.BusinessException;
import com.jiping.staging.model.CategoryModel;

import java.util.List;

public interface CategoryService {

    CategoryModel create(CategoryModel categoryModel) throws BusinessException;
    CategoryModel get(Integer id);
    List<CategoryModel> selectAll();

    Integer countAllCategory();
}
