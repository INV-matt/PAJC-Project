package it.unibs.ctf.client;

import it.unibs.ctf.common.ServerMsg;

public interface ServerMsgReceiver {
	public void receiveServerMsg(ServerMsg msg);
}
