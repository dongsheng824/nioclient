package com.guods.nioclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient {
	// 通道管理器
	private Selector selector;
	private SocketChannel channel;
	private ClientKeyHandler keyHandler = new ClientKeyHandler();

	public static void main(String[] args) throws IOException {
		Thread.currentThread().setName("thread-" + System.currentTimeMillis());
		NioClient client = new NioClient();
		client.initClient("localhost", 8888);
		client.listening();
		client.close();
	}
	
	public void initClient(String ip, int port) throws IOException {
		// 获得一个Socket通道
		channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		// 用channel.finishConnect();才能完成连接
		channel.connect(new InetSocketAddress(ip, port));
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	/**
	 * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 * 
	 * @throws IOException
	 */
	public void listening() throws IOException {
		System.out.println("客户端已启动：");
		// 轮询访问selector
		int i = 0;
		while (i < 3) {
			selector.select();
			// 获得selector中选中的项的迭代器
			Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理
				ite.remove();
				keyHandler.handleKey(selector, key);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
	
	public void close() throws IOException{
		if (channel != null) {
			channel.close();
		}
		if (selector != null) {
			selector.close();
		}
	}

}
