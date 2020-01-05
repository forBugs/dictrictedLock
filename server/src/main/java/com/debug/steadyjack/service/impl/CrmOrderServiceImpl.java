package com.debug.steadyjack.service.impl;

import com.debug.steadyjack.dto.RobbingDto;
import com.debug.steadyjack.entity.CrmOrder;
import com.debug.steadyjack.listener.event.CrmOrderRobbingEvent;
import com.debug.steadyjack.mapper.CrmOrderMapper;
import com.debug.steadyjack.mapper.UserMapper;
import com.debug.steadyjack.service.CrmOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CrmOrderServiceImpl implements CrmOrderService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void robbing(RobbingDto dto) throws Exception {
        if (mobileHasRobbedDB(dto.getMobile())) {
            log.info("该手机号已被抢, {}--结束逻辑", dto.getMobile());

        } else {
            log.info("该手机号还没有被抢: {}", dto.getMobile());

            // 手机号插入抢单记录表
            CrmOrder crmOrder = CrmOrder.builder()
                    .mobile(dto.getMobile())
                    .userId(dto.getUserId()).build();
            crmOrderMapper.insertSelective(crmOrder);

            // 发送异步消息通知
            CrmOrderRobbingEvent event = new CrmOrderRobbingEvent(this, dto.getUserId(), dto.getMobile());
            applicationContext.publishEvent(event);

        }

    }

    /**
     * 判断手机号是否已被抢
     * @param mobile
     * @return
     */
    private boolean mobileHasRobbedDB(final String mobile) {
        int count = crmOrderMapper.countByMobile(mobile);

        if (count > 0) {
            return true;
        }
        return false;
    }
}
