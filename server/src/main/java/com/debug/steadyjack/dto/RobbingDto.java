package com.debug.steadyjack.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Created by steadyjack on 2018/9/26.
 */
@Data
@ToString
public class RobbingDto {

    @NotNull
    private Integer userId;

    @NotNull
    private String mobile;
}
