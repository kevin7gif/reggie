package com.xiehn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiehn.reggie.common.CustomException;
import com.xiehn.reggie.dto.SetmealDto;
import com.xiehn.reggie.mapper.SetmealMapper;
import com.xiehn.reggie.pojo.Setmeal;
import com.xiehn.reggie.pojo.SetmealDish;
import com.xiehn.reggie.service.SetmealDishService;
import com.xiehn.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐的基本信息，操作的是setmeal表，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作的是setmeal_dish表，执行的是insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    /**
     * 删除套餐，同时删除套餐与菜品的关联关系
     * @param ids
     */
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态，判断是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        long count = this.count(queryWrapper);
        if(count>0){
            //说明该套餐状态为起售状态，不能删除
            throw  new CustomException("该套餐为起售状态，不能删除!");
        }

        //如果可以删除，先删除套餐的基本信息，对应setmeal表
        this.removeByIds(ids);
        //再删除套餐与菜品的关联信息，对应setmeal_dish表
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
