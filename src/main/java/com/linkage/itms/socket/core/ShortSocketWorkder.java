
package com.linkage.itms.socket.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

/**
 * <pre>
 * Socket短连接实现方式
 * 启动一个线程进行Socket监听，启动一个线程池，大小可配置，来进行Socket消息处理 ，提高并发性
 * </pre>
 * 
 * @author fangchao (Ailk No.)
 * @version 1.0
 * @since 2014-3-6
 * @category com.linkage.itms.socket
 * @copyright Ailk NBS-Network Mgt. RD Dept.
 */
public class ShortSocketWorkder extends SocketWorker
{

	private ServerSocket server = null;
	private ExecutorService service = null;

	protected ShortSocketWorkder(int port, int processThreads)
	{
		this.port = port;
		this.processThreads = processThreads;
	}

	public void start()
	{
		init();
		Thread thread = new Thread("ShortSocketKeeper")
		{

			@Override
			public void run()
			{
				try
				{
					Socket client = null;
					while ((client = server.accept()) != null)
					{
						handle(client);
					}
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		};
		thread.start();
	}

	private void init()
	{
		try
		{
			logger.warn("start ServerSocket by port[{}] with processing threads[{}]",
					port, processThreads);
			server = new ServerSocket(port);
			service = Executors.newFixedThreadPool(processThreads);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private void handle(final Socket client)
	{
		service.execute(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					doExecute(client);
				}
				catch (Throwable e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	private void doExecute(Socket client) throws IOException
	{
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try
		{
			long sTime = System.currentTimeMillis();
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = reader.readLine();
			logger.warn("receive socket message:[{}]", message);
			String response = msgDispatcher.dispatch(message);
			writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			writer.write(response);
			logger.warn("response socket message[{}], cost time[{}]ms", response,
					System.currentTimeMillis() - sTime);
		}
		finally
		{
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(reader);
			close(client);
		}
	}

	private void close(Socket client)
	{
		if (client != null)
		{
			try
			{
				client.close();
			}
			catch (IOException e)
			{
				logger.error("close socket failed(ignored)", e);
			}
		}
	}
}
