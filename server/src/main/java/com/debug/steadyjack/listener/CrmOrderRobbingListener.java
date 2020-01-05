package com.debug.steadyjack.listener;

import com.debug.steadyjack.entity.User;
import com.debug.steadyjack.listener.event.CrmOrderRobbingEvent;
import com.debug.steadyjack.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * spring中的异步事件监听模型
 */
@Slf4j
@Component
public class CrmOrderRobbingListener implements ApplicationListener<CrmOrderRobbingEvent> {

    @Autowired
    private UserMapper userMapper;


    @Override
    public void onApplicationEvent(CrmOrderRobbingEvent crmOrderRobbingEvent) {
        String msg = "%s,恭喜您，手机号%s已被您成功抢到，请完成后续业务操作";
        User user = userMapper.selectByPrimaryKey(crmOrderRobbingEvent.getUserId());
        if (Objects.nonNull(user)) {
            msg = String.format(msg, user.getUserName(), crmOrderRobbingEvent.getMobile());
            log.info("cms系统成功监听到并发送抢单消息：-->{}", msg);

        }
    }
}
