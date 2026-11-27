package it.unibs.ctf.common;

public class Globals {
	private Globals(){}
	
	public static final int TILE_SIZE = 8;
	public static final int TILE_WIDTH = 8;
	public static final int TILE_HEIGHT = 8;
	
	public static final int WORLD_WIDTH = TILE_SIZE * TILE_WIDTH;
	public static final int WORLD_HEIGHT = TILE_SIZE * TILE_HEIGHT;
	
	public static final float SCREEN_SCALE = 3f;

	public static final float SENSITIVITY = 0.01f;
	
	public static final int PORT = 1234;
	public static final int TPS = 30; 
	public static final int MAX_PLAYERS = 8;
	
	

}
