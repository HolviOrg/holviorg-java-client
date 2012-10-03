package org.holvi.client.dialog;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class HolviDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7365470393411056897L;
	protected JPanel panel = new JPanel();
	
	public HolviDialog() {
		super();
	}
	
	public HolviDialog(JFrame parent, boolean modal) {
		super(parent, modal);
	}
	public HolviDialog(JFrame parent) {
		super(parent);
	}
	
	public abstract int showDialog();
}
