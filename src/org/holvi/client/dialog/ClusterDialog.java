package org.holvi.client.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.holvi.sci.container.Cluster;

/**
 * Dialog for adding {@link Cluster} elements. 
 *
 */
public class ClusterDialog extends HolviDialog implements ActionListener {
	private static final long serialVersionUID = -6882600158764110887L;
	private JTextField name;
	int returnValue = JOptionPane.CANCEL_OPTION;
	
	public ClusterDialog() {
		super();
		setTitle("Add a new cluster");
		JLabel lblName = new JLabel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;

		lblName.setText("Cluster name: ");
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;
		panel.add(lblName, cs);

		name = new JTextField(15);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		panel.add(name, cs);
		
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

	/**
	 * Sets dialog visible.
	 * @return <code>JOptionPane.OK_OPTION</code> on add action, else <code>JOptionPane.CANCEL_OPTION</code>
	 */
	@Override
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
		return this.name.getText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int action = Integer.parseInt(e.getActionCommand());
		returnValue = action;
		dispose();
	}

}
