package it.unibs.ctf.common;

public record InputData(
		boolean up,
		boolean down,
		boolean left,
		boolean right,
		boolean shoot
) implements GamePacket{}
