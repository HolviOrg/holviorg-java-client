package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.holvi.client.BeaverGUI;
import org.holvi.client.HolviKeyManager;

public class KeyAdditionDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 4922683368605498164L;
	private static final int OPEN_KEY_WINDOW = 1;
	private static final int GENERATION_WINDOW = 2;
	private static final int ACTION_LOADKEY = 3;
	private static final int ACTION_GENERATE = 4;
	private static final int ACTION_CLOSE = 0;
	private static final int ACTION_BACK = 5;
	private KeyPanel dialog;
	private HolviKeyManager parent;
	private String defaultKeyDir;

	public KeyAdditionDialog(HolviKeyManager parent) {
		super();
		this.parent = parent;
		Preferences prefs = BeaverGUI.getPreferences();
		defaultKeyDir = prefs.get("org.holvi.keydir", System.getProperty("user.home") + System.getProperty("file.separator") + "holvi");
		setIconImage(new ImageIcon(BeaverGUI.iconUrl).getImage());
		setModal(true);
		createUI();
		setLocationByPlatform(true);
		setResizable(false);
	}

	public void createUI() {
		setTitle("Holvi.org Cloud Beaver - Key addition");
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.insets = new Insets(0, 0, 0, 0);
		cs.gridy = 0;

		JButton generateButton = new JButton("Generate a new key");
		generateButton.setActionCommand(Integer.toString(GENERATION_WINDOW));
		generateButton.addActionListener(this);
		generateButton.setIcon(new ImageIcon(BeaverGUI.class.getResource("images/key_add_48x48.png")));
		generateButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(generateButton, cs);

		JButton loadButton = new JButton("Load a key from file");
		loadButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		loadButton.setIcon(new ImageIcon(BeaverGUI.class.getResource("images/folder_document_48x48.png")));
		loadButton.setActionCommand(Integer.toString(OPEN_KEY_WINDOW));
		loadButton.addActionListener(this);
		cs.gridy = 1;
		cs.insets = new Insets(10, 0, 0, 0);
		panel.add(loadButton, cs);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(panel);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		byte[] iv = new byte[0];
		byte[] key = new byte[0];
		HashMap<String, byte[]> keyMap;
		boolean writeAllowed;
		switch (Integer.parseInt(e.getActionCommand())) {
		case GENERATION_WINDOW:
			dialog = new KeyPanel(this, true);
			setContentPane(dialog);
			pack();
			repaint();
			break;
		case OPEN_KEY_WINDOW:
			dialog = new KeyPanel(this, false);
			setContentPane(dialog);
			pack();
			repaint();
			break;
		case ACTION_GENERATE:
			writeAllowed = true;
			File generatedKeyLocation = new File(dialog.getPath());
			if (generatedKeyLocation.isDirectory()) {
				JOptionPane.showMessageDialog(this, "Selected destination is a folder.\nPlease select destination file", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			if (generatedKeyLocation.exists()) {
				if (generatedKeyLocation.getAbsoluteFile().equals(new File(defaultKeyDir, "keylist.xml"))) {
					JOptionPane.showMessageDialog(this, "Selected file cannot be replaced.\nPlease select another file.", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
					break;
				}
				writeAllowed = false;
				Object[] options = new Object[]{"Replace", "Cancel"};
				int result = JOptionPane.showOptionDialog(this, 
						"File already exists, would you like to replace it?", "File already exists", 
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				switch (result) {
				case 0:
					writeAllowed = true;
					break;
				case 1:
					writeAllowed = false;
					break;
				default:
					writeAllowed = false;
					break;
				}
			}
			if (writeAllowed) {
				parent.generateKey(dialog.getPath(), dialog.getDescription(), dialog.getKeyLength());
				dispose();
			}
			break;
		case ACTION_LOADKEY:
			File loadKeyLocation = new File(dialog.getPath());
			if (loadKeyLocation.isDirectory()) {
				JOptionPane.showMessageDialog(this, "Selection is a folder.\nPlease select another file", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			try {
				keyMap = BeaverGUI.loadKeyFromFile(loadKeyLocation.getAbsolutePath());
				iv = keyMap.get("iv");
				key = keyMap.get("key");
				int keyLength = 0;
				switch (key.length) {
				case 16:
					keyLength = 128;
					break;
				case 24:
					keyLength = 192;
					break;
				case 32:
					keyLength = 256;
					break;
				}
				if (dialog.allowCopy()) {
					File oldFile = new File(dialog.getPath());
					File keyFile = new File(defaultKeyDir, oldFile.getName());
					writeAllowed = false;
					if (keyFile.exists()) {
						if (keyFile.getAbsoluteFile().equals(new File(defaultKeyDir, "keylist.xml"))) {
							JOptionPane.showMessageDialog(this, "This file cannot be copied due to invalid name.", "Invalid selection", JOptionPane.INFORMATION_MESSAGE);
							break;
						}
						int response = JOptionPane.showConfirmDialog(this, "Would you like to replace the file?", "File already exists", 
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.YES_OPTION) {
							writeAllowed = true;
						}
					} else {
						writeAllowed = true;
					}
					if (writeAllowed) {
						FileOutputStream fo = null;
						try {
							fo = new FileOutputStream(keyFile);
							fo.write(iv);
							fo.write(key);
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} finally {
							if (fo != null) {
								try {
									fo.close();
								} catch (IOException e1) {
								}
							}

						}
						parent.addKey(iv, key, keyFile.getAbsolutePath(), dialog.getDescription(), keyLength);
					}
				}
				else {
					parent.addKey(iv, key, dialog.getPath(), dialog.getDescription(), keyLength);
				}
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			dispose();
			break;
		case ACTION_CLOSE:
			dispose();
			break;
		case ACTION_BACK:
			createUI();
			break;
		}

	}


	class KeyPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 7251806453536160081L;
		private JTextField tfLocation;
		private JTextField tfDescription;
		private JCheckBox copyFile;
		private ButtonGroup group;
		private int keyLength = 0;
		private final int ACTION_OPEN_FILE = 0;
		private final int ACTION_SAVE_FILE = 1;
		private final int ENC_AES_128 = 3;
		private final int ENC_AES_192 = 4;
		private final int ENC_AES_256 = 5;

		public KeyPanel(KeyAdditionDialog keyAdditionDialog, boolean isNew) {
			super();
			setLayout(new BorderLayout());
			if (isNew) {
				setTitle("Holvi.org Cloud Beaver - Key generation");
			} else {
				setTitle("Holvi.org Cloud Beaver - Load existing key");
			}
			JPanel panel = new JPanel(new GridBagLayout());
			JLabel lblFileLocation = new JLabel("File: ");
			JLabel lblDescription = new JLabel("Description: ");

			tfLocation = new JTextField(20);
			if (isNew) {
				int i = 1;
				File file = null;
				while (true) {
					file = new File(defaultKeyDir, "holvikey" + i + ".key");
					if (!file.exists()) {
						break;
					}
					i++;
				}
				tfLocation.setText(file.getAbsolutePath());
			}
			tfDescription = new JTextField(20);

			GridBagConstraints cs = new GridBagConstraints();
			cs.fill = GridBagConstraints.HORIZONTAL;

			cs.insets = new Insets(0, 0, 5, 0);
			cs.gridx = 0;
			cs.gridy = 0;
			cs.gridwidth = 1;
			panel.add(lblFileLocation, cs);

			cs.gridx = 1;
			cs.gridy = 0;
			cs.gridwidth = 1;
			panel.add(tfLocation, cs);

			JButton button = new JButton("Select file");
			if (isNew) {
				button.setActionCommand(Integer.toString(ACTION_SAVE_FILE));
			} else {
				button.setActionCommand(Integer.toString(ACTION_OPEN_FILE));
			}
			button.addActionListener(this);
			cs.gridx = 2;
			panel.add(button, cs);

			cs.gridx = 0;
			cs.gridy = 1;
			cs.gridwidth = 1;
			panel.add(lblDescription, cs);

			cs.gridx = 1;
			cs.gridy = 1;
			cs.gridwidth = 2;
			panel.add(tfDescription, cs);

			if (isNew) {
				cs.gridx = 0;
				cs.gridy = 2;
				cs.gridwidth = 1;
				JLabel lblExtra = new JLabel("Mode: ");
				panel.add(lblExtra, cs);

				JPanel radioPanel = new JPanel(new GridLayout(0, 1));

				JRadioButton aes128 = new JRadioButton("AES128");
				aes128.setActionCommand(Integer.toString(ENC_AES_128));
				aes128.addActionListener(this);
				JRadioButton aes192 = new JRadioButton("AES192");
				aes192.setActionCommand(Integer.toString(ENC_AES_192));
				aes192.addActionListener(this);

				JRadioButton aes256 = new JRadioButton("AES256");
				aes256.setActionCommand(Integer.toString(ENC_AES_256));
				aes256.addActionListener(this);
				aes256.doClick();
				group = new ButtonGroup();
				group.add(aes128);
				group.add(aes192);
				group.add(aes256);
				radioPanel.add(aes128);
				radioPanel.add(aes192);
				radioPanel.add(aes256);

				cs.gridx = 1;
				cs.gridwidth = 2;
				panel.add(radioPanel, cs);
			} else {
				copyFile = new JCheckBox("Copy file to the default key directory");
				copyFile.setSelected(true);
				cs.gridx = 1;
				cs.gridy = 2;
				cs.gridwidth = 1;
				panel.add(copyFile, cs);
			}

			panel.setBorder(new EmptyBorder(5, 5, 5, 5));
			add(panel, BorderLayout.CENTER);

			JButton backButton = new JButton("Back");
			backButton.setActionCommand(Integer.toString(ACTION_BACK));
			backButton.addActionListener(keyAdditionDialog);
			JButton addButton;
			if (isNew){
				addButton = new JButton("Generate");
				addButton.setActionCommand(Integer.toString(ACTION_GENERATE));
			} else {
				addButton = new JButton("Load");
				addButton.setActionCommand(Integer.toString(ACTION_LOADKEY));
			}
			addButton.addActionListener(keyAdditionDialog);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand(Integer.toString(ACTION_CLOSE));
			cancelButton.addActionListener(keyAdditionDialog);
			JPanel bp = new JPanel();
			bp.add(backButton, BorderLayout.WEST);
			bp.add(addButton, BorderLayout.EAST);
			bp.add(cancelButton);
			add(bp, BorderLayout.SOUTH);
		}

		public String getPath() {
			return tfLocation.getText();
		}

		public String getDescription() {
			return tfDescription.getText();
		}

		public int getKeyLength() {
			return this.keyLength;
		}

		public boolean allowCopy() {
			return copyFile.isSelected();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int cmd = Integer.parseInt(e.getActionCommand());
			int returnVal;
			JFileChooser fc;
			switch (cmd) {
			case ACTION_OPEN_FILE:
				fc = new JFileChooser(defaultKeyDir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					tfLocation.setText(file.getAbsolutePath());
				}
				break;
			case ACTION_SAVE_FILE:
				fc = new JFileChooser(defaultKeyDir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					tfLocation.setText(file.getAbsolutePath());
				}
				break;
			case ENC_AES_128:
				this.keyLength = 128;
				break;
			case ENC_AES_192:
				this.keyLength = 192;
				break;
			case ENC_AES_256:
				this.keyLength = 256;
				break;
			default:
				break;
			}
		}
	}




}