package it.unibs.ctf.server;

import it.unibs.ctf.common.InputData;
import it.unibs.ctf.engine.Vec2;

public class Player {
	private int id;
	private float x;
	private float y;
	private float angle;
	private int collMask;
	private InputData input = new InputData(false, false, false, false, false);
	
	public static final float MIN_DST_FROM_WALL = 6f;
	public static final float SPEED = 20f; // m/s
	public static final float ANGULAR_VEL = (float)Math.toRadians(20); // rad/s
	
	public Player(int id, float x, float y) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.angle = 0;
		this.collMask = 1;
	}
	
	
	public InputData getInput() { return input; }
	public int id() { return id; }
	public Vec2 pos() { return new Vec2(x, y); }
	public float angle() { return angle; }
	public Vec2 dir() { return new Vec2((input.right() ? 1 : 0) - (input.left() ? 1 : 0), (input.down() ? 1 : 0) - (input.up() ? 1 : 0));}
	public int collMask() { return collMask; }

	public void setPosition(Vec2 pos) {
		this.x = pos.x(); this.y = pos.y();		
	}
	
	public void setAngle(float a) {
		this.angle = a;
		while (angle >= Math.TAU) this.angle -= Math.TAU;
		while (angle < 0) this.angle += Math.TAU;
	}

	public void setInput(InputData input) { this.input = input;	}

//	public void update(float dt) {
//		float xAxis = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
//		float yAxis = (input.down() ? 1 : 0) - (input.up() ? 1 : 0);
//		float dirLen = (float)Math.sqrt(xAxis * xAxis + yAxis * yAxis);
//		if (Math.abs(dirLen) < 0.001) dirLen = 1;
//		angle += xAxis * dt * ANGULAR_VEL;
//		
//		float dx = dt * Player.SPEED * xAxis / dirLen;
//		float dy = dt * Player.SPEED * yAxis / dirLen;
//		
//		x += dx;
//		y += dy;
//	}

	public Vec2 tryMove(float dt) {
		float xAxis = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
		float yAxis = (input.up() ? 1 : 0) - (input.down() ? 1 : 0);
		
		
		angle += xAxis * dt * ANGULAR_VEL;
		
		float dx = dt * yAxis * Player.SPEED * (float)Math.cos(angle);
		float dy = dt * yAxis * Player.SPEED * (float)Math.sin(angle);
		
		return new Vec2(x + dx, y + dy);
	}



}
