package com.debug.steadyjack.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Author fanbai
 * @Date 2020/2/23 12:30
 * @Description
 **/
@Data
public class UserDto {
    @NotBlank
    private String userName;

    @NotBlank
    private String email;

}
