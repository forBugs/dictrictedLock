package com.debug.steadyjack.controller;

import com.debug.steadyjack.dto.ProductLockDto;
import com.debug.steadyjack.dto.UserDto;
import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import com.debug.steadyjack.service.DataLockService;
import com.debug.steadyjack.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Administrator on 2018/10/17.
 */
@RestController
public class UserController {

    private static final Logger log= LoggerFactory.getLogger(UserController.class);

    private static final String prefix="/user";

    @Autowired
    private IUserService userService;

    /**
     * 更新商品库存-1
     * @param dto
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = prefix+"/register",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse dataBasePositive(@RequestBody @Validated UserDto dto, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return new BaseResponse(StatusCode.InvalidParam);
        }

        BaseResponse response=new BaseResponse(StatusCode.Ok);
        try {
            log.debug("当前注册请求数据：{} ",dto);

            int res = userService.register(dto);
            if (res<=0) {
                return new BaseResponse(StatusCode.Fail);
            }
        }catch (Exception e){
            log.error("发生异常：",e.fillInStackTrace());
            response=new BaseResponse(StatusCode.Fail);
        }
        return response;
    }


}

















































