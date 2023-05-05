package com.xiehn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiehn.reggie.dto.SetmealDto;
import com.xiehn.reggie.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐与菜品的关联关系
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);

}
