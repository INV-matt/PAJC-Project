package it.unibs.ctf.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unibs.ctf.common.GameState;
import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.PlayerState;
import it.unibs.ctf.common.WorldData;

public class ClientModel {
	private int localId = -1;
	private List<PlayerState> players = List.of();
	private byte[][] map = new byte[Globals.WIDTH_TILES][Globals.HEIGHT_TILES];
	
	private final List<GameModelListener> listeners = new CopyOnWriteArrayList<>(); // not needed, but future-proof
	public void addListener(GameModelListener l) { listeners.add(l); }
		
	public int getLocalId() { return localId; }
	public List<PlayerState> getPlayers(){ return players; } // players is immutable, no need to copy
	public byte[][] getMap(){ return map; }
	
	public void update(GameState gs) {
		localId = gs.playerId();
		players = gs.players();
		
		for (GameModelListener l : listeners) {
			l.modelChanged(this);
		}
	}
	
	public void updateMap(WorldData wd) {
		map = wd.map();
		for (GameModelListener l : listeners) {
			l.mapChanged(this);
		}
	}
}
