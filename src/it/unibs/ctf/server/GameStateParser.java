package it.unibs.ctf.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


import it.unibs.ctf.common.GameState;
import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.PlayerState;
import it.unibs.ctf.common.ServerMsg;
import it.unibs.ctf.common.WorldData;

public class GameStateParser {
	private final List<ClientNetworkHandler> clients = new  CopyOnWriteArrayList<>();
	
	public void addClient(ClientNetworkHandler client) { clients.add(client); }
	public void removeClient(ClientNetworkHandler client) { clients.remove(client); }

	public void broadcastPlayers(ServerGameModel model) {
		List<PlayerState> players = model.getPlayers().stream()
				.map(p -> new PlayerState((byte)p.id(), p.pos().x(), p.pos().y(), p.angle()))
				.toList();

		for (ClientNetworkHandler client : clients) {
			client.send(new GameState((byte)client.getId(), players));
		}
	}
	public void broadcastWorld(ServerGameModel model) {
		for (ClientNetworkHandler client : clients) {
			byte[][] map = new byte[Globals.WIDTH_TILES][Globals.WIDTH_TILES];
			for (int c = 0; c < map.length; ++c) {
				for (int r = 0; r < map[0].length; ++r) {
					map[c][r] = model.map()[r][c].id();
				}
			}
			client.send(new WorldData(map));
		}
	}
	public void broadcastMsg(String msg) {
		System.out.println("Broadcasting msg: " + msg);
		ServerMsg m = new ServerMsg(msg);
		for (ClientNetworkHandler client : clients) {
			client.send(m);
		}
	}
	
	// notify only one player about the world, on join
	public void sendWorldToPlayer(int id, ServerGameModel model) {
		for (ClientNetworkHandler client : clients) {
			if (client.getId() == id) {
				byte[][] map = new byte[Globals.WIDTH_TILES][Globals.WIDTH_TILES];
				for (int c = 0; c < map.length; ++c) {
					for (int r = 0; r < map[0].length; ++r) {
						map[c][r] = model.map()[r][c].id();
					}
				}
				client.send(new WorldData(map));
				return;
			}
		}
	}
}
