package org.holvi.client.tree;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.holvi.client.BeaverGUI;
import org.holvi.client.dialog.DataItemInformationDialog;
import org.holvi.sci.DataItem;
import org.holvi.sci.Utils;
import org.holvi.sci.exception.HolviException;
import org.json.JSONException;

/**
 * Node representing a single {@link DataItem}
 *
 */
public class DataItemNode extends ClusterTreeNode {
	private static final long serialVersionUID = -3712740684567040272L;
	private DataItem object;
	private HashMap<String, String> metaMap;

	public DataItemNode(DataItem dataitem) {
		super(dataitem);
		this.object = dataitem;
		this.setMetaMap(Utils.metaToMap(dataitem.getMeta()));
		this.allowsChildren = false;
		this.children = null;
	}

	@Override
	public String toString() {
		String displayName = object.getName();
		displayName += " (" + Utils.humanReadableByteCount(object.getKeyLength(), true) + ")";
		displayName += (metaMap.get("DSC") != null && metaMap.get("DSC").length() > 0 ? " - " + metaMap.get("DSC"): "");
		if (metaMap.get("TGS") != null && metaMap.get("TGS").length() > 0) {
			String tags[] = metaMap.get("TGS").split(",");
			displayName += " [";
			for (int i=0; i<tags.length; i++) {
				displayName += (i > 0 ? ", " + tags[i] : tags[i]);
			}
			displayName += "]";
		}
		return displayName;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public void remove() throws HolviException, JSONException, IOException {
		this.object.remove();
	}

	@Override
	public DataItem getElement() {
		return this.object;
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
	
	public JDialog informationDialog() {
		return new DataItemInformationDialog(object);
	}
	
	private void populatePopupMenu(JPopupMenu menu, BeaverGUI gui) {
		JMenuItem item;
		item = new JMenuItem("Download");
		item.setActionCommand(Integer.toString(gui.ACTION_SAVE_ITEM));
		item.addActionListener(gui);
		item.setIcon(new ImageIcon(BeaverGUI.class.getResource("icons/floppy_disk_16x16.png")));
		menu.add(item);

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

	public HashMap<String, String> getMetaMap() {
		return metaMap;
	}

	public void setMetaMap(HashMap<String, String> metaMap) {
		this.metaMap = metaMap;
	}
}
