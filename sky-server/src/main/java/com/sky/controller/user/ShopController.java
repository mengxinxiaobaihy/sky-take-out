package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate  redisTemplate;

    public static final String KEY="SHOP_STATUS";


    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        //本来是这样
//        Object shop_status = redisTemplate.opsForValue().get("SHOP_STATUS");

        //因为我们当时放进去的是Integer这种类型来存的，现在取出来就可以给他强转回来
        //说白了当时放进去使用什么类型，现在取出来也用什么类型接收就可以了
        Integer shop_status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺营业状态为：{}",shop_status==1?"营业中":"打烊中");
        return Result.success(shop_status);
    }
}
