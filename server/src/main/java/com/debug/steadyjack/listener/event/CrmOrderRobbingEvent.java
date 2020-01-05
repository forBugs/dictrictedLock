package com.debug.steadyjack.listener.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class CrmOrderRobbingEvent extends ApplicationEvent {
    private Integer userId;

    private String mobile;


    public CrmOrderRobbingEvent(Object source, Integer userId, String mobile) {
        super(source);
        this.userId = userId;
        this.mobile = mobile;
    }
}
