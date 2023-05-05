package com.xiehn.reggie.dto;

import com.xiehn.reggie.pojo.Setmeal;
import com.xiehn.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
