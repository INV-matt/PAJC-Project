package it.unibs.ctf.client;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import it.unibs.ctf.common.Globals;


public class CTFApp {


	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		String host = "localhost";
		int port = Globals.PORT;
		
		ClientModel model = new ClientModel();
//		ClientView2D view2d = new ClientView2D(80, 72);
		ClientView3D view = new ClientView3D(160, 144);
		ClientNetwork netw = new ClientNetwork();
		ClientController ctrl = new ClientController(netw, model, view);
		InputHandler input = new InputHandler(ctrl, view);

		
		model.addListener(view);
//		model.addListener(view2d);
		netw.setDisconnectHandler(() -> SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, "Disconnected");
			System.exit(0);
		}));
		
		try {
			netw.connect(host, port);
		} catch (IOException e) {
			System.err.println(String.format("Could not connect to %s:%d", host, port));
			System.exit(1);
		}
		
		EventQueue.invokeLater(() -> {
			JFrame main = new JFrame("MAIN");
			main.setLocationByPlatform(true);
			main.setLocationRelativeTo(null);
			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			main.getContentPane().add(view);
			main.requestFocusInWindow();
			main.pack();
			main.setResizable(false);
			main.setBounds(0, 0, view.getPreferredSize().width, view.getPreferredSize().height);
			main.setVisible(true);
			
//			JFrame sec = new JFrame("2d");
//			sec.setLocationByPlatform(true);
//			sec.setLocationRelativeTo(null);
//			sec.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			sec.getContentPane().add(view2d);
//			sec.pack();
//			sec.setBounds(400, 0, view2d.getPreferredSize().width, view2d.getPreferredSize().height);
//			sec.setVisible(true);

			
		});
	}
}
