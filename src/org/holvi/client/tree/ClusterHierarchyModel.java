package org.holvi.client.tree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.holvi.client.BeaverGUI;
import org.holvi.client.dialog.DownloadDialog;
import org.holvi.client.dialog.UploadDialog;
import org.holvi.sci.DataItem;
import org.holvi.sci.container.Cluster;
import org.holvi.sci.container.Vault;
import org.holvi.sci.exception.HolviException;
import org.json.JSONException;

/**
 * Model for {@link ClusterTree} containing {@link DataItemNode}s and {@link ClusterNode} as elements.
 *
 */
public class ClusterHierarchyModel extends DefaultTreeModel {
	private static final long serialVersionUID = 3158753260522896349L;
	private ClusterNode homeNode;
	private BeaverGUI gui;

	public ClusterHierarchyModel(ClusterNode treenode, BeaverGUI gui) {
		super(treenode);
		homeNode = treenode;
		this.gui = gui;
		populateRoot();
	}

	public ClusterHierarchyModel(TreeNode treenode, boolean bool) {
		super(treenode, bool);
	}

	@Override
	public boolean isLeaf(Object object) {
		if (object instanceof DataItemNode) {
			return true;
		} 
		return false;
	}

	public boolean isHomeNode(ClusterTreeNode node) {
		if (node == homeNode) {
			return true;
		}
		return false;
	}
	
	public void loadChildren(final ClusterNode node) throws IOException {
		node.removeAllChildren();
		Cluster[] clusters = null;
		DataItem[] dataitems = null;
		try {
			HashMap<String, Object[]> elements = node.getElement().getElements();
			clusters = (Cluster[]) elements.get("clusters");
			dataitems = (DataItem[]) elements.get("dataitems");
		} catch (HolviException e) {
			gui.handleException(e);
		}	

		for (Cluster cluster: clusters) {
			node.add(new ClusterNode(cluster));
		}
		for (DataItem dataitem: dataitems) {
			dataitem.setParentId(node.getElement().getId());
			node.add(new DataItemNode(dataitem));
		}
		nodeStructureChanged(node);
	}

	public void populateRoot() {
		nodeStructureChanged(homeNode);
		homeNode.removeAllChildren();
		try {
			for (Vault vault: gui.getVaults()) {
				ClusterNode node = new ClusterNode(vault);
				homeNode.add(node);
			}

		} catch (HolviException e) {
			gui.handleException(e);
		} catch (IOException e) {
			gui.handleException(e);
		}
		nodeStructureChanged(homeNode);
	}


	public void removeSelected(TreePath[] paths) {
		if (paths.length == 1) {
			ClusterTreeNode node = (ClusterTreeNode)paths[0].getLastPathComponent();
			String dialogTitle = "Remove vault";

			if (node instanceof DataItemNode) {
				dialogTitle = "Remove dataitem";
			} else if (node.getParentId() > 0) {
				dialogTitle = "Remove cluster";
			}

			int n = JOptionPane.showConfirmDialog(
					null,
					"Are you sure you want to remove " + node.getName() + "?",
					dialogTitle,
					JOptionPane.YES_NO_OPTION);
			switch (n) {
			case JOptionPane.YES_OPTION:
				removePaths(paths);
				break;
			default: break;
			}
		} else if (paths.length > 1){
			int n = JOptionPane.showConfirmDialog(
					null,
					"Are you sure you want to remove selected items?",
					"Remove " + Integer.toString(paths.length) + " items",
					JOptionPane.YES_NO_OPTION);
			switch (n) {
			case JOptionPane.YES_OPTION:
				removePaths(paths);
				break;
			default: break;
			}
		}
	}

	public void removePaths(final TreePath[] paths) {
		Thread worker = new Thread() {
			public void run() {
				ClusterTreeNode node;
				try {
					for (int i = 0; i < paths.length; i++) {
						node = (ClusterTreeNode)paths[i].getLastPathComponent();
						node.remove();
						removeNodeFromParent(node);
					}
				} catch (HolviException e) {
					gui.handleException(e);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}}
		};
		worker.start();
	}

	public void downloadPaths(TreePath[] paths, final File destDirectory) {
		ClusterTreeNode node;
		destDirectory.mkdir();
		DownloadDialog dialog = new DownloadDialog();
		for (int i = 0; i < paths.length; i++) {
			node = (ClusterTreeNode)paths[i].getLastPathComponent();
			if (node instanceof DataItemNode) {
				File destination  = new File(destDirectory, node.getName());
				dialog.addFile((DataItem)node.getElement(), destination);
				//				downloadDataItem((DataItemNode) node, destination);
			}
		}
		dialog.pack();
		dialog.setVisible(true);
		dialog.startTask();
	}

	public void downloadDataItem(DataItemNode node, File destination) {
		DataItem di = (DataItem) node.getElement();
		DownloadDialog dialog = new DownloadDialog();
		dialog.addFile(di, destination);
		dialog.pack();
		dialog.setVisible(true);
		dialog.startTask();
	}

	public void addNode(DefaultMutableTreeNode root, Cluster c) {
		ClusterNode clusterNode = new ClusterNode(c);
		insertNodeInto(clusterNode, root, root.getChildCount());
	}

	public void previous() {
		ClusterNode root = (ClusterNode) getRoot();
		//		this.previousNode = root;
		TreeNode parent = root.getParent();
		if (parent != null) {
			setRoot(parent);
			root.removeAllChildren();
			gui.updateTitle(parent.toString());
			gui.allowUpload(parent != homeNode);
		}
	}

	public void home() {
		this.homeNode.removeAllChildren();
		populateRoot();
		setRoot(this.homeNode);
		gui.updateTitle(this.homeNode.toString());
		gui.allowUpload(false);
	}

	public synchronized void storeData(File[] files) {
		ClusterNode node = (ClusterNode) getRoot();
		UploadDialog dialog = new UploadDialog(gui, node);
		for (File file: files) {
			dialog.addFile(file);
		}
		dialog.startTask();
	}

	public void fetchData(final File destination, final TreePath[] paths) {
		Thread worker = new Thread() {
			public void run() {
				if (paths.length == 1) {

					DataItemNode node = (DataItemNode) paths[0].getLastPathComponent(); 
					downloadDataItem(node, destination);
				} else if (paths.length > 1) {
					downloadPaths(paths, destination);
				}

			}
		};
		worker.start();
	}

	public void fetchAll(final File destination) {
		ClusterNode node = (ClusterNode) getRoot();
		if (node.getChildCount() >= 0) {
			TreePath[] paths = new TreePath[node.getChildCount()];
			for (int i=0; i<node.getChildCount(); i++) {
				ClusterTreeNode child = (ClusterTreeNode) node.getChildAt(i);
				paths[i] = new TreePath(child.getPath());
			}
			downloadPaths(paths, destination);
		}
	}

	public void refresh() throws IOException {
		ClusterNode node = (ClusterNode) getRoot();
		if (node == homeNode) {
			populateRoot();
		} else {
			loadChildren(node);
		}
	}

	public ClusterTreeNode getHomeNode() {
		return this.homeNode;
	}

}

