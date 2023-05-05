package com.xiehn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiehn.reggie.common.CustomException;
import com.xiehn.reggie.common.R;
import com.xiehn.reggie.dto.DishDto;
import com.xiehn.reggie.pojo.Category;
import com.xiehn.reggie.pojo.Dish;
import com.xiehn.reggie.pojo.DishFlavor;
import com.xiehn.reggie.service.CategoryService;
import com.xiehn.reggie.service.DishFlavorService;
import com.xiehn.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){//当需要向前端传送json数据时，加上此注解
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功！");
    }

    /**
     * 分页查询菜品信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();

        //对菜品名字进行模糊查询
        queryWrapper.like(name!=null,Dish::getName,name);
        //对筛选出来的菜品按照更新时间降序排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        //records是获取到的菜品信息集合，所以不需要拷贝records这个属性，只需要获得其中的菜品名属性
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        //将records集合中的属性拷贝到list集合中
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();

            //item为遍历出来的每一个菜品对象
            BeanUtils.copyProperties(item,dishDto);

            //获取菜品的分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            //将查询到的对象的name属性赋值给dishDto的name属性
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;

        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    @Transactional
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }

    /**
     * 实现批量菜品状态的停售与起售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("status:{},ids:{}", status, ids);
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ids != null, Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, status);
        dishService.update(updateWrapper);
        return R.success("批量操作成功");
    }

    /**
     * 实现菜品的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除的ids：{}", ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        Long count =  dishService.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("删除列表中存在启售状态商品，无法删除");
        }
        dishService.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);
        return R.success("删除成功");
    }


    /**
     * 根据条件查询对应的菜品信息
     * @param dish
     * @return
     *//*
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/

    /**
     * 根据条件查询对应的菜品信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            //查询菜品对应的分类id
            Long categoryId = item.getCategoryId();
            //根据id查询菜品分类名称
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long itemId = item.getId();
            //根据菜品的id查询菜品对应的口味信息
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,itemId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }


}
