package it.unibs.ctf.client;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseListener {
	private InputListener listener;
	
	private boolean up, down, left, right, shoot;
	
	public InputHandler(InputListener list, Component comp) {
		listener = list;
		comp.addKeyListener(this);
		comp.addMouseListener(this);
		comp.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				setKeys(false, false, false, false, false);
			}
		});
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setKeys(up, down, left, right, true);
	}
	
	@Override 
	public void mouseReleased(MouseEvent e) {
		setKeys(up, down, left, right, false);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W, KeyEvent.VK_UP	  -> setKeys(true, down, left, right, shoot);
		case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> setKeys(up,   true, left, right, shoot);
		case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> setKeys(up,   down, true, right, shoot);
		case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> setKeys(up,   down, left, true,  shoot);
		case KeyEvent.VK_SPACE 				  -> setKeys(up,   down, left, right, true );
		default -> {}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W, KeyEvent.VK_UP	  -> setKeys(false, down,  left,  right, shoot);
		case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> setKeys(up,    false, left,  right, shoot);
		case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> setKeys(up,    down,  false, right, shoot);
		case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> setKeys(up,    down,  left,  false, shoot);
		case KeyEvent.VK_SPACE 				  -> setKeys(up,    down,  left,  right, false);
		default -> {}
		}		
	}
	
	private void setKeys(boolean u, boolean d, boolean l, boolean r, boolean sh) {
		if (up == u && down == d && left == l && right == r && shoot == sh) return;
		up = u; down = d;
		left = l; right = r;
		shoot = sh;
		listener.inputChanged(u, d, l, r, sh);
	}

	
	// UNUSED EVENTS
	@Override public void keyTyped(KeyEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}
