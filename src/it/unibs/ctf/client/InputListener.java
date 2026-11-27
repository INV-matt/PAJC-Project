package it.unibs.ctf.client;

public interface InputListener {
	public void inputChanged(
		boolean up, boolean down, 
		boolean left, boolean right,
		boolean shoot
	);
}
