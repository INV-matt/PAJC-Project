package it.unibs.ctf.engine;

public class Vec2i {
	private int x;
	private int y;
	
	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2i(Vec2 v) {
		x = (int)v.x();
		y = (int)v.y();
	}
	
	public int x() { return x; }
	public int y() { return y; }
	
	public Vec2i add(Vec2i a) { return new Vec2i(x + a.x(), y + a.y()); }
	public Vec2i mul(int k)   { return new Vec2i(k * x, k * y);}
	public Vec2i mul(Vec2i a) { return new Vec2i(x * a.x(), y * a.y()); }
}
