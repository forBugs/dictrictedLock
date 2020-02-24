package com.debug.steadyjack.service;

import com.debug.steadyjack.dto.UserDto;

/**
 * @Author fanbai
 * @Date 2020/2/23 12:33
 * @Description
 **/
public interface IUserService {

    int register(UserDto dto) throws Exception;
}
