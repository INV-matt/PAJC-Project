package it.unibs.ctf.client;

import javax.swing.SwingUtilities;

import it.unibs.ctf.common.InputData;

public class ClientController implements InputListener {
	private final ClientNetwork net;
	
	public ClientController(ClientNetwork net, ClientModel model) {
		this.net = net;
		net.setStateHandler(s -> SwingUtilities.invokeLater(() -> {
			model.update(s); 
		}));
		net.setWorldHandler(w -> SwingUtilities.invokeLater(() -> {
			model.updateMap(w);
		}));
	}

	@Override
	public void inputChanged(boolean up, boolean down, boolean left, boolean right, boolean shoot) {
		net.send(new InputData(up, down, left, right, shoot));
	}
}
