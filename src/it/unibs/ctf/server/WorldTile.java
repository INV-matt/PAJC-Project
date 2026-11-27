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
//	public boolean collideWith(int collMask) {
//		int pow = 0;
//		int bit = 0;
//		while((bit = 1 << pow) < collMask) {
//			/*
//			 * mask: 001010 = 10
//			 * layr: 000110 =  6
//			 * bit:      ^ -> true 
//			 * bit scorre le posizioni, se combaciano, true
//			 * 
//			 */
//			if ((collMask & bit) == 1 && (collMask & bit) == (collisionLayer & bit)) return true;
//			++pow;
//		}
//		return false;
//	}
	
	public boolean collideWith(int mask) {
		// if they have ones in common, it means they collide. and is 0 iff it has all 0, ie they dont collide
		int and = mask & this.collisionLayer;
		System.out.printf("%d %d -> %d\n", mask, collisionLayer, and);
		return and > 0;
	}

	
	
	
}
