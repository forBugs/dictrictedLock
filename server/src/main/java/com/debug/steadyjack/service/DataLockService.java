package com.debug.steadyjack.service;

import com.debug.steadyjack.components.DistributeRedisLock;
import com.debug.steadyjack.dto.ProductLockDto;
import com.debug.steadyjack.entity.ProductLock;
import com.debug.steadyjack.mapper.ProductLockMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/10/17.
 */
@Service
public class DataLockService {

    private static final Logger log= LoggerFactory.getLogger(DataLockService.class);

    @Autowired
    private ProductLockMapper lockMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 记录成功获取到锁的线程总个数
    private static final String countKey = "redis:key:count";

    @Autowired
    private CuratorFramework client;

    private static final String lockPrifix = "/zklock/mylock";
    @Autowired
    private Environment env;

    @Autowired
    private DistributeRedisLock distributeRedisLock;



    /**
     * 正常更新商品库存 - 重现了高并发的场景
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStock(ProductLockDto dto) throws Exception{
        int res=0;

        ProductLock entity=lockMapper.selectByPrimaryKey(dto.getId());
        if (entity!=null && entity.getStock().compareTo(dto.getStock())>=0){
            entity.setStock(dto.getStock());
            return lockMapper.updateStock(entity);
        }


        return res;
    }


    /**
     * 数据库乐观锁：添加version版本号字段
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStockPositive(ProductLockDto dto) throws Exception{
        int res=0;

        // 读取db库存时带出相应的version版本号
        ProductLock entity=lockMapper.selectByPrimaryKey(dto.getId());
        if (entity!=null && entity.getStock().compareTo(dto.getStock())>=0){
            // 更新db库存时需要判断version字段并递增
            entity.setStock(dto.getStock());
            res = lockMapper.updateStockV1(entity);
            if (res > 0) {
                log.info("更新成功的数据：=>{}", dto.getStock());
            }
            return res;
        }


        return res;
    }


    /**
     * 数据库悲观锁：读取时加for update
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStockNegitive(ProductLockDto dto) throws Exception{
        int res=0;

        // 读取db库存时通过for update给该行记录加锁，其他读线程或写线程会阻塞
        ProductLock entity=lockMapper.selectByPKForNegative(dto.getId());
        if (entity!=null && entity.getStock().compareTo(dto.getStock())>=0){

            entity.setStock(dto.getStock());
            res = lockMapper.updateStockForNegative(entity);
            if (res > 0) {
                log.info("更新成功的数据：=>{}", dto.getStock());
            }
            return res;
        }


        return res;
    }

    /**
     * redis分布式锁
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStockRedis(ProductLockDto dto) throws Exception{
        int result = 0;
        boolean res = true;
        final String key = "redis:product:id:" + dto.getId();


        while (res) {
            String value = UUID.randomUUID().toString() + System.currentTimeMillis();
            res = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
            if (res) {
                try {
                    stringRedisTemplate.opsForValue().increment(countKey, 1L);
                    res = false;
                    // 执行真正的业务逻辑
                    result = 0;
                    ProductLock entity = lockMapper.selectByPrimaryKey(dto.getId());
                    if (entity != null && entity.getStock().compareTo(dto.getStock()) >= 0) {

                        entity.setStock(dto.getStock());
                        result = lockMapper.updateStock(entity);
                        if (result > 0) {
                            log.info("redis分布式锁获取成功：=>{}", dto.getStock());
                        }
                        return result;
                    } else {
                        // 发送库存不够短信，稍后处理
                        log.info("该商品库存不够了，请选择其他商品或稍后再试");
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (Objects.equals(value, stringRedisTemplate.opsForValue().get(key))) {
                        stringRedisTemplate.delete(key);
                    }
                }

            } else {
                res = true;
                Thread.sleep(1000);

            }
        }



        return result;
    }

    /**
     * zookeeper分布式锁
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStockZookeeper(ProductLockDto dto) throws Exception{
        int result = 0;

        //
        InterProcessLock lock = new InterProcessMutex(client, lockPrifix + dto.getId());

        try {
            // do some work inside of the critical section here

            if (lock.acquire(10L, TimeUnit.SECONDS)) {
                // 执行真正的业务逻辑
                ProductLock entity = lockMapper.selectByPrimaryKey(dto.getId());
                if (entity != null && entity.getStock().compareTo(dto.getStock()) >= 0) {

                    entity.setStock(dto.getStock());
                    result = lockMapper.updateStock(entity);
                    if (result > 0) {
                        log.info("zookeeper分布式锁获取成功：=>{}", dto.getStock());
                    }
                    return result;
                } else {
                    // 发送库存不够短信，稍后处理
                    log.info("该商品库存不够了，请选择其他商品或稍后再试");
                }

            } else {
                throw new RuntimeException("获取zookeeper锁失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.release();
        }

        return result;
    }
    /**
     * 基于redisson分布式锁
     * @param dto
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateStockRedisson(ProductLockDto dto) throws Exception{
        int result = 0;

        // 设置共享资源名称
        RLock lock = distributeRedisLock.acquireLock(dto.getId() + "");
        boolean isLock;
        try {
            //尝试获取分布式锁
            // 加锁以后10秒钟自动解锁
            // 无需调用unlock方法手动解锁
            isLock = lock.tryLock(10, TimeUnit.SECONDS);

            // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
//            islock = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if (isLock) {
                //TODO if get lock success, do something;
                ProductLock entity = lockMapper.selectByPrimaryKey(dto.getId());
                if (entity != null && entity.getStock().compareTo(dto.getStock()) >= 0) {

                    entity.setStock(dto.getStock());
                    result = lockMapper.updateStock(entity);
                    if (result > 0) {
                        log.info("redisson分布式锁获取成功：=>{}", dto.getStock());
                    }
                    return result;
                } else {
                    // 发送库存不够短信，稍后处理
                    log.info("该商品库存不够了，请选择其他商品或稍后再试");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 无论如何, 最后都要解锁
            distributeRedisLock.releaseLock(lock);
        }

        return result;
    }




}















































