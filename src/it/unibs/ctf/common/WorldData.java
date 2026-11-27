package it.unibs.ctf.common;

public class WorldData implements GamePacket {
//	private int width, -> uncomment to implement custom world sizes, in future
//	private int height,
	private byte[][] map;
	
	public WorldData(byte[][] m) {
		map = m.clone();
	}
	
	public byte[][] map() {
		return map; // consider adding clone()
	}
	
}

