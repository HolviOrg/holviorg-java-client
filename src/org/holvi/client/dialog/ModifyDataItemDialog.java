package org.holvi.client.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.holvi.client.BeaverGUI;
import org.holvi.client.WrapLayout;

public class ModifyDataItemDialog extends HolviDialog {
	private static final long serialVersionUID = -8065189591501016726L;
	private JTextField name;
	private JLabel lblName;
	private JTextField description;
	private JLabel lblDescription;
	private JTextField tags;
	private JLabel lblTags;
	private JPanel checkboxPanel;
	private JScrollPane scrollPane;
	private URL imageURL = BeaverGUI.class.getResource("icons/error_16x16.png");
	private Vector<String> tagVector;
	private HashMap<String, String> metamap;
	private boolean answer = false;
	
	public ModifyDataItemDialog(JFrame parent, HashMap<String, String> metaMap, String diName) {
		super(parent, true);
		setTitle("Modify " + diName);
		this.metamap = metaMap;
		tagVector = new Vector<String>();
		setLocationRelativeTo(parent);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.insets = new Insets(0, 0, 5, 0);

		lblName = new JLabel("Name: ");
		cs.gridx = 0;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 1;
		cs.weightx = 0;
		panel.add(lblName, cs);

		name = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 0;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.gridwidth = 2;
		cs.weightx = 1.0;
		name.setText(diName);
		panel.add(name, cs);

		lblDescription = new JLabel("Description: ");
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 1;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		panel.add(lblDescription, cs);

		description = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 1;
		cs.gridwidth = 2;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 1.0;
		description.setText(metaMap.get("DSC"));
		panel.add(description, cs);

		lblTags = new JLabel("Tags: ");
		cs.gridx = 0;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 0;
		panel.add(lblTags, cs);

		tags = new JTextField(getWidth());
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 2;
		cs.fill = GridBagConstraints.HORIZONTAL;
		cs.weightx = 1.0;
		panel.add(tags, cs);

		JButton addButton = new JButton("Add");
		cs.gridx = GridBagConstraints.RELATIVE;
		cs.gridy = 2;
		cs.gridwidth = 1;
		cs.weightx = 0;
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tagText = tags.getText().trim();
				if (!tagText.equals("")) {
					addTags(tagText);
				}
				tags.setText("");
				
				checkboxPanel.revalidate();
				checkboxPanel.repaint();
			}
		});
		panel.add(addButton, cs);

		checkboxPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		checkboxPanel.setSize(new Dimension(320, 1));
		try {
			addTags(metamap.get("TGS"));
		} catch (NullPointerException e) {
			// No existing tags
		}
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(panel, BorderLayout.NORTH);
		scrollPane = new JScrollPane(checkboxPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		
		JButton generateButton = new JButton("Save");
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tags.getText().length() > 0) {
					addTags(tags.getText());
				}
				answer = true;
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				answer = false;
				setVisible(false);
			}
		});
		JPanel bp = new JPanel();
		bp.add(generateButton);
		bp.add(cancelButton);
		getContentPane().add(bp, BorderLayout.PAGE_END);
		
		setMinimumSize(new Dimension(320, 350));
		pack();
	}
	
	private void addTags(String tag) {
		String[] tagArray = tag.split(",");
		for (String str: tagArray) {
			if (str.trim().length() == 0) {
				continue;
			}
			final JCheckBox check = new JCheckBox(str.trim());
			check.setIcon(new ImageIcon(imageURL));
			check.setSelectedIcon(new ImageIcon(imageURL));
			check.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkboxPanel.remove(check);
					tagVector.remove(check.getText());
					checkboxPanel.revalidate();
					checkboxPanel.repaint();
				}
			});
			checkboxPanel.add(check);
			tagVector.add(str);
		}
	}
	@Override
	public int showDialog() {
		setVisible(true);
		if (answer) {
			return JOptionPane.OK_OPTION;
		}
		return JOptionPane.CANCEL_OPTION;
	}
	
	public String getTags() {
		StringBuilder sb = new StringBuilder();
		for (String tag: tagVector) {
			sb.append(tag.trim() + ",");
		}
		String tagString = sb.toString();
		 // remove last ,
		if (tagString.length() == 0) {
			return "";
		}
		return tagString.substring(0, tagString.length()-1);
	}

	public String getDesc() {
		return description.getText().trim();
	}
	
	public String getName() {
		return name.getText().trim();
	}
}
