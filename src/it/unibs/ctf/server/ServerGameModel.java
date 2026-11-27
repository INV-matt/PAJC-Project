package it.unibs.ctf.server;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.InputData;
import it.unibs.ctf.engine.Vec2;
import it.unibs.ctf.engine.Vec2i;

public class ServerGameModel {
	private final Map<Integer, Player> players = new LinkedHashMap<>();
	
	private final WorldTile[][] map = new WorldTile[Globals.TILE_WIDTH][Globals.TILE_HEIGHT];
	
	public ServerGameModel() {
		initializeMap();
	}
	
	public WorldTile[][] map(){
		return map;
	}

	public List<Player> getPlayers() {
		return List.copyOf(players.values());
	}

	public void addPlayer(int id) {
		float startX = 20 * id + Globals.TILE_SIZE;
		float startY = 20 * id + Globals.TILE_SIZE;
		players.put(id, new Player(id, startX, startY));
	}

	public void removePlayer(int id) {
		players.remove(id);
	}

	public void applyInput(int id, InputData input) {
		Player p = players.get(id);
		if (p != null) { 
			p.setInput(input);
		}
	}

	public void update(float dt) {
		for (Player p: players.values()) {
			Vec2 pos = p.tryMove(dt);
			Vec2 safePos = pos.add(p.dir().mul(Player.MIN_DST_FROM_WALL));
			Vec2i tilePos = new Vec2i((int)(safePos.x() / Globals.TILE_SIZE), (int)(safePos.y() / Globals.TILE_SIZE));
			WorldTile tile = map[tilePos.x()][tilePos.y()];
//			System.out.printf("(%.1f %.1f) <-> (%d %d) - T:(%d %d)\n", pos.x(), pos.y(), tilePos.x(), tilePos.y(), tile.id(), tile.collisionLayer());
			if (! tile.collideWith(p.collMask())) {
				p.setPosition(pos);
			}
		}
	}
	
	private void initializeMap() {
		for (int c = 0; c < map.length; ++c){
			for (int r = 0; r < map[0].length; ++r){
				if (c == 0 || c == map.length - 1 || r == 0 || r == map[0].length - 1) map[c][r] = new WorldTile(1, 1, 1);
				else map[c][r] = new WorldTile(0, 0, 0);
			}
		}
	}

}
