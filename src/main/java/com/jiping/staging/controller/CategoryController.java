package com.jiping.staging.controller;

import com.jiping.staging.common.CommonRes;
import com.jiping.staging.model.CategoryModel;
import com.jiping.staging.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller("/category")
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ResponseBody
    @RequestMapping("/list")
    public CommonRes list(){
        List<CategoryModel> categoryModelList = categoryService.selectAll();
        return CommonRes.create(categoryModelList);

    }

}
