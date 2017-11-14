package com.uttara.sem.listeners;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolInitializer implements ServletContextListener{
	private static JedisPool pool = null;
	private ServletContextEvent sce = null;
	
	public static JedisPool getPool(){
		return pool;
	}

	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Inside contextInitialized() of JedisPoolInitializer");
		pool = new JedisPool(new JedisPoolConfig(),"localhost");
	}
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Inside contextDestroyed() of JedisPoolInitializer");
		if(pool!=null){
			pool.getResource().save();
			pool.destroy();
		}
		System.out.flush();
	}
}
