package it.unibs.ctf.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;

import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.PlayerState;

public class ClientView3D extends JPanel implements GameModelListener {
	private static final long serialVersionUID = 1L;

	private int width, height;
	
	private transient volatile byte[][] map = new byte[Globals.WIDTH_TILES][Globals.HEIGHT_TILES];
	private volatile int localId = -1;
	private transient volatile List<PlayerState> players = List.of();
	
//	private transient volatile double oldAngle = 0;
//	private transient volatile double dirX = 1, dirY = 0;
//	private transient volatile double planeX = 0, planeY = 1; // -> FOV = atan(planeY/dir.length) = 90deg


	
	public ClientView3D(int w, int h) {
		width = Math.round(w * Globals.SCREEN_SCALE); 
		height = Math.round(h * Globals.SCREEN_SCALE);
		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		setBackground(COLORS.BG);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(getFont().deriveFont(Font.PLAIN, 8));
		
		
		PlayerState localPlayer = null;
		for (PlayerState p: players) if (p.id() == localId) localPlayer = p;
		if (localPlayer != null) drawWalls(g2, localPlayer);
		
		
		for (PlayerState p: players) paintPlayer(g2, p);
		
		
		g2.dispose();
	}
	
	private void paintPlayer(Graphics2D g2, PlayerState p) {
		if (p.id() == localId) return;
	}
	
	private void drawWalls(Graphics2D g2, PlayerState p) {
		double pX = p.x() / Globals.TILE_SIZE, pY = p.y() / Globals.TILE_SIZE;
		double a = p.angle();
		double camPlaneLength = 0.66; //.66-> fov=66deg, 1-> fov=90deg
		double dirX = Math.cos(a), dirY = Math.sin(a);
		double planeX = -dirY * camPlaneLength, planeY = dirX * camPlaneLength;
		
		
		for (int x = 0; x < getWidth(); ++x) {
			double camX = 2. * x / getWidth() - 1;
			double rayDirX = dirX + planeX * camX;
			double rayDirY = dirY + planeY * camX;
			
			int mapX = (int)pX, mapY = (int)pY;
			double sideDstX, sideDstY;
			double deltaDstX = Math.abs(1/rayDirX); // should still work even if divide by 0 (IEEE754)
			double deltaDstY = Math.abs(1/rayDirY);
			double perpWallDst;
			int stepX, stepY;
			int hitVal = 0;
			int side = 0;
			
			if (rayDirX < 0) {
				stepX = -1; sideDstX = (pX - mapX) * deltaDstX;
			} else {
				stepX = 1;  sideDstX = (mapX + 1.0 - pX) * deltaDstX;
			}
			if (rayDirY < 0) {
				stepY = -1; sideDstY = (pY - mapY) * deltaDstY;
			} else {
				stepY = 1;  sideDstY = (mapY + 1.0 - pY) * deltaDstY;
			}
			
			// DDA
			while (hitVal == 0) {
				if (sideDstX < sideDstY) {
					sideDstX += deltaDstX;
					mapX += stepX;
					side = 0;
				} else {
					sideDstY += deltaDstY;
					mapY += stepY;
					side = 1;
				}
				hitVal = map[mapX][mapY];
			}
			
			if (side == 0) 	perpWallDst = sideDstX - deltaDstX;
			else 			perpWallDst = sideDstY - deltaDstY;
			
			
			// Draw Walls
			int lineHeight = (int)(getHeight() / perpWallDst);
			
			Color wallCol = switch(hitVal) {
			case 1 	-> side == 0 ? COLORS.PRI : COLORS.PRI_D;
			case 2 	-> side == 0 ? COLORS.SEC : COLORS.SEC_D;
			default	-> side == 0 ? COLORS.TER : COLORS.TER_D;
			};
			
			g2.setColor(wallCol);
			g2.fillRect(x, (getHeight() - lineHeight) / 2, 1, lineHeight);
		}
	}
	
	
	@Override
	public void modelChanged(ClientModel model) {
		localId = model.getLocalId();
		players = List.copyOf(model.getPlayers());
		
//		for (PlayerState p: players) {
//			if (p.id() == localId) {
//				double dtheta = p.angle() - oldAngle;
//				double dir = dirX;
//				dirX = dir * Math.cos(- dtheta) - dirY * Math.sin(- dtheta);
//				dirY = dir * Math.sin(-dtheta)  + dirY * Math.cos(-dtheta);
//				double plane = planeX;
//				planeX = plane * Math.cos(- dtheta) - planeY * Math.sin(- dtheta);
//				planeY = plane * Math.sin(-dtheta)  + planeY * Math.cos(-dtheta);
//				oldAngle = p.angle();
//			}
//		}
		
		repaint();
	}
	
	@Override
	public void mapChanged(ClientModel model) {
		map = model.getMap();
		repaint();
	}
}


class COLORS {
	private static final Color COLORS[] = {
			new Color(0x4b3d44),   /* bg0 */
			new Color(0xd2c9a5),   /* fg0 */
			new Color(0x574852),   /* bg1*/    
			new Color(0xd1b187),   /* fg1 */
			new Color(0x847875),   /* bg2 */
			new Color(0xab9b8e),   /* fg2 */
			new Color(0xae5d40),   /* red      - PRIMARY */
			new Color(0xc77b58),   /* orange   - PRIMARY_L */
			new Color(0x79444a),   /* maroon   - PRIMARY_D */
			new Color(0xba9158),   /* yellow   - SECONDARY */
			new Color(0xb3a555),   /*          - SECONDARY_L */
			new Color(0x927441),   /* ocra     - SECONDARY_D */
			new Color(0x8caba1),   /* lblue    - TERTIARY */   
			new Color(0x4b726e),   /* blue     - TERTIARY_D */
			new Color(0x77743b),   /* green    - CONTRAST_P */   
			new Color(0x4d4539),   /* brown    - CONTRAST_S */
	}; // https://lospec.com/palette-list/lost-century
	
	public static final Color BG 		    = COLORS[0];
	public static final Color FG 	 	    = COLORS[1];
	public static final Color BG_1 		    = COLORS[2];
	public static final Color FG_1 		    = COLORS[3];
	public static final Color BG_2 		    = COLORS[4];
	public static final Color FG_2 		    = COLORS[5];
	public static final Color PRI		    = COLORS[6];
	public static final Color PRI_L			= COLORS[7];
	public static final Color PRI_D			= COLORS[8];
	public static final Color SEC			= COLORS[9];
	public static final Color SEC_L			= COLORS[10];
	public static final Color SEC_D			= COLORS[11];
	public static final Color TER			= COLORS[12];
	public static final Color TER_D			= COLORS[13];
	public static final Color CONTRAST_P		= COLORS[14];
	public static final Color CONTRAST_S		= COLORS[15];

	
}
