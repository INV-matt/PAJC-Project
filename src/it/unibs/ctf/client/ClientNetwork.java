package it.unibs.ctf.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import it.unibs.ctf.common.GamePacket;
import it.unibs.ctf.common.GameState;
import it.unibs.ctf.common.PacketIO;
import it.unibs.ctf.common.WorldData;

public class ClientNetwork {
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	
	private Consumer<GameState> stateHandler = s -> {};
	private Consumer<WorldData> worldHandler = w -> {};
	private Runnable disconnectHandler = () -> {};
	
	public void setStateHandler(Consumer<GameState> stateHandler) {
		this.stateHandler = stateHandler;
	}
	public void setWorldHandler(Consumer<WorldData> worldHandler) {
		this.worldHandler = worldHandler;
	}
	public void setDisconnectHandler(Runnable disconnectHandler) {
		this.disconnectHandler = disconnectHandler;
	}
	
	public void connect(String host, int port) throws IOException {
		socket = new Socket(host, port);
		socket.setTcpNoDelay(false);
		out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		
		Thread receiverThread = new Thread(this::receiverLoop, "Packet-Receiver");
		receiverThread.setDaemon(true);
		receiverThread.start();
	}
	
	public void send(GamePacket packet) {
		try {
			synchronized (out) { PacketIO.write(out, packet); }
		} catch (IOException e) {
			close();
		}
	}
	
	public void close() {
		try {
			if (socket != null && socket.isClosed()) { 
				socket.close();
				out.close();
			}
		} catch (IOException e){
			// ignore or handle differently
		}
	}
	
	private void receiverLoop() {
		boolean done = false;
		while(!done) {
			GamePacket packet;
			try {
				packet = PacketIO.read(in);
				if (packet instanceof GameState gs) stateHandler.accept(gs); 
				else if (packet instanceof WorldData wd) worldHandler.accept(wd);
			} catch (IOException e) {
				e.printStackTrace();
				// connection lost
				done = true;
			} 
		}
		// loop ended, so disconnect
		disconnectHandler.run();
	}
	
	
	
	
}
