package de.hsma.ppr.blockchain.bootnode;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class BootNodeTest
{
	public static void main(String[] argv)
	{
		org.eclipse.jetty.websocket.client.WebSocketClient holder = new WebSocketClient();
		WebSocketInterface webSocket = new WebSocketInterface();
		Client client = new Client(webSocket);

		StdErrLog logger = new StdErrLog();
		logger.setLevel(StdErrLog.LEVEL_OFF);
		Log.setLog(logger);

		try
		{
			webSocket.setListener(client);
			holder.start();

			URI uri = new URI("ws://localhost:8080/ws/");
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			holder.connect(webSocket, uri, request);

			System.out.printf("Connecting...");
			Thread.sleep(10000);
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
