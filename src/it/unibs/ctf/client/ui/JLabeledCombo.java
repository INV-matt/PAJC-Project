package it.unibs.ctf.client.ui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JLabeledCombo<T> extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JComboBox<T> combo;
	private List<T> objects;
	
	public JLabeledCombo(List<T> objects) {
		setLayout(new FlowLayout());
		this.objects = new ArrayList<T>(objects); 
		this.label = new JLabel(objects.get(0).toString());
		
		add(label);
		add(combo);
		
		combo.addActionListener(e -> {
			label.setText(combo.getSelectedItem().toString());
		});
	}

}
