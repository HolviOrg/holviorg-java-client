package org.holvi.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;

import org.holvi.client.dialog.UploadDialog;
import org.holvi.client.tree.ClusterHierarchyModel;
import org.holvi.client.tree.ClusterNode;
import org.holvi.client.tree.ClusterTree;

/**
 * Handles all copy and paste file operations in {@link BeaverGUI} instance.
 *
 */
public class DropHandler extends TransferHandler {
	private static final long serialVersionUID = -6611620362488703404L;
	private BeaverGUI gui;

	public DropHandler(BeaverGUI gui) {
		this.gui = gui;
	}

	@SuppressWarnings("rawtypes")
	public boolean importData(JComponent comp, Transferable t) {
		if (!(comp instanceof JTree)) {
			return false;
		}

		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		ClusterTree tree = (ClusterTree) comp;
		final ClusterHierarchyModel model = (ClusterHierarchyModel) tree.getModel();
		final ClusterNode root = (ClusterNode) model.getRoot();
		if (root == model.getHomeNode()) {
			return false;
		}
		List data = null;
		try {
			data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		final Iterator i = data.iterator();
		//		Thread worker = new Thread() {
		//			public void run() {
		UploadDialog dialog = new UploadDialog(gui, root);
		while (i.hasNext()) {
			File f = (File) i.next();
			dialog.addFile(f);
		}
		dialog.setVisible(true);
		dialog.startTask();
		return true;
	}

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if (comp instanceof JTree) {
			for (int i = 0; i < transferFlavors.length; i++) {
				if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
