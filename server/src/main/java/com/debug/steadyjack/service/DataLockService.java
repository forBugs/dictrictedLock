package com.debug.steadyjack.service;

import com.debug.steadyjack.dto.ProductLockDto;
import com.debug.steadyjack.entity.ProductLock;
import com.debug.steadyjack.mapper.ProductLockMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;


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
}















































