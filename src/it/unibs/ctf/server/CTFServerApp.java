package it.unibs.ctf.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unibs.ctf.common.Globals;

public class CTFServerApp {
	private final int port;
	private ServerSocket server;
	private ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
	
	private final ServerGameModel state = new ServerGameModel();
	private final GameStateParser stateParser = new GameStateParser();
	private final ServerController serverController = new ServerController(state, stateParser);
	private final Map<Integer, ClientNetworkHandler> clients = new ConcurrentHashMap<>();
	
	public CTFServerApp(int port) {
		this.port = port;
	}
	
	public static void main(String[] args) {
		try { 
			int _p = args.length > 0 ? Integer.parseInt(args[0]) : Globals.PORT;
			new CTFServerApp(_p).start();
			System.out.printf("Server started on port: %d\n", _p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start() throws IOException {
		server = new ServerSocket(port);
		serverController.start();
		new Thread(this::acceptLoop, "Client-Accept").start();
	}
	
	public void stop() {
		serverController.stop();
		try {
			if (!server.isClosed()) server.close();
		} catch (IOException e) {
			System.err.println("IOEx while closing server");
		}
		clients.values().forEach(ClientNetworkHandler::close);
	}
	
	private void acceptLoop() {
		while(!server.isClosed()) {
			try {
				Socket client = server.accept();
				handleJoin(client);
			} catch (IOException e) {
				if (!server.isClosed()) e.printStackTrace();
			}
		}
	}
	
	private synchronized void handleJoin(Socket client) throws IOException {
		int freeId = getFreeId();
		if (freeId < 0) {
			client.close();
			return;
		}
		
		ClientNetworkHandler handler = new ClientNetworkHandler(client, freeId, serverController, this::onClientClosed);
		clients.put(freeId, handler);
		stateParser.addClient(handler);
		serverController.onPlayerJoined(freeId);
		exec.submit(handler);
		System.out.printf("Player %d joined\n", freeId);
		
	}
	
	private int getFreeId() {
		for (int i = 1; i < Globals.MAX_PLAYERS; ++i) {
			if (!clients.containsKey(i)) return i;
		}
		return -1;
	}
	
	private void onClientClosed(ClientNetworkHandler handler) {
		int id = handler.getId();
		if (clients.remove(id, handler)) {
			stateParser.removeClient(handler);
			serverController.onPlayerLeft(id);
			System.out.printf("Player %d has left the game\n", id);
			
		}
	}

}
