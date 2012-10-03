package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.holvi.client.BeaverGUI;
import org.holvi.sci.exception.HolviException;

public class LoginDialog extends JFrame {

	private static final long serialVersionUID = 6541434171800705107L;
	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private JCheckBox cbRemember;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JLabel lblRegister;
	private JButton btnLogin;
	private boolean succeeded;

	public LoginDialog(JFrame parent, final BeaverGUI guiClient) {
		super("Holvi.org Cloud Beaver - Login");
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		URL imageURL = BeaverGUI.class.getResource("images/login.png");
		JLabel picLabel = new JLabel(new ImageIcon(imageURL));
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		Preferences prefs = BeaverGUI.getPreferences();
		boolean rememberUsername = prefs.getBoolean("holvi.remember.username", false);
		String username = prefs.get("holvi.username", "");
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.insets = new Insets(15, 0, 0, 0);
		lblUsername = new JLabel("Username: ");
		cs.gridx = 0;
		cs.gridy = 0;
		cs.gridwidth = 1;

		panel.add(lblUsername, cs);

		tfUsername = new JTextField();
		tfUsername.setText(username);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridwidth = 2;
		panel.add(tfUsername, cs);

		lblPassword = new JLabel("Password: ");
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		cs.insets = new Insets(10, 0, 0, 0);
		panel.add(lblPassword, cs);

		tfPassword = new JPasswordField();
		cs.gridx = 1;
		cs.gridy = 1;
		cs.gridwidth = 2;
		panel.add(tfPassword, cs);

		cbRemember = new JCheckBox("Remember me");
		cbRemember.setSelected(rememberUsername);
		cbRemember.setBackground(Color.WHITE);
		cs.gridx = 1;
		cs.gridy = 2;
		cs.gridwidth = 1;
		panel.add(cbRemember, cs);

		btnLogin = new JButton("Login");
		btnLogin.setPreferredSize(new Dimension(150, 25));
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread worker = new Thread() {
					public void run() {
						try {
							guiClient.authenticate(getUsername(), getPassword(), getRememberUsername());
							dispose();
						} catch (HolviException exc) {
							JOptionPane.showMessageDialog(LoginDialog.this,
									exc.getMessage(),
									"Login",
									JOptionPane.ERROR_MESSAGE);
							tfPassword.setText("");
							succeeded = false;
						} 
					}
				};
				worker.start();
			}
		});
		cs.gridx = 1;
		cs.gridy = 3;
		cs.gridwidth = 1;
		cs.insets = new Insets(8, 0, 0, 0);
		panel.add(btnLogin, cs);

		lblRegister = new JLabel("<html><u>Register new account</u><html>");
		lblRegister.setForeground(Color.BLUE);
		lblRegister.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("https://my.holvi.org?lang=en"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		cs.gridx = 1;
		cs.gridy = 4;
		cs.gridwidth = 2;
		cs.insets = new Insets(7, 0, 15, 60);
		panel.add(lblRegister, cs);

		getContentPane().add(picLabel, BorderLayout.PAGE_START );
		getContentPane().add(panel, BorderLayout.CENTER);
		setResizable(false);
		pack();
		if (!tfUsername.getText().equals("")) {
			tfPassword.requestFocusInWindow();
		}
		setLocationByPlatform(true);
	}

	public String getUsername() {
		return tfUsername.getText().trim();
	}

	public String getPassword() {
		return new String(tfPassword.getPassword());
	}

	public boolean getRememberUsername() {
		return cbRemember.isSelected();
	}
	public boolean isSucceeded() {
		return succeeded;
	}
}