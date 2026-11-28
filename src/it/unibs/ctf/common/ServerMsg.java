package it.unibs.ctf.common;

public record ServerMsg(
		// byte code, // for errors,death msg etc...
		String msg
) implements GamePacket {}
