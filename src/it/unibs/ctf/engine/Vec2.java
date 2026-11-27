package it.unibs.ctf.engine;

public record Vec2(float x, float y) {
	public Vec2 add(Vec2 a) { return new Vec2(x + a.x(), y + a.y()); }
	public Vec2 mul(float k) { return new Vec2(k * x, k * y);}
	public Vec2 mul(Vec2 a) { return new Vec2(x * a.x(), y * a.y()); }
}
