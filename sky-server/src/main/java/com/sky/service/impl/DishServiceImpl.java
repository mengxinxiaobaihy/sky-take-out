package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;

import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //这个地方可能涉及到两张表的操作：一个是Dish表、另一个是DishFlavor表
        //需要保持数据的一致性，所以得加上事务


        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //1、向菜品表插入数据一条数据
        dishMapper.insert(dish);//这里得参数注意一下，这里dishDTO对象中包含了菜品得口味数据，现在只是向菜品表插入
        //所以这里只需要传入实体对象即可


        //获取insert语句生成的主键值
        Long dishId = dish.getId();



        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){

            ////有了这个主键值之后需要为DishFlavor每一个id属性赋值，因此就需要给集合遍历一下，
            // 然后把这里的每一个元素也就是DishFlavor对象遍历出来，然后给DishId属性赋值

            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));

            //
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch( flavors);

        }
    }
}
