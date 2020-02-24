package com.debug.steadyjack;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ImportResource(locations = "${classpath:spring/spring-jdbc.xml}")
@MapperScan(basePackages = "com.debug.steadyjack.mapper")
public class ServerApplication {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Bean
	public CuratorFramework curatorFramework() {
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(env.getProperty("zookeeper.url"))
				.sessionTimeoutMs(1000)    // 连接超时时间
				.connectionTimeoutMs(1000) // 会话超时时间
				// 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();
		client.start();
		return client;
	}

	@Bean
	public RedissonClient redissonClient() {
		// 构造redisson实现分布式锁必要的Config
		Config config = new Config();
		config.useSingleServer().setAddress(env.getProperty("redisson.url")).setDatabase(0);
		// 构造RedissonClient
		RedissonClient redissonClient = Redisson.create(config);
		return redissonClient;
	}

}

























