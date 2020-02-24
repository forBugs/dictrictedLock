package com.debug.steadyjack.components;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author fanbai
 * @Date 2020/2/23 0:21
 * @Description
 **/
@Component
public class DistributeRedisLock implements InitializingBean {

    @Autowired
    private RedissonClient client;

    public RLock acquireLock(String lockName) {
        // 设置锁定资源名称
        RLock disLock = client.getLock(lockName);

        return disLock;
    }

    public void releaseLock(RLock lock) {
        lock.unlock();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
