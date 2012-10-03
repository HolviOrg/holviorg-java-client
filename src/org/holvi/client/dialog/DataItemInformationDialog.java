package org.holvi.client.dialog;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.holvi.client.BeaverGUI;
import org.holvi.client.HolviXMLKeyItem;
import org.holvi.client.SpringUtilities;
import org.holvi.sci.DataItem;
import org.holvi.sci.Utils;

public class DataItemInformationDialog extends JDialog {
	private static final long serialVersionUID = 4132565251099958709L;

	public DataItemInformationDialog(DataItem dataItem) {
		setTitle("Data item information");
		setResizable(false);
		JPanel contentPanel = new JPanel(new SpringLayout());
		int rows = 8;
		HashMap<String, String> metaMap = Utils.metaToMap(dataItem.getMeta());
		JLabel label = new JLabel();
		label = new JLabel("Name:", JLabel.TRAILING);
		contentPanel.add(label);

		JLabel labelContents = new JLabel(dataItem.getName());
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		if (metaMap.containsKey("DSC") && metaMap.get("DSC").length() > 0) {
			label = new JLabel("Description:", JLabel.TRAILING);
			contentPanel.add(label);
			labelContents = new JLabel(metaMap.get("DSC"));
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		} else {
			rows--;
		}
		
		if (metaMap.containsKey("TGS") && metaMap.get("TGS").length() > 0) {
			label = new JLabel("Tags:", JLabel.TRAILING);
			contentPanel.add(label);
			String[] tags = (metaMap.get("TGS") != null ? metaMap.get("TGS").split(","): new String[0]);
			String tagString = "";
			for (int i=0; i<tags.length; i++) {
				tagString += (i > 0 ? ", " + tags[i] : tags[i]);
			}
			labelContents = new JLabel(tagString);
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		} else {
			rows--;
		}
		label = new JLabel("Size:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Utils.humanReadableByteCount(dataItem.getKeyLength(), true));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Last modified:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(dataItem.getLastModifiedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Last accessed:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(dataItem.getLastAccessedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Encryption mode:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(metaMap.get("ENC"));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		if (!metaMap.get("ENC").equalsIgnoreCase("NONE") && metaMap.containsKey("KHA")) {
			rows++;
			label = new JLabel("Key hash:", JLabel.TRAILING);
			contentPanel.add(label);
			labelContents = new JLabel(metaMap.get("KHA"));
			HolviXMLKeyItem keyItem = BeaverGUI.findKeyFromList(metaMap.get("KHA"));
			ImageIcon icon = null;
			if (keyItem != null) {
				icon = new ImageIcon(BeaverGUI.class.getResource("icons/lock_ok_16x16.png"));
				labelContents.setToolTipText("Encryption key available");
			} else {
				icon = new ImageIcon(BeaverGUI.class.getResource("icons/lock_error_16x16.png"));
				labelContents.setToolTipText("Encryption key not available");
			}
			labelContents.setIcon(icon);
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);

			if (keyItem != null) {
				rows++;
				label = new JLabel("Key description:", JLabel.TRAILING);
				contentPanel.add(label);
				labelContents = new JLabel(keyItem.getDescription());
				label.setLabelFor(labelContents);
				contentPanel.add(labelContents);
			}
		}
		label = new JLabel("Container ID:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Integer.toString(dataItem.getParentId()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		SpringUtilities.makeCompactGrid(contentPanel,
				rows, 2, 
				6, 6,        
				6, 6);
		setContentPane(contentPanel);
		pack();
		setLocationByPlatform(true);
	}
}
