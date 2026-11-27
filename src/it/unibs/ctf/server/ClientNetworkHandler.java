package it.unibs.ctf.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

import it.unibs.ctf.common.GamePacket;
import it.unibs.ctf.common.InputData;
import it.unibs.ctf.common.PacketIO;

public class ClientNetworkHandler implements Runnable{
	private final Socket socket;
	private final int playerId;
	private final ServerController controller;
	private final Consumer<ClientNetworkHandler> closeCallback;
	private final DataInputStream in;
	private final DataOutputStream out;
	
	private volatile boolean closed;	
	
	public ClientNetworkHandler(Socket socket, int playerId, ServerController ctrl, 
			Consumer<ClientNetworkHandler> onClose) throws IOException {
		this.socket = socket; 
		this.socket.setTcpNoDelay(false);
		this.playerId = playerId;
		this.controller = ctrl;
		this.closeCallback = onClose;
		
		this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}
	
	public int getId() { return this.playerId; }

	@Override
	public void run() {
		try {
			while(!closed) {
				GamePacket packet = PacketIO.read(in);
				if (packet instanceof InputData input) {
					controller.onPlayerInput(playerId, input);
				}
			} 
		} catch (IOException e) {
				
		} finally {
			close();
		}
	}
	
	public void send(GamePacket packet) {
		if (closed) return;
		try {
			synchronized (out) {
				PacketIO.write(out, packet);
			}
		} catch (IOException e) {
			close();
		}
	}
	
	public void close() {
		if (closed) return;
		
		closed = true;
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			// error while closing client. ignore
		}
		closeCallback.accept(this);
	}
}
