package org.holvi.client.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.holvi.client.BeaverGUI;

public class VaultDialog extends HolviDialog implements ActionListener {
	private static final long serialVersionUID = -6852338647941744668L;
	JTextField name;
	JComboBox<String> type;
	int returnValue = JOptionPane.CANCEL_OPTION;
	public VaultDialog(BeaverGUI gui) {
		super();
		setTitle("Add a new vault");
		String[] allowedTypes = gui.getClient().getAllowedTypes();
		JLabel lblName;
		JLabel lblType;
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		lblName = new JLabel("Name: ");
		cs.insets = new Insets(0, 0, 5, 0);
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(lblName, cs);

		name = new JTextField(15);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		name.requestFocusInWindow();
		panel.add(name, cs);

		lblType = new JLabel("Type: ");
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		panel.add(lblType, cs);

		type = new JComboBox<String>(allowedTypes);
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(type, cs);
		
		JButton addButton;
		addButton = new JButton("Add");
		addButton.setActionCommand(Integer.toString(JOptionPane.OK_OPTION));
		addButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(Integer.toString(JOptionPane.CANCEL_OPTION));
		cancelButton.addActionListener(this);
		JPanel bp = new JPanel();
		bp.add(addButton);
		bp.add(cancelButton);
		
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 2;
		panel.add(bp, cs);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setDefaultCloseOperation(JOptionPane.CANCEL_OPTION);
	}
	
	public String getVaultType() {
		return type.getSelectedItem().toString();
	}
	
	public int showDialog() {
		name.grabFocus();
		setResizable(false);
		setModal(true);
		setContentPane(panel);
		pack();
		setLocationByPlatform(true);
		setVisible(true);
		return returnValue;
	}

	public String getClusterName() {
		return name.getText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int action = Integer.parseInt(e.getActionCommand());
		returnValue = action;
		dispose();
	}
}
