package it.unibs.ctf.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import it.unibs.ctf.common.Globals;
import it.unibs.ctf.common.PlayerState;
import it.unibs.ctf.common.ServerMsg;

public class ClientView3D extends JPanel implements GameModelListener, ServerMsgReceiver {
	private static final long serialVersionUID = 1L;
	private static final float FOV = 90; // in deg
	
	private int width, height;
	
	private transient volatile byte[][] map = new byte[Globals.WIDTH_TILES][Globals.HEIGHT_TILES];
	private volatile int localId = -1;
	private transient volatile List<PlayerState> players = List.of();
	private transient volatile CameraData cam = new CameraData();
	
	private transient volatile BufferedImage frame;
	private transient volatile int pixels[];
	private transient volatile double zbuffer[];
	
	private transient volatile String latestServerMsg = "CIAO";
//	private JLabel lbl_serverMsg;

	
	public ClientView3D(int w, int h) {
		width = w;
		height = h;
		setPreferredSize(new Dimension(Math.round(width * Globals.SCREEN_SCALE), Math.round(height * Globals.SCREEN_SCALE)));
		setFocusable(true);
		setBackground(COLORS.BG);
		
		zbuffer = new double[width];

//		setLayout(null);
//		lbl_serverMsg = new JLabel();
//		lbl_serverMsg.setBounds(4, getHeight() - 32, 128, 32);
//		add(lbl_serverMsg);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR); // crunchy textures
		g2.setFont(getFont().deriveFont(Font.PLAIN, 8));
		
		initializePixelBuffer(width, height);
		Arrays.fill(pixels, 0, pixels.length / 2, COLORS.BG_2.getRGB());
		Arrays.fill(pixels, pixels.length / 2, pixels.length, COLORS.BG.getRGB());

		
		PlayerState localPlayer = null;
		for (PlayerState p: players) if (p.id() == localId) localPlayer = p;
		if (localPlayer != null) drawWalls(localPlayer);
		
		for (PlayerState p: players) paintOtherPlayer(g2, localPlayer, p);
		
		g2.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
		
		drawServerMsg(g2);
		
		g2.dispose();
	}
	
	private void paintOtherPlayer(Graphics2D g2, PlayerState localPlayer, PlayerState player) {
		if (player.id() == localId) return;
		
//		int w = getWidth(), h = getHeight(); // SCALED
		int w = width, h = height; // UNSCALED
		
		double rx = (player.x() - localPlayer.x()) / Globals.TILE_SIZE;
		double ry = (player.y() - localPlayer.y()) / Globals.TILE_SIZE;
		
		double det = cam.planeX() * cam.dirY() - cam.dirX() * cam.planeY();
		double tX = (cam.dirY() * rx - cam.dirX() * ry) / det;
		double tY = (-cam.planeY() * rx + cam.planeX() * ry) / det;

		if (tY < 0.1) return; // behind or too close
		
		int screenX = (int)((w / 2.) * (1 + tX / tY));
		int size = (int)(h / tY);
		if (size <= 0) return;
		
		int left = screenX - size / 2;
		int top = (h - size) / 2;
		
		int startX = Math.max(0,  left);
		int endX = Math.min(w, left + size);
		
		int startY = Math.max(0, top);
		int endY = Math.min(h, top + size);
				
		for (int x = startX; x < endX; ++x) {
			if (tY > zbuffer[x]) continue;
			
			for (int y = startY, i = startY * w + x; y < endY; ++y, i += w) {
				pixels[i] = COLORS.CONTRAST_P.getRGB();
			}
		}
		
	}
	
	private void drawWalls(/*Graphics2D g2,*/ PlayerState p) {
		double pX = p.x() / Globals.TILE_SIZE, pY = p.y() / Globals.TILE_SIZE;
		cam.set(p.angle(), 0.66);
		
//		int w = getWidth(), h = getHeight(); // SCALED
		int w = width, h = height; // UNSCALED
		
		
		for (int x = 0; x < w; ++x) {
			double camX = 2. * x / w - 1;
			double rayDirX = cam.dirX() + cam.planeX() * camX;
			double rayDirY = cam.dirY() + cam.planeY() * camX;
			
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
			
			zbuffer[x] = perpWallDst;
			
			// Draw Walls
			int lineHeight = 4 * (int)(h / perpWallDst);
			int top = (h - lineHeight) / 2;
			int start = Math.max(0, top);
			int end = Math.min(h, top + lineHeight);
			
			Color wallCol = switch(hitVal) {
			case 1 	-> side == 0 ? COLORS.PRI : COLORS.PRI_D;
			case 2 	-> side == 0 ? COLORS.SEC : COLORS.SEC_D;
			default	-> side == 0 ? COLORS.TER : COLORS.TER_D;
			};
			int rgb = wallCol.getRGB();
			
			for (int y = start, i = start * w + x; y < end; ++y, i += w) {
				pixels[i] = rgb;
			}
		}
	}
	
	private void drawServerMsg(Graphics2D g2) {
		int w = getWidth(), h = getHeight();
		Color c = Color.BLACK;
		g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
		g2.fillRect(4, h - 32, 50, 16);
		g2.setColor(COLORS.FG);
		g2.setStroke(new BasicStroke(2f));
		g2.drawChars(latestServerMsg.toCharArray(), 0, latestServerMsg.length(), 8, h - 20);
	}
	
	private void initializePixelBuffer(int width, int height) {
		if (frame != null && frame.getWidth() == width && frame.getHeight() == height) return;
		frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
		zbuffer = new double[width];
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
		repaint();
	}

	@Override
	public void receiveServerMsg(ServerMsg msg) {
		latestServerMsg = msg.msg();
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
