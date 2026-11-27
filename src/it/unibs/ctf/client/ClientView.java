package it.unibs.ctf.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.PlayerState;


public class ClientView extends JPanel implements GameModelListener {
	private static final long serialVersionUID = 1L;

	private int width, height;
	
	private transient volatile List<PlayerState> players = List.of();
	private transient volatile byte[][] map = new byte[Globals.TILE_WIDTH][Globals.TILE_HEIGHT];
	private volatile int localId = -1;
	
	private final Color COLORS[] = {
		new Color(0x000000), // BLACK
		new Color(0xFFFFFF), // WHITE
		new Color(0xFF0000), // RED
		new Color(0x800000), // DARK RED
		new Color(0x0000FF), // BLUE
		new Color(0x000080), // DARK BLUE
	};
	
	public ClientView(int w, int h) {
		width = Math.round(w * Globals.SCREEN_SCALE); 
		height = Math.round(h * Globals.SCREEN_SCALE);
		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		setBackground(COLORS[0]);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g_orig = (Graphics2D) g.create();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 8));
        g2.scale(Globals.SCREEN_SCALE, Globals.SCREEN_SCALE);		
		
        g2.setStroke(new BasicStroke(Globals.TILE_WIDTH / 4));
        g2.setColor(COLORS[1]);
        for (int c = 0; c < map.length; ++c) {
        	for (int r = 0; r < map[0].length; ++r) {
        		if (map[c][r] == 0) continue;
        		g2.drawRect(c * Globals.TILE_SIZE, r * Globals.TILE_SIZE, Globals.TILE_SIZE, Globals.TILE_SIZE);
        	}
        }
		
		for (PlayerState p : players) {
			int x = Math.round(p.x()), y = Math.round(p.y());
			g2.setColor(localId == p.id() ? COLORS[2] : COLORS[5]);
			g2.fillRect(x-4, y-4, 8, 8);
			g2.drawString(String.format("%d - %.1f", p.id(), p.angle()), x, y + 10);
		}
		g_orig.drawImage(image, 0, 0, getWidth(), getHeight(), COLORS[0], null);
		g_orig.dispose();
	}

	@Override
	public void modelChanged(ClientModel model) {
		localId = model.getLocalId();
		players = List.copyOf(model.getPlayers());
		repaint();
	}
	
	@Override
	public void mapChanged(ClientModel model) {
		map = model.getMap();
		System.out.println("Painted Map");
		repaint();
	}

}
