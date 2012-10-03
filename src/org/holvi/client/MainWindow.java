package org.holvi.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.net.ssl.KeyManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.holvi.client.dialog.AboutDialog;
import org.holvi.sci.HolviClient;
import org.holvi.sci.HolviClient.ENC;

/**
 * Main Window for Holvi.org Cloud Beaver. 
 *
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = -6463168887784521424L;
	private BeaverGUI gui;
	private JLabel encryptionLabel;
	private ImageIcon encryptionON = new ImageIcon(BeaverGUI.class.getResource("icons/lock_ok_16x16.png"));
	private ImageIcon encryptionOFF = new ImageIcon(BeaverGUI.class.getResource("icons/lock_error_16x16.png"));
	private JButton uploadButton;
	private JMenuItem uploadMenuItem;
	private JCheckBoxMenuItem cbMenuItem;

	public MainWindow(BeaverGUI gui) {
		super("Holvi.org Cloud Beaver - Vault Directory");
		this.gui = gui;
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(500, 450));
		pack();
		setLocationByPlatform(true);
	}

	/**
	 * Populates a panel with a button to {@link KeyManager} and label displaying encryption status.
	 * @param statusPanel panel to be populated
	 */
	public void populateStatusPanel(JPanel statusPanel) {
		JButton button;
		statusPanel.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		button = makeButton("key_16x16.png", gui.ACTION_MANAGE_KEYS, "Select encryption key");
		button.setPreferredSize(new Dimension(24, 24));
		buttonPanel.add(button, BorderLayout.WEST);
		statusPanel.add(buttonPanel, BorderLayout.WEST);
		this.encryptionLabel = new JLabel();
		this.encryptionLabel.setText("ENCRYPTION DISABLED");
		this.encryptionLabel.setIcon(encryptionOFF);
		statusPanel.add(encryptionLabel);
	}
	
	/**
	 * Updates {@link #encryptionLabel} to show current encryption status.
	 * @param keyhash hash of the current encryption key
	 * 
	 */
	public void updateEncryptionLabel(String keyhash) {
		if (gui.getClient().getEncryptionMode() == HolviClient.ENC.NONE) {
			encryptionLabel.setText("ENCRYPTION DISABLED");
			encryptionLabel.setIcon(encryptionOFF);
		} else {
			for (HolviXMLKeyItem key: gui.getKeyList()) {
				if (key.getKeyHash().equals(keyhash) && key.isAvailable()) {
					File f = new File(key.getPath());
					encryptionLabel.setText(f.getName() + " (" + key.getDescription() +")");
					encryptionLabel.setIcon(encryptionON);
				}
			}
		}
		encryptionLabel.invalidate();
		encryptionLabel.repaint();
	}
	
	/**
	 * Updates {@link #cbMenuItem} with current encryption status.
	 * @param status current status. <code>true</code> when encryption is disabled, <code>false</code> when enabled.
	 * 
	 */
	public void updateEncryptionCheckbox(boolean status) {
		cbMenuItem.setSelected(status);
	}
	
	/**
	 * Populates {@link JToolBar} with buttons.
	 * @param pToolBar 
	 */
	public void populateToolbar(JToolBar pToolBar) {
		JButton button = null;
		
		button = makeButton("house_24x24.png", gui.ACTION_HOME, "Return to top level");
		pToolBar.add(button);

		button = makeButton("folder_up_24x24.png", gui.ACTION_PREVIOUS, "Go back one level");
		pToolBar.add(button);

		button = makeButton("refresh_24x24.png", gui.ACTION_REFRESH, "Refresh hierarchy");
		pToolBar.add(button);

		pToolBar.addSeparator();

		button = makeButton("folder_add_24x24.png", gui.ACTION_ADD_CONTAINER, "Add folder");
		pToolBar.add(button);

		this.uploadButton = makeButton("document_add_24x24.png", gui.ACTION_ADD_ITEM, "Upload file");
		this.uploadButton.setEnabled(false);
		pToolBar.add(this.uploadButton);

		pToolBar.addSeparator();

		button = makeButton("floppy_disk_24x24.png", gui.ACTION_SAVE_ITEM, "Download selected file(s)");
		pToolBar.add(button);

		button = makeButton("floppy_disks_24x24.png", gui.ACTION_SAVE_ALL, "Download all files");
		pToolBar.add(button);

		pToolBar.addSeparator();
		
		button = makeButton("preferences_edit_24x24.png", gui.ACTION_MODIFY, "Modify");
		pToolBar.add(button);
		
		button = makeButton("information_24x24.png", gui.ACTION_INFORMATION, "Show information");
		pToolBar.add(button);
		
		button = makeButton("delete_24x24.png", gui.ACTION_DELETE, "Delete selected item(s)");
		pToolBar.add(button);
	}
	
	/**
	 * Returns {@link URL} of an icon by it's name.
	 * @param imageName name of the icon file.
	 * @return {@link URL} pointing to the icon resource.
	 */
	private URL getIconUrl(String imageName) {
		return BeaverGUI.class.getResource("icons/"+imageName);
	}
	
	/**
	 * Creates new {@link JButton}.
	 * @param imageName name of the icon displayed in this {@link JButton}
	 * @param action action to be bound to this {@link JButton}
	 * @param tooltipText tool tip for this {@link JButton}
	 * @return new {@link JButton}
	 */
	public JButton makeButton(String imageName, int action, String tooltipText) {
		URL imageURL = BeaverGUI.class.getResource("icons/"+imageName);
		JButton button = new JButton();
		button.setActionCommand(Integer.toString(action));
		button.setToolTipText(tooltipText);
		button.setIcon(new ImageIcon(imageURL));
		button.setBorderPainted(false);
		button.addActionListener(gui);
		return button;
	}

	/**
	 * Populates {@link JMenuBar} with items.
	 * @param menuBar
	 */
	public void populateMenubar(JMenuBar menuBar) {
		JMenuItem menuItem;
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		this.uploadMenuItem = new JMenuItem("New file");
		this.uploadMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_D, ActionEvent.ALT_MASK));
		this.uploadMenuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_ITEM));
		this.uploadMenuItem.addActionListener(gui);
		this.uploadMenuItem.setEnabled(false);
		this.uploadMenuItem.setIcon(new ImageIcon(getIconUrl("document_add_16x16.png")));
		fileMenu.add(this.uploadMenuItem);
		menuItem = new JMenuItem("New folder");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_C, ActionEvent.ALT_MASK));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_ADD_CONTAINER));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(getIconUrl("folder_add_16x16.png")));
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Download selected file(s)");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_SAVE_ITEM));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(getIconUrl("floppy_disk_16x16.png")));
		fileMenu.add(menuItem);
		menuItem = new JMenuItem("Download all files");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_SAVE_ALL));
		menuItem.addActionListener(gui);
		menuItem.setIcon(new ImageIcon(getIconUrl("floppy_disks_16x16.png")));
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Exit");
		menuItem.setIcon(new ImageIcon(getIconUrl("exit_16x16.png")));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(menuItem);
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		this.cbMenuItem = new JCheckBoxMenuItem("Disable encryption", false);
		this.cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (cbMenuItem.isSelected()) {
					gui.getClient().setEncryptionMode(ENC.NONE);
					updateEncryptionLabel("");
				} else {
					gui.getClient().setEncryptionMode(ENC.AES_256);
					updateEncryptionLabel(BeaverGUI.getPreferences().get("selected.key", ""));
				}
			}
		});
		settingsMenu.add(cbMenuItem);
		
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		menuItem = new JMenuItem("Key manager");
		menuItem.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/key_16x16.png")));
		menuItem.setActionCommand(Integer.toString(gui.ACTION_MANAGE_KEYS));
		menuItem.addActionListener(gui);
		toolsMenu.add(menuItem);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuItem = new JMenuItem("About");
		menuItem.setIcon(new ImageIcon(getIconUrl("information_16x16.png")));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog dialog = new AboutDialog((JFrame)getParent());
				dialog.setVisible(true);
			}
		});
		helpMenu.add(menuItem);
		menuBar.add(fileMenu);
		menuBar.add(settingsMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
	}

	/**
	 * Sets state of {@link #uploadButton} and {@link #uploadMenuItem}.
	 * @param allow <code>true</code> if uploading is allowed, otherwise <code>false</code> 
	 */
	public void allowUpload(boolean allow) {
		uploadButton.setEnabled(allow);
		uploadMenuItem.setEnabled(allow);
	}
}

