package it.unibs.ctf.client;

public class CameraData {
	private double dirX = 1;
	private double dirY = 0;
	private double planeX = 0;
	private double planeY = 1;
	
	public void set(double angle, double planeLength) {
		double camPlaneLength = 0.66; //.66-> fov=66deg, 1-> fov=90deg
		dirX = Math.cos(angle);
		dirY = Math.sin(angle);
		planeX = -dirY * camPlaneLength;
		planeY = dirX * camPlaneLength;
	}

	public double dirX() { return dirX; }
	public double dirY() { return dirY; }
	public double planeX() { return planeX; }
	public double planeY() { return planeY; }
	
}
