package de.hsma.ppr.blockchain.nodes.ws;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.messages.*;
import org.whispersystems.websocket.messages.protobuf.ProtobufWebSocketMessageFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@WebSocket(maxTextMessageSize = 64 * 1024, maxIdleTime = 30000)
public class WebSocketInterface
{

	public interface Listener
	{
		void onReceivedRequest(WebSocketRequestMessage requestMessage);

		void onReceivedResponse(WebSocketResponseMessage responseMessage);
	}

	public static abstract class WebSocketListener implements Listener
	{
		@Override
		public void onReceivedRequest(WebSocketRequestMessage requestMessage)
		{
			logger.trace("Received Request [id: {}, code: {}, message: {}, body: {}]",
			             requestMessage.getRequestId(),
			             requestMessage.getVerb(),
			             requestMessage.getPath(),
			             requestMessage.getBody());
		}

		@Override
		public void onReceivedResponse(WebSocketResponseMessage responseMessage)
		{
			if (responseMessage.getBody().isPresent())
			{
				String body = new String(responseMessage.getBody().get());
				logger.trace("Received Response: [id: {}, status: {}, message: {}, body: {}]",
				             responseMessage.getRequestId(),
				             responseMessage.getStatus(),
				             responseMessage.getMessage(),
				             body);
			} else
			{
				logger.trace("Received Response: [id: {}, status: {}, message: {}]",
				             responseMessage.getRequestId(),
				             responseMessage.getStatus(),
				             responseMessage.getMessage());
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(WebSocketInterface.class);

	private final WebSocketMessageFactory															factory						= new ProtobufWebSocketMessageFactory();
	private final Map<Long, SettableFuture<WebSocketResponseMessage>>	pendingRequestMap	= new ConcurrentHashMap<>();

	private Session					session;
	private RemoteEndpoint	remote;
	private Listener				requestListener;

	private WebSocketInterface()
	{
	}

	public static WebSocketInterface connect(String url) throws ConnectException
	{
		return new WebSocketInterface().doConnect(url);
	}

	private WebSocketInterface doConnect(String url) throws ConnectException
	{
		String target = String.format("%s/ws/", url);
		try
		{
			WebSocketClient client = new WebSocketClient();
			client.start();

			URI uri = new URI(target);
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(this, uri, request).get(10, TimeUnit.SECONDS);
		} catch (Throwable t)
		{
			if (hasCause(t, ConnectException.class)) { throw (java.net.ConnectException) t.getCause(); }
			t.printStackTrace();
			// TODO
			throw new RuntimeException(String.format("Failed to establish websocket connection to %s.", target), t);
		}
		return this;
	}

	private boolean hasCause(Throwable t, Class<ConnectException> clazz)
	{
		return t.getCause() != null && t.getCause().getClass() == clazz;
	}

	public void setListener(Listener requestListener)
	{
		this.requestListener = requestListener;
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason)
	{
		for (long requestId : pendingRequestMap.keySet())
		{
			SettableFuture<?> outstandingRequest = pendingRequestMap.remove(requestId);

			if (outstandingRequest != null)
			{
				outstandingRequest.setException(new IOException("Connection closed!"));
			}
		}
	}

	@OnWebSocketConnect
	public void onConnect(Session session)
	{
		this.session = session;
		this.remote = session.getRemote();
	}

	@OnWebSocketMessage
	public void onMessage(byte[] buffer, int offset, int length)
	{
		try
		{
			WebSocketMessage message = factory.parseMessage(buffer, offset, length);

			if (message.getType() == WebSocketMessage.Type.REQUEST_MESSAGE)
			{
				handleRequest(message.getRequestMessage());
			} else if (message.getType() == WebSocketMessage.Type.RESPONSE_MESSAGE)
			{
				handleResponse(message.getResponseMessage());
			} else
			{
				System.out.println("Received websocket message of unknown type: " + message.getType());
				// TODO
			}
		} catch (InvalidMessageException e)
		{
			e.printStackTrace();
			// TODO
		}
	}

	private void handleResponse(WebSocketResponseMessage responseMessage)
	{
		logger.trace("Received WebSocketResponse: [id: {}, status: {}, message: {}]",
		             responseMessage.getRequestId(),
		             responseMessage.getStatus(),
		             responseMessage.getMessage());
		SettableFuture<WebSocketResponseMessage> future = pendingRequestMap.remove(responseMessage.getRequestId());

		if (future != null)
		{
			future.set(responseMessage);
		}
	}

	private void handleRequest(WebSocketRequestMessage requestMessage)
	{
		logger.trace("Received WebSocketRequest: [id: {}, verb: {}, path: {}]",
		             requestMessage.getRequestId(),
		             requestMessage.getVerb(),
		             requestMessage.getPath());
		requestListener.onReceivedRequest(requestMessage);
	}

	public ListenableFuture<WebSocketResponseMessage> sendRequest(String verb, String path)
	{
		return sendRequest(generateRequestId(), verb, path);
	}

	private ListenableFuture<WebSocketResponseMessage> sendRequest(long requestId, String verb, String path)
	{
		return sendRequest(requestId, verb, path, Optional.empty());
	}

	private ListenableFuture<WebSocketResponseMessage> sendRequest(long requestId,
	                                                               String verb,
	                                                               String path,
	                                                               Optional<byte[]> body)
	{
		LinkedList<String> headers = new LinkedList<>();
		headers.add("Content-Type: application/json");

		WebSocketMessage requestMessage = factory.createRequest(Optional.of(requestId), verb, path, headers, body);
		final SettableFuture<WebSocketResponseMessage> future = SettableFuture.create();

		pendingRequestMap.put(requestId, future);

		logger.trace("Sending message: [id: {}, verb: {}, path: {}]", requestId, verb, path);

		try
		{
			remote.sendBytes(ByteBuffer.wrap(requestMessage.toByteArray()), new WriteCallback() {
				@Override
				public void writeFailed(Throwable x)
				{
					logger.debug("Write failed", x);
					pendingRequestMap.remove(requestId);
					future.setException(x);
				}

				@Override
				public void writeSuccess()
				{
				}
			});
		} catch (WebSocketException e)
		{
			logger.debug("Write", e);
			pendingRequestMap.remove(requestId);
			future.setException(e);
		}
		return future;
	}

	public ListenableFuture<WebSocketResponseMessage> sendRequest(String verb, String path, byte[] body)
	{
		return sendRequest(generateRequestId(), verb, path, Optional.of(body));
	}

	public void sendResponse(long id, int code, String message, byte[] body) throws IOException
	{
		sendResponse(id, code, message, Optional.of(body));
	}

	private void sendResponse(long id, int code, String message, Optional<byte[]> body) throws IOException
	{
		logger.trace("Sending WebSocketResponse: [id: {}, code: {}, message: {}, body: {}]", id, code, message, body);
		LinkedList<String> headers = new LinkedList<>();
		headers.add("Content-Type: application/json");
		WebSocketMessage response = factory.createResponse(id, code, message, headers, body);
		session.getRemote().sendBytes(ByteBuffer.wrap(response.toByteArray()));
	}

	public void sendResponse(long requestId, int code, String message) throws IOException
	{
		sendResponse(requestId, code, message, Optional.empty());
	}

	private static long generateRequestId()
	{
		return Math.abs(new SecureRandom().nextLong());
	}
}
