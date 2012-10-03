package org.holvi.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.holvi.client.dialog.ClusterDialog;
import org.holvi.client.dialog.HolviDialog;
import org.holvi.client.dialog.LoginDialog;
import org.holvi.client.dialog.ModifyClusterDialog;
import org.holvi.client.dialog.ModifyDataItemDialog;
import org.holvi.client.dialog.VaultDialog;
import org.holvi.client.tree.ClusterHierarchyModel;
import org.holvi.client.tree.ClusterNode;
import org.holvi.client.tree.ClusterTree;
import org.holvi.client.tree.ClusterTreeNode;
import org.holvi.client.tree.DataItemNode;
import org.holvi.sci.DataItem;
import org.holvi.sci.HolviClient;
import org.holvi.sci.HolviClient.ENC;
import org.holvi.sci.container.Cluster;
import org.holvi.sci.container.Vault;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;

/**
 * Holvi.org Cloud Beaver main class. <p>
 * Contains all the control logic for the Holvi.org Cloud Beaver client.
 * 
 * @author Holvi.org / SCIS Holvi.org Secure Cloud Infrastructure Services
 */
public class BeaverGUI implements Runnable, ActionListener {
	public final int ACTION_HOME = 0;
	public final int ACTION_PREVIOUS = 1;	
	public final int ACTION_REFRESH = 2;
	public final int ACTION_ADD_CONTAINER = 3;
	public final int ACTION_ADD_ITEM = 4;
	public final int ACTION_SAVE_ITEM = 5;
	public final int ACTION_SAVE_ALL = 6;
	public final int ACTION_DELETE = 7;
	public final int ACTION_MANAGE_KEYS = 8;
	public final int ACTION_MODIFY = 9;
	public final int ACTION_INFORMATION = 10;
	
	private Vault[] vaults = new Vault[0];
	private HolviClient client;
	private String configNodeName = "/org/holvi/client";
	private File keyFileXML;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private MainWindow mainWindow;
	private ClusterHierarchyModel treeModel;
	private ClusterTree treeView;
	
	private static ArrayList<HolviXMLKeyItem> keyList = new ArrayList<HolviXMLKeyItem>();
	private static Preferences prefs;
	public static final URL iconUrl = BeaverGUI.class.getResource("icons/favicon.png");
	public static String serverUrl = "https://my.holvi.org";
	public static String apikey = "atk8vzrhnc2by4f";
	
	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			serverUrl = args[0];
			apikey = args[1];
		}
		EventQueue.invokeLater(new BeaverGUI());
	}

	@Override
	public void run() {
		// User preferences
		prefs = Preferences.userRoot().node(configNodeName);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// Initialize HolviClient
		client = new HolviClient();
		
		mainWindow = new MainWindow(this);
		mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		
		// Ask for login
		// Modal dialog, application will not proceed without succesful authentication
		LoginDialog loginDlg = new LoginDialog(mainWindow, this);
		loginDlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		loginDlg.setVisible(true);
		
		// Preference change listener for encryption key changes
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if (event.getKey().equals("selected.key")) {
					changeEncryptionKey();
				}
			}
		});
	}

	/**
	 * Notifies {@link #mainWindow} about upload availability status
	 * @param allow state declaring if uploading is possible
	 * @see MainWindow
	 */
	public void allowUpload(boolean allow) {
		mainWindow.allowUpload(allow);
	}

	/**
	 * Returns updated array of available vaults.
	 * <p>
	 * Calls for {@link #listVaults()} to update available {@link #vaults} and returns.
	 * @return available {@link #vaults}
	 * @throws HolviException
	 * @throws IOException
	 * @see Vault
	 */
	public Vault[] getVaults() throws HolviException, IOException {
		listVaults();
		return vaults;
	}
	
	/**
	 * Updates {@link #mainWindow} title to show current cluster
	 * @param title new suffix for the window title
	 * @see MainWindow
	 */
	public void updateTitle(String title) {
		mainWindow.setTitle("Holvi.org Cloud Beaver - " + (title.length() > 0 ? title : "Vault Directory"));
	}

	/**
	 * Loads cipher key and IV from file.
	 * @param path path to the file
	 * @return {@link HashMap} that contains 'iv' and 'key' keys and corresponding byte[] values.
	 * @throws IOException
	 */
	public static HashMap<String, byte[]> loadKeyFromFile(String path) throws IOException {
		HashMap<String, byte[]> returnValues = new HashMap<String, byte[]>();
		byte[] buffer = new byte[32];
		byte[] iv = new byte[16];
		byte[] key = new byte[0];
		File f = new File(path);
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(f);
			fi.read(iv);
			int read = fi.read(buffer);
			if (read != -1) {
				key = new byte[read];
				System.arraycopy(buffer, 0, key, 0, read);
			}
		} finally {
			try {
				if (fi != null) {
					fi.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		returnValues.put("iv", iv);
		returnValues.put("key", key);
		return returnValues;
	}

	/**
	 * Changes encryption key used by {@link #client} and updates {@link #mainWindow}.
	 * @see HolviClient
	 * @see MainWindow
	 */
	public void changeEncryptionKey() {
		String keyHash = prefs.get("selected.key", "");
		if (keyHash.equals("")) {
			mainWindow.updateEncryptionLabel("");
			return;
		}
		HolviXMLKeyItem keyItem = findKeyFromList(keyHash);
		if (keyItem != null) {
			HashMap<String, byte[]> keyMap;
			int keyLength = 0;
			try {
				keyMap = loadKeyFromFile(keyItem.getPath());
				byte[] iv = keyMap.get("iv");
				byte[] key = keyMap.get("key");
				client.setIV(iv);
				client.setEncryptionKey(key);
				mainWindow.updateEncryptionCheckbox(false);
				keyLength = key.length;
				keyItem.exists(true);
			} catch (FileNotFoundException e) {
				keyItem.exists(false);
				mainWindow.updateEncryptionCheckbox(true);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(mainWindow, "Key file not available", "File not found", JOptionPane.OK_OPTION);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			switch (keyLength) {
			case 16:
				client.setEncryptionMode(ENC.AES_128);
				break;
			case 24:
				client.setEncryptionMode(ENC.AES_192);
				break;
			case 32:
				client.setEncryptionMode(ENC.AES_256);
				break;
			default:
				client.setEncryptionMode(ENC.NONE);
				break;
			}
			mainWindow.updateEncryptionLabel(keyItem.getKeyHash());
		}
	}

	/***
	 * Finds a {@link HolviXMLKeyItem} from {@link HolviXMLKeyList} that matches with given hash value.
	 * @param keyHash hash to be searched
	 * @return {@link HolviXMLKeyItem} that matches with keyHash, otherwise null
	 */
	public static HolviXMLKeyItem findKeyFromList(String keyHash) {
		if (keyList != null) {
			for (HolviXMLKeyItem keyItem: keyList) {
				if (keyItem.getKeyHash().equals(keyHash)) {
					return keyItem;
				}
			}
		}
		return null;
	}
	
	/**
	 * Initializes {@link MainWindow} with {@link JMenuBar}, {@link JToolBar}, {@link ClusterTree} and status panel.
	 * @param main 
	 */
	public void intializeMainWindow(MainWindow main) {
		// Init and populate Menu
		menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(450, 20));
		main.populateMenubar(menuBar);
		main.setJMenuBar(menuBar);
		// Init and populate Toolbar
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		main.populateToolbar(toolBar);
		main.add(toolBar, BorderLayout.PAGE_START);

		// Init ScrollView
		ClusterNode homeNode = new ClusterNode();
		treeModel = new ClusterHierarchyModel(homeNode, this);
		treeView = new ClusterTree(treeModel, this);
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(treeView);
		main.add(scrollPanel, BorderLayout.CENTER);
		//		treeView.populateRoot();

		JPanel statusPanel = new JPanel();
		//		statusPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		main.add(statusPanel, BorderLayout.PAGE_END);
		statusPanel.setPreferredSize(new Dimension(main.getWidth(), 24));
		main.populateStatusPanel(statusPanel);
		scrollPanel.requestFocusInWindow();
	}

	/**
	 * Returns the {@link #client} instance used by Holvi.org Cloud Beaver
	 * @return current client
	 * @see HolviClient
	 */
	public HolviClient getClient() {
		return this.client;
	}

	/**
	 * Loads available keys from XML-file located in user's home directory.<p>
	 * Populates {@link #keyList} with available {@link HolviXMLKeyItem}s
	 * Updates {@link #client} with available keys.
	 */
	public void parseKeyFile() {
		File keyDir = new File(prefs.get("org.holvi.keydir", System.getProperty("user.home") + System.getProperty("file.separator") + "holvi"));
		keyDir.mkdirs();
		keyFileXML = new File(keyDir, "keylist.xml");
		try {
			keyFileXML.createNewFile();
		} catch (IOException e) {
			
		}
		JAXBContext context;
		Unmarshaller um;
		HolviXMLKeyList xmlkeyList = null;
		try {
			context = JAXBContext.newInstance(HolviXMLKeyList.class);
			um = context.createUnmarshaller();
			xmlkeyList = (HolviXMLKeyList) um.unmarshal(new InputStreamReader(new FileInputStream(keyFileXML), "UTF-8"));
		} catch (JAXBException e1) {
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		}

		if (xmlkeyList != null) {
			keyList = xmlkeyList.getKeysList();
			if (keyList != null) {
				for (int i = 0; i < keyList.size(); i++) {
					HolviXMLKeyItem keyItem = keyList.get(i);
					try {
						HashMap<String, byte[]> keyMap = loadKeyFromFile(keyItem.getPath());
						client.addEncryptionKey(keyMap.get("iv"), keyMap.get("key"));
						keyItem.exists(true);
					} catch (FileNotFoundException e) {
						keyItem.exists(false);
						continue;
					} catch (IOException e) {
						keyItem.exists(false);
						continue;
					} catch (HolviEncryptionException e) {
						keyItem.exists(false);
						continue;
					}
				}
			} else {
				keyList = new ArrayList<HolviXMLKeyItem>();
			}
		}
	}

	/**
	 * Authenticates {@link #client} with given parameters and sets the {@link #mainWindow} visible.
	 * @param username username for authentication.
	 * @param password password for authentication.
	 * @param rememberUsername should the username be stored in {@link #prefs}
	 * @throws HolviException
	 * @see {@link HolviClient}
	 */
	public void authenticate(String username, String password, boolean rememberUsername) throws HolviException {
		client.setServerUrl(BeaverGUI.serverUrl);
		client.setUsername(username);
		client.setPassword(password);
		client.setApikey(BeaverGUI.apikey);
		client.setEncryptionMode(HolviClient.ENC.NONE);
		client.auth();
		if (rememberUsername) {
			prefs.putBoolean("holvi.remember.username", true);
			prefs.put("holvi.username", username);
		} else {
			prefs.remove("holvi.remember.username");
			prefs.remove("holvi.username");
		}
		intializeMainWindow(mainWindow);
		parseKeyFile();
		changeEncryptionKey();
		mainWindow.setVisible(true);
	}

	/**
	 * Updates {@link #vaults} by querying {@link #HolviClient.listVaults()}
	 * @throws HolviException
	 * @throws IOException
	 * @see HolviClient
	 */
	public void listVaults() throws HolviException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		vaults = client.listVaults(map);
	}
	
	/**
	 * Returns {@link Preferences} used by {@link BeaverGUI}.
	 * @return application {@link #prefs}
	 */
	public static Preferences getPreferences() {
		return prefs;
	}
	
	/**
	 * Return {@link ArrayList} of available {@link HolviXMLKeyItem}s
	 * @return available cipher keys
	 */
	public ArrayList<HolviXMLKeyItem> getKeyList() {
		return keyList;
	}

	/**
	 * Sets {@link #keyList}
	 * @param keylist new encryption keys
	 */
	public void setKeyList(ArrayList<HolviXMLKeyItem> keylist) {
		BeaverGUI.keyList = keylist;
	}

	/**
	 * Returns {@link File} {@link #keyFileXML}
	 * @return file containing available cipher keys in XML format
	 */
	public File getKeyFileXML() {
		return keyFileXML;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int cmd = Integer.parseInt(e.getActionCommand());
		int returnVal;
		TreePath[] paths;
		ClusterTreeNode selection;
		try {
			switch (cmd) {
			case ACTION_HOME:
				treeModel.home();
				break;
			case ACTION_PREVIOUS:
				treeModel.previous();
				break;
			case ACTION_REFRESH:
				treeModel.refresh();
				break;
			case ACTION_ADD_CONTAINER:
				ClusterNode node = (ClusterNode) treeModel.getRoot();
				HolviDialog dialog;
				if (treeModel.isHomeNode(node)) {
					dialog = new VaultDialog(this);
					if (showClusterModifyDialog(dialog)) {
						Cluster cluster = client.addVault(((VaultDialog) dialog).getClusterName(), ((VaultDialog) dialog).getVaultType());
						treeModel.addNode(node, cluster);
					}
				} else {
					dialog = new ClusterDialog();
					if (showClusterModifyDialog(dialog)) {
						Cluster cluster = client.addCluster(((ClusterDialog) dialog).getClusterName(), node.getElement().getId());
						treeModel.addNode(node, cluster);
					}
				}
				break;
			case ACTION_ADD_ITEM:
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(true);
				returnVal = fc.showOpenDialog(this.mainWindow);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fc.getSelectedFiles();
					treeModel.storeData(files);
				}
				break;
			case ACTION_SAVE_ITEM:
				int selectionCount = treeView.getSelectionCount();
				paths = treeView.getSelectionPaths();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setCurrentDirectory(new File(System.getProperty("user.home")));
				if (selectionCount > 1) {
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				} else if (selectionCount == 1){
					selection = treeView.getSelection();
					if (selection instanceof DataItemNode) {
						fc.setSelectedFile(new File(selection.getName()));
					}
				}
				returnVal = fc.showSaveDialog(mainWindow);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					treeModel.fetchData(file, paths);
				}
				break;
			case ACTION_SAVE_ALL:
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				returnVal = fc.showSaveDialog(mainWindow);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					treeModel.fetchAll(file);
				}
				break;
			case ACTION_DELETE:
				treeModel.removeSelected(treeView.getSelectionPaths());
				break;
			case ACTION_MANAGE_KEYS:
				HolviKeyManager manager = new HolviKeyManager(this);
				manager.setVisible(true);
				break;
			case ACTION_MODIFY: 
				paths = treeView.getSelectionPaths();
				try {
					if (paths.length == 1) {
						selection = (ClusterTreeNode) paths[0].getLastPathComponent();
						if (selection instanceof DataItemNode) {
							DataItem di = (DataItem)selection.getElement();
							HashMap<String, String> metamap = ((DataItemNode)selection).getMetaMap();
							ModifyDataItemDialog mdid = new ModifyDataItemDialog(mainWindow, metamap, di.getName());
							returnVal = mdid.showDialog();
							if (returnVal == JOptionPane.OK_OPTION) {
								if (!di.getName().equals(mdid.getName())) {
									di.rename(mdid.getName());
								}
								metamap.put("TGS", mdid.getTags());
								metamap.put("DSC", mdid.getDesc());
								di.updateMeta(metamap);
								treeModel.nodeChanged(selection);
								treeView.repaint();

							}
						} else {
							Cluster cluster = (Cluster)selection.getElement();
							ModifyClusterDialog mcd = new ModifyClusterDialog(mainWindow, cluster.getName());
							returnVal = mcd.showDialog();
							if (returnVal == JOptionPane.OK_OPTION) {
								if (!mcd.getName().equals(cluster.getName())) {
									cluster.rename(mcd.getName());
									treeModel.nodeChanged(selection);
									treeView.repaint();
								}
							}
						}
					}
				} catch (NullPointerException npe) {

				}
				break;
			case ACTION_INFORMATION:
				paths = treeView.getSelectionPaths();
				try {
					if (paths.length == 1) {
						selection = (ClusterTreeNode) paths[0].getLastPathComponent();
						selection.informationDialog().setVisible(true);
					} 
				} catch (NullPointerException npe) {

				}
				break;
			default:
				break;
			}
		} catch (Exception exc) {
			handleException(exc);
		}
	}
	
	/**
	 * Shows a dialog to modify selected {@link ClusterNode} or {@link DataItemNode}.
	 * @param dialog dialog to be shown.
	 * @return <code>true</code> on approved action, otherwise <code>false</code>.
	 * @see ClusterNode
	 * @see DataItemNode
	 * @see HolviDialog
	 */
	public boolean showClusterModifyDialog(HolviDialog dialog) {
		int result = dialog.showDialog();
		if (result == JOptionPane.OK_OPTION) {
			if (dialog instanceof VaultDialog) {
				if (((VaultDialog) dialog).getClusterName().length() > 256) {
					JOptionPane.showMessageDialog(dialog.getParent(), "Vault name is too long\n" +
							"Maximum length is 256 characters", 
							"Invalid vault name", JOptionPane.ERROR_MESSAGE);
					return showClusterModifyDialog(dialog);
				} 
			} else if (dialog instanceof ClusterDialog) {
				if (((ClusterDialog) dialog).getClusterName().length() > 256) {
					JOptionPane.showMessageDialog(dialog.getParent(), "Cluster name is too long\n" +
							"Maximum length is 256 characters", 
							"Invalid cluster name", JOptionPane.ERROR_MESSAGE);
					return showClusterModifyDialog(dialog);
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * Shows dialog of encountered {@link Exception}
	 * @param e {@link Exception} encountered
	 */
	public void handleException(Exception e) {
		if (e instanceof NullPointerException) {
			e.printStackTrace();
			e = new Exception("Internal client exception");
		}
		JOptionPane.showMessageDialog(mainWindow,
				e.getMessage(),
				"An error occurred",
				JOptionPane.ERROR_MESSAGE);
	}

	
	public ClusterHierarchyModel getTreeModel() {
		return this.treeModel;	
	}

}

