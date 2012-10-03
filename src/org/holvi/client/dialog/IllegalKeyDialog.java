package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class IllegalKeyDialog extends JDialog {
	private static final long serialVersionUID = -8382022158097193355L;

	public IllegalKeyDialog() {
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(new JLabel("Using strong encryption ciphers (AES192 and AES256) requires installation of Java Cryptography Extension Unlimited Strength patch."), BorderLayout.PAGE_START);
		JLabel label = new JLabel("Please follow Oracle Java instructions and download links at");
		JLabel lblLink = new JLabel("<html><u>http://www.oracle.com/technetwork/java/javase/downloads/index.html</u><html>");
		lblLink.addMouseListener(new MouseListener() {
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
					desktop.browse(new URI("http://www.oracle.com/technetwork/java/javase/downloads/index.html"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		textPanel.add(label, BorderLayout.WEST);
		textPanel.add(lblLink, BorderLayout.EAST);
		textPanel.add(new JLabel("AES128 can be used with the standard international Java runtime environment."), BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(this, textPanel, "Unsupported key strength", JOptionPane.INFORMATION_MESSAGE);
	}
}
