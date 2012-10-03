package org.holvi.client.tree;

import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.holvi.client.BeaverGUI;
import org.holvi.client.dialog.ClusterInformationDialog;
import org.holvi.sci.container.Cluster;
import org.holvi.sci.exception.HolviException;
import org.json.JSONException;

/**
 * Node representing a single {@link Cluster}.
 *
 */
public class ClusterNode extends ClusterTreeNode {
	private static final long serialVersionUID = 5192486455209305678L;
	private Cluster object;
	private boolean isRoot;
	
	public ClusterNode(Cluster cluster) {
		super(cluster);
		this.object = cluster;
		this.isRoot = false;
		this.allowsChildren = true;
	}
	
	public ClusterNode() {
		super();
		this.object = null;
		this.isRoot = true;
		this.allowsChildren = true;
	}
	
	@Override
	public String toString() {
		if (object == null) {
			return "";
		}
		return object.getName();

	}
	
	@Override
	public Cluster getElement() {
		return this.object;
	}
	
	@Override
	public boolean isRoot() {
		return isRoot;
	}
	
	@Override
	public void remove() throws HolviException, JSONException, IOException {
		this.object.remove();
	}

	@Override
	public String getName() {
		return object.getName();
	}

	@Override
	public int getParentId() {
		return object.getParentId();
	}

	@Override
	public JPopupMenu popupMenu(BeaverGUI gui) {
		JPopupMenu popup = new JPopupMenu();
		populatePopupMenu(popup, gui);
		return popup;
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	
	public JDialog informationDialog() {
		return new ClusterInformationDialog(object);
	}
	
	private void populatePopupMenu(JPopupMenu menu, BeaverGUI gui) {
		JMenuItem item;
		item = new JMenuItem("Modify");
		item.setActionCommand(Integer.toString(gui.ACTION_MODIFY));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/preferences_edit_16x16.png")));
		menu.add(item);
		
		item = new JMenuItem("Information");
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/information_16x16.png")));
		item.setActionCommand(Integer.toString(gui.ACTION_INFORMATION));
		item.addActionListener(gui);
		menu.add(item);
		menu.addSeparator();
		
		item = new JMenuItem("Delete");
		item.setActionCommand(Integer.toString(gui.ACTION_DELETE));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/delete_16x16.png")));
		menu.add(item);
	}
}
