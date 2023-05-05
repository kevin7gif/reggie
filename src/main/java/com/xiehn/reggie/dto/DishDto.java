package com.xiehn.reggie.dto;

import com.xiehn.reggie.pojo.Dish;
import com.xiehn.reggie.pojo.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象，用于在数据层和展示层之间传输数据
 */
@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors=new ArrayList<>();
    private String categoryName;
    private Integer copies;
}
