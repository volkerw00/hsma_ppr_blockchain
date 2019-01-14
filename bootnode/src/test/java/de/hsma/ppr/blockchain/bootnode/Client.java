package de.hsma.ppr.blockchain.bootnode;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.whispersystems.websocket.messages.WebSocketRequestMessage;
import org.whispersystems.websocket.messages.WebSocketResponseMessage;

import java.io.IOException;

@WebSocket(maxTextMessageSize = 64 * 1024, maxIdleTime = 30000)
public class Client implements WebSocketInterface.Listener
{

	private final WebSocketInterface webSocket;

	public Client(WebSocketInterface webSocket)
	{
		this.webSocket = webSocket;
	}

	@Override
	public void onReceivedRequest(WebSocketRequestMessage requestMessage)
	{
		System.err.println("Got request");
		if ("POST".equals(requestMessage.getVerb()) && "/updateClients".equals(requestMessage.getPath()))
		{
			try
			{
				webSocket.sendResponse(requestMessage.getRequestId(), 200, "OK", null);
				webSocket.sendRequest(requestMessage.getRequestId() + 1, "GET", "/ws/clients");

			} catch (IOException e)
			{
				e.printStackTrace();
				// TODO
			}

		} else
		{
			try
			{
				webSocket.sendResponse(requestMessage.getRequestId(), 404, "NOT FOUND", null);
			} catch (IOException e)
			{
				e.printStackTrace();
				// TODO
			}
		}
	}

	@Override
	public void onReceivedResponse(WebSocketResponseMessage responseMessage)
	{
		System.err.println("Got response: " + responseMessage.getStatus());

		if (responseMessage.getBody().isPresent())
		{
			System.err.println("Got response body: " + new String(responseMessage.getBody().get()));
		}
	}

	@Override
	public void onClosed()
	{
		System.err.println("onClosed()");
	}

	@Override
	public void onConnected()
	{
		try
		{
			webSocket.sendRequest(1, "POST", "/ws/hello");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
