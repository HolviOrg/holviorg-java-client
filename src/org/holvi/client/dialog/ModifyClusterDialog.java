package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ModifyClusterDialog extends HolviDialog {
	private static final long serialVersionUID = 5558089668105493820L;
	private JTextField name;
	private JLabel lblName;
	private boolean answer = false;
	
	public ModifyClusterDialog(JFrame parent, String clusterName) {
		super(parent, true);
		setTitle("Modify " +clusterName);
		setLocationRelativeTo(parent);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.insets = new Insets(0, 0, 5, 0);

		lblName = new JLabel("Name: ");
		cs.gridx = 0;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 1;
		cs.weightx = 0;
		panel.add(lblName, cs);

		name = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 2;
		cs.weightx = 1.0;
		name.setText(clusterName);
		panel.add(name, cs);
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		JButton generateButton = new JButton("Save");
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				answer = true;
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				answer = false;
				setVisible(false);
			}
		});
		JPanel bp = new JPanel();
		bp.add(generateButton);
		bp.add(cancelButton);
		getContentPane().add(panel, BorderLayout.NORTH);
		getContentPane().add(bp, BorderLayout.PAGE_END);
		
		setMinimumSize(new Dimension(320, 100));
		pack();
	}
	
	@Override
	public int showDialog() {
		setVisible(true);
		if (answer) {
			return JOptionPane.OK_OPTION;
		}
		return JOptionPane.CANCEL_OPTION;
	}
	
	public String getName() {
		return name.getText().trim();
	}
}

