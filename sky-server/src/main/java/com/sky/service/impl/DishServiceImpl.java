package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;

import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;;


    /**
     * 新增菜品和对应的口味
     *
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
        if (flavors != null && flavors.size() > 0) {

            ////有了这个主键值之后需要为DishFlavor每一个id属性赋值，因此就需要给集合遍历一下，
            // 然后把这里的每一个元素也就是DishFlavor对象遍历出来，然后给DishId属性赋值

            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));

            //
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //分页插件
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        //这个Page对象是PageHelper里面的
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 批量删除菜品
     *
     * @param ids
     */

    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品？？
        for (Long id : ids) {
            Dish dish = dishMapper.getByid(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }

        //判断当前菜品是否能够删除---当前彩品是否被套餐关联？

        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIdsByDishIds != null && setmealIdsByDishIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的数据

        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
            
        }



    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */

    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getByid(id);

        //根据菜品id查询查询口味数据
        List<DishFlavor> dishFlavors =dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应口味信息
     * @param dishDTO
     */
    public void updateDishWithFlavor(DishDTO dishDTO) {
        //这个地方可能涉及到两张表得操作：一个是Dish表、另一个是DishFlavor表

        //修改菜品表基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {

            ////有了这个主键值之后需要为DishFlavor每一个id属性赋值，因此就需要给集合遍历一下，
            // 然后把这里的每一个元素也就是DishFlavor对象遍历出来，然后给DishId属性赋值

            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));

            //
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }



    }



    }


