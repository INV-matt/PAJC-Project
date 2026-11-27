package it.unibs.ctf.common;

import java.util.List;

public record GameState (
		byte playerId,
		List<PlayerState> players 
) implements GamePacket{}
