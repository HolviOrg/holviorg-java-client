package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.holvi.client.BeaverGUI;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -5009018726039275476L;

	public AboutDialog(JFrame parent) {
		super(parent, "About Holvi.org Cloud Beaver", true);
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		URL imageURL = BeaverGUI.class.getResource("images/about.png");
		JLabel picLabel = new JLabel(new ImageIcon(imageURL));
		getContentPane().add(picLabel, BorderLayout.PAGE_START);
		JPanel panel = new JPanel(new BorderLayout());
		
		panel.setBackground(Color.WHITE);
		JPanel buttonPanel = new JPanel();
		JLabel copyright = new JLabel("© 2011-2012 SCIS Holvi.org Secure Cloud Infrastructure Services");
		copyright.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel authorInfo = new JLabel("Holvi.org Cloud Beaver (Fall 2012)");
		authorInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(Color.WHITE);
		textPanel.add(Box.createVerticalGlue());
		textPanel.add(authorInfo);
		textPanel.add(copyright);
		textPanel.add(Box.createVerticalGlue());
		panel.add(textPanel, BorderLayout.CENTER);
		buttonPanel.setBackground(Color.WHITE);
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		close.setPreferredSize(new Dimension(100, 25));
		buttonPanel.add(close);
		panel.setPreferredSize(new Dimension(picLabel.getWidth(), 100));
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		getContentPane().add(panel, BorderLayout.PAGE_END);
		setResizable(false);
		pack();
		setLocationByPlatform(true);
		textPanel.requestFocusInWindow();
	}
}
