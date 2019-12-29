package com.debug.steadyjack;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.apache.curator.framework.imps.CuratorFrameworkState.STARTED;

@SpringApplicationConfiguration(classes = {ServerApplication.class})
public class ServerApplicationTests {

	private static final Logger log= LoggerFactory.getLogger(ServerApplicationTests.class);


	@Test
	public void contextLoads() {
		log.debug("测试");
	}


	@Test
	public void testZookeeper() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client =
				CuratorFrameworkFactory.newClient(
						"118.190.134.69:2181",
						5000,
						3000,
						retryPolicy);
		client.start();

		if (client.getState() == STARTED) {
			try {
//				client.create().forPath("/fanbai","init".getBytes());
				byte[] bytes = client.getData().forPath("/fanbai");

				System.out.println(new String(bytes));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
