package it.unibs.ctf.server;

public class WorldTile {
	
	private byte id;
	private int collisionLayer; // resides on this coll layer. a power of 2
	private int collisionMask; // checks for coll over this layers (bit mask)
	
	public WorldTile() { this((byte)0,0,0); }
	public WorldTile(byte id) { this(id, 0, 0); }
	public WorldTile(byte id, int collLayer, int collMask) {
		this.id = id;
		this.collisionLayer = collLayer;
		this.collisionMask = collMask;
	}
	public WorldTile(int id) { this(id, 0, 0); }
	public WorldTile(int id, int collLayer, int collMask) {
		this.id = (byte)(id % 256);
		this.collisionLayer = collLayer;
		this.collisionMask = collMask;
	}
	
	public byte id() { return id; }
	public int collisionLayer() { return collisionLayer; }

	
	public boolean collideWith(int mask) {
		// if they have ones in common, it means they collide. It is 0 iff the bitwise and has all 0, ie iff they dont collide
		return (mask & this.collisionLayer) > 0;
	}

	
	
	
}
