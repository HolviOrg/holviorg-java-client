package org.holvi.client.dialog;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.holvi.client.SpringUtilities;
import org.holvi.sci.Utils;
import org.holvi.sci.container.Cluster;

public class ClusterInformationDialog extends JDialog {
	private static final long serialVersionUID = 4467779036913817740L;

	public ClusterInformationDialog(Cluster cluster) {
		setTitle("Container information");
		int rows = 5;
		setResizable(false);
		JPanel contentPanel = new JPanel(new SpringLayout());
		JLabel label = new JLabel();
		label = new JLabel("Name:", JLabel.TRAILING);
		contentPanel.add(label);

		JLabel labelContents = new JLabel(cluster.getName());
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Last modified:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(cluster.getLastModifiedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Last accessed:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Utils.parseDateTimeString(cluster.getLastAccessedDate()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);

		label = new JLabel("Container ID:", JLabel.TRAILING);
		contentPanel.add(label);
		labelContents = new JLabel(Integer.toString(cluster.getId()));
		label.setLabelFor(labelContents);
		contentPanel.add(labelContents);
		
		if (cluster.getParentId() > 0) {
			rows++;
			label = new JLabel("Parent ID:", JLabel.TRAILING);
			contentPanel.add(label);
			labelContents = new JLabel(Integer.toString(cluster.getParentId()));
			label.setLabelFor(labelContents);
			contentPanel.add(labelContents);
		}
		
		label = new JLabel("Permissions:", JLabel.TRAILING);
		contentPanel.add(label);
		String permissionString = "";
		String[] permissions = cluster.getPermissions();
		for (int i=0; i<permissions.length; i++) {
			permissionString += (i > 0 ? ", " + permissions[i] : permissions[i]);
		}
		labelContents = new JLabel(permissionString);
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
