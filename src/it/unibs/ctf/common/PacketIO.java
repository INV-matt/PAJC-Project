package it.unibs.ctf.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PacketIO {
	private PacketIO() {}
	private static final byte GAME_STATE = 1;  // server -> clients, state of players 
	private static final byte INPUT 		= 2;  // client -> server, input of player
	private static final byte WORLD		= 3;  // server -> client, world generation
	private static final byte SERVER_CMD = 16; // server -> client,  other operations
	
	
	public static GamePacket read(DataInputStream in) throws IOException {
		byte type =  in.readByte();
		
		switch (type) {
		case GAME_STATE: {
			byte playerId = in.readByte();
			byte length = in.readByte();
			List<PlayerState> players = new ArrayList<>();
			for (int i = 0; i < length; ++i) {
				byte id = in.readByte();
				float x = in.readFloat();
				float y = in.readFloat();
				float a = in.readFloat();
				players.add(new PlayerState(id, x, y, a));
			}
			return new GameState(playerId, List.copyOf(players));
		}
		case INPUT: {
			byte bits = in.readByte();
			boolean up 		= (bits & 1)  != 0;
			boolean down 	= (bits & 2)  != 0;
			boolean left	= (bits & 4)  != 0;
			boolean right	= (bits & 8)  != 0;
			boolean shoot	= (bits & 16) != 0;
			return new InputData(up, down, left, right, shoot);
		}
		case WORLD: {
			byte[][] map = new byte[Globals.WIDTH_TILES][Globals.HEIGHT_TILES];
			
			for (int c = 0; c < map.length; ++c) {
				for (int r = 0; r < map[0].length; ++r) {
					map[c][r] = in.readByte();
				}
			}
			
			return new WorldData(map);
		}
		case SERVER_CMD: {
			StringBuffer buf = new StringBuffer();
			char c;
			while ((c = in.readChar()) != '\0') buf.append(c);
			return new ServerMsg(buf.toString());
		}
		default: {
//			throw new IOException(String.format("Unknown Packet Type: %d", type));
		}
		}
		return null;
		
		
	}
	
	public static void write(DataOutputStream out, GamePacket packet) throws IOException {
		if (packet instanceof GameState gs) {
			out.writeByte(GAME_STATE);
			out.writeByte(gs.playerId());
			out.writeByte(gs.players().size());
			for (PlayerState p : gs.players()) {
				out.writeByte(p.id());
				out.writeFloat(p.x());
				out.writeFloat(p.y());
				out.writeFloat(p.angle());
			}
		
		} else if (packet instanceof InputData input) {
			out.writeByte(INPUT);
			int bits = 0;
			bits |= input.up() 		?  1 : 0;
			bits |= input.down() 	?  2 : 0;
			bits |= input.left() 	?  4 : 0;
			bits |= input.right()	?  8 : 0;
			bits |= input.shoot()	? 16 : 0;
			out.writeByte(bits);
			
		} else if (packet instanceof WorldData wd) {
			out.writeByte(WORLD);
			for (int c = 0; c < Globals.WIDTH_TILES; ++c) {
				for (int r = 0; r < Globals.HEIGHT_TILES; ++r) {
					out.writeByte(wd.map()[c][r]); // TODO: consider reducing from byte to 4bit
				}
			}
			
		} else if (packet instanceof ServerMsg msg) {
			out.writeByte(SERVER_CMD);
			out.writeChars(msg.msg() + '\0');
		} else { 
			throw new IOException(String.format("Unknown packet: %s", packet.getClass()));
		}
		out.flush();
	}
}
