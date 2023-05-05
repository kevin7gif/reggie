package com.xiehn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiehn.reggie.pojo.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
