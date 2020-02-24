package com.debug.steadyjack.service.impl;

import com.debug.steadyjack.dto.UserDto;
import com.debug.steadyjack.entity.User;
import com.debug.steadyjack.mapper.UserMapper;
import com.debug.steadyjack.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author fanbai
 * @Date 2020/2/23 12:35
 * @Description
 **/
@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public int register(UserDto dto) throws Exception {

        User user = userMapper.selectByUserName(dto.getUserName());
        if (Objects.isNull(user)) {

        }

        return 0;
    }
}
