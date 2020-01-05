package com.debug.steadyjack.controller;

import com.debug.steadyjack.dto.ProductLockDto;
import com.debug.steadyjack.dto.RobbingDto;
import com.debug.steadyjack.entity.User;
import com.debug.steadyjack.mapper.UserMapper;
import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import com.debug.steadyjack.service.CrmOrderService;
import com.debug.steadyjack.service.DataLockService;
import org.apache.ibatis.javassist.runtime.DotClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;


/**
 * Created by Administrator on 2018/10/17.
 */
@RestController
public class CrmOrderController {

    private static final Logger log= LoggerFactory.getLogger(CrmOrderController.class);

    private static final String prefix="crm/order";

    @Autowired
    private CrmOrderService crmOrderService;

    @Autowired
    private UserMapper userMapper;


    /**
     * crm销售人员抢单
     * @param dto
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = prefix+"/robbing",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse dataBasePositive(@RequestBody @Validated RobbingDto dto, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return new BaseResponse(StatusCode.InvalidParam);
        }

        BaseResponse response=new BaseResponse(StatusCode.Ok);

        User user = userMapper.selectByPrimaryKey(dto.getUserId());
        if (Objects.isNull(user)) {
            response = new BaseResponse(StatusCode.UserNotExist);
        }
        try {

            crmOrderService.robbing(dto);

        }catch (Exception e){
            log.error("发生异常：",e.fillInStackTrace());
            response=new BaseResponse(StatusCode.Fail);
        }
        return response;
    }




}

















































