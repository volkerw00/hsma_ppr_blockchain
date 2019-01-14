package de.hsma.ppr.blockchain.nodes.dropwizard;

import io.dropwizard.setup.Environment;
import org.whispersystems.websocket.WebSocketResourceProviderFactory;
import org.whispersystems.websocket.configuration.WebSocketConfiguration;
import org.whispersystems.websocket.setup.WebSocketEnvironment;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Arrays;

public class WebsocketApplicationHelper
{
	public static void registerWebSocketServlet(Environment environment, WebSocketEnvironment webSocketEnvironment)
	    throws ServletException
	{
		WebSocketResourceProviderFactory servlet = new WebSocketResourceProviderFactory(webSocketEnvironment);
		ServletRegistration.Dynamic webSocket = environment.servlets().addServlet("WebSocket", servlet);

		webSocket.addMapping("/ws/*");
		webSocket.setAsyncSupported(true);
		servlet.start();
	}

	public static WebSocketEnvironment registerWebsocketComponent(WebSocketConfiguration configuration,
	                                                              Environment environment,
	                                                              Object... components)
	{
		WebSocketEnvironment webSocketEnvironment = new WebSocketEnvironment(environment, configuration);
		Arrays.stream(components).forEach(component -> webSocketEnvironment.jersey().register(component));
		return webSocketEnvironment;
	}
}
