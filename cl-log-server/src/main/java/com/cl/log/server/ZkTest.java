package com.cl.log.server;

import com.cl.log.config.register.ZkRegister;

public class ZkTest {

	public static void main(String[] args) throws InterruptedException {

		ZkRegister.getInstance().register("192.168.1.1:1111", "192.168.1.1:1111");
		ZkRegister.getInstance().register("192.168.1.2:2222", "192.168.1.2:2222");
		ZkRegister.getInstance().register("192.168.1.3:3333", "192.168.1.3:3333");
		ZkRegister.getInstance().register("192.168.1.4:4444", "192.168.1.4:4444");
		ZkRegister.getInstance().register("192.168.1.5:5555", "192.168.1.5:5555");

		while (true){
			Thread.sleep(1000);
			ZkRegister.getInstance().getAvailableUrl();
		}
	}
}
