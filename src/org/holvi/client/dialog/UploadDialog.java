package org.holvi.client.dialog;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import org.holvi.client.BeaverGUI;
import org.holvi.client.tree.ClusterNode;
import org.holvi.client.tree.DataItemNode;
import org.holvi.sci.DataItem;
import org.holvi.sci.HolviClient;
import org.holvi.sci.container.Cluster;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;

/**
 * 
 *
 */
public class UploadDialog extends TransferFrame implements ActionListener, PropertyChangeListener  {
	private static final long serialVersionUID = 4392195188843021681L;
	private UploadTask task;
	private final Object[] options = {"Replace all", "Replace", "Skip", "Skip all"};

	/**
	 * 
	 *
	 */
	class UploadTask extends SwingWorker<Void, Void> {
		private ArrayList<File> fileArray = new ArrayList<File>();
		private HolviClient client;
		private Cluster cluster;
		private BeaverGUI gui;
		private ClusterNode node;
		private Long totalLength = 0L;
		private Long progress = 0L;
		private boolean autoClose = true;
		int i;
		@Override
		protected Void doInBackground() {
			String method = "new";
			int replaceAswer = 2; // skip one
			boolean exists = false;
			for (i = 0; i<fileArray.size(); i++) {
				File file = fileArray.get(i);
				String name = file.getName();

				if (file.isFile()) {
					FileInputStream fi = null;
					DataItem[] dataItemArray = null;
					try {
						dataItemArray = cluster.getDataItems(new String[] {name});
					} catch (HolviException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					DataItem dataItem;
					if (dataItemArray.length == 1) {
						exists = true;
						dataItem = dataItemArray[0];
						if (replaceAswer != 0 && replaceAswer != 3) {
							int result = JOptionPane.showOptionDialog(UploadDialog.this, 
									"Dataitem with given name already exists, how to proceed?", "Replace dataitem", 
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
							switch (result) {
							case 0:
								method = "replace";
								replaceAswer = result;
								break;
							case 1:
								method = "replace";
								break;
							case 2:
								method = "new";
								firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());
								break;
							case 3:
								method = "new";
								replaceAswer = result;
								firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());
								break;
							default:
								break;
							}
						} else {
							if (replaceAswer == 0) {
								method = "replace";
							}
							if (replaceAswer == 3) {
								method = "new";
							}
							firePropertyChange("totalNumBytesRead", progress, progress+dataItem.getKeyLength());
						}
					} else {
						exists = false;
						method = "new";
						dataItem = new DataItem(client, name, cluster.getId());

					}

					try {
						fi = new FileInputStream(file);
						int j = i+1;
						taskOutput.append("Sending " + file.getName() + " (" + j + "/" + fileArray.size() +")" + " ... ");
						ProgressStream prs = new ProgressStream(fi, totalLength);
						prs.addPropertyChangeListener(UploadDialog.this);
						int endPosition = taskOutput.getDocument().getLength();
						Rectangle bottom = taskOutput.modelToView(endPosition);
						bottom.width = 0;
						taskOutput.scrollRectToVisible(bottom);
						if (!exists || method.equals("replace")) {
							if(sendFile(dataItem, method, prs)) {
								if (method.equals("new")) {
									dataItem.setKeyLength(file.length());
									gui.getTreeModel().insertNodeInto(new DataItemNode(dataItem), node, node.getChildCount());
								} 
							} else {
								autoClose = false;
							}
						}
						taskOutput.append("\n");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (BadLocationException e) {
						e.printStackTrace();
					} finally {
						if (fi != null) {
							try {
								fi.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			return null;
		}
		private boolean sendFile(DataItem dataItem, String method, InputStream is) {
			try {
				dataItem.storeData(method, is);
				taskOutput.append("OK");
				return true;
			} catch (HolviException e1) {
				autoClose = false;
				taskOutput.append("[" + e1.getMessage() + "] FAILED");
				e1.printStackTrace();
				return false;
			} catch (IOException e1) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e1.printStackTrace();
				return false;
			} catch (InvalidKeyException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (InvalidAlgorithmParameterException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				return false;
			} catch (HolviEncryptionException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void done() {
			taskOutput.append("Done!\n");
			if (autoClose) {
				dispose();
			}
		}

		public void addFile(File file) {
			fileArray.add(file);
			totalLength += file.length();

		}

		public void setup(BeaverGUI gui, ClusterNode node) {
			this.client = gui.getClient();
			this.cluster = node.getElement();
			this.node = node;
			this.gui = gui;
		}
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int value;
		if ("totalNumBytesRead" == evt.getPropertyName()){
			task.progress += (Long)evt.getNewValue() - (Long)evt.getOldValue();
			value = (int) ((task.progress.doubleValue()/task.totalLength.doubleValue())*100);
			progressBar.setValue(value);
			int j = task.i+1;
			setTitle("Uploading file " + j + " of " + task.fileArray.size() + " - " + Integer.toString(value) + "%");
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}

	public UploadDialog(BeaverGUI gui, ClusterNode root) {
		setTitle("Upload in progress...");
		progressBar.setIndeterminate(true);
		task = new UploadTask();
		task.addPropertyChangeListener(this);
		task.setup(gui, root);
		setVisible(true);
	}

	/**
	 * Add new file to be sent.
	 * @param file file to be sent in this {@link UploadTask}.
	 */
	public void addFile(File file) {
		task.addFile(file);
	}

	public void startTask() {
		task.execute();
	}
}
