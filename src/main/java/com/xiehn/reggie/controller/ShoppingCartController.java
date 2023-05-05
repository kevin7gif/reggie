package com.xiehn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiehn.reggie.common.BaseContext;
import com.xiehn.reggie.common.R;
import com.xiehn.reggie.pojo.ShoppingCart;
import com.xiehn.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据："+shoppingCart.toString());

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        Long dishId = shoppingCart.getDishId();
        if(dishId!=null){
            //在购物车里的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //在购物车里的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //SQL: select * from shopping_cart where user_id=? and dish_id (setmeal_id) =?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne!=null){
            //如果已经在购物车中，则数量加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不在购物车中，则数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车中的菜品信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车。。。。");

        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL：delete from shopping_cart where user_id=?
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("删除成功！");
    }

    /**
     * 对购物车中数据进行减一操作
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        //构造条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //通过用户id查询当前用户的购物车
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //判断购物车的菜品信息
        if(dishId!=null){
            //通过dishId查询购物车的菜品数据
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart dishCart = shoppingCartService.getOne(queryWrapper);
            //将查出来的菜品数据减一
            dishCart.setNumber(dishCart.getNumber()-1);
            Integer number = dishCart.getNumber();
            //进行判断
            if(number>0){
                //如果数量大于0，则更新此菜品信息
                shoppingCartService.updateById(dishCart);
            }else if(number==0){
                //将此菜品移除
                shoppingCartService.removeById(dishCart.getId());
            }
            return R.success(dishCart);
        }

        //判断购物车的套餐信息
        if(setmealId!=null){
            //通过setmealId查询购物车的套餐数据
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart setmealCart = shoppingCartService.getOne(queryWrapper);
            //将查出来的套餐数据减一
            setmealCart.setNumber(setmealCart.getNumber()-1);
            Integer setmealCartNumber = setmealCart.getNumber();
            //进行判断
            if(setmealCartNumber>0){
                //如果数量大于0，更新套餐的数量
                shoppingCartService.updateById(setmealCart);
            } else if (setmealCartNumber==0) {
                //数量等于0则直接删除购物车中的套餐信息
                shoppingCartService.removeById(setmealCart.getId());
            }
            return R.success(setmealCart);
        }
        return R.error("操作异常！");
    }
}
