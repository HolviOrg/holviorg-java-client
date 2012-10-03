package org.holvi.client.dialog;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.holvi.sci.DataItem;
import org.holvi.sci.exception.HolviEncryptionException;
import org.holvi.sci.exception.HolviException;

/**
 * 
 *
 */
public class DownloadDialog extends TransferFrame implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = -7919455375814441902L;
	private DownloadTask task;
	private final Object[] options = {"Replace all", "Replace", "Skip", "Skip all"};

	/**
	 * 
	 *
	 */
	class DownloadTask extends SwingWorker<Void, Void> {
		List<File> fileArray = new ArrayList<File>();
		List<DataItem> dataItemArray = new ArrayList<DataItem>();
		long maxSize = 0;
		long read = 0;
		boolean openFileOnComplete = false;
		boolean autoClose = true;
		
		@Override
		protected Void doInBackground() throws Exception {
			setProgress(0);
			boolean allowReplace = false;
			boolean askReplace = true;
			for (int i=0; i<fileArray.size(); i++) {
				taskOutput.append("Downloading file: " + dataItemArray.get(i).getName() + " ... ");

				if (askReplace) {
					if (fileArray.get(i).exists()) {
						allowReplace = false;
						int result = JOptionPane.showOptionDialog(DownloadDialog.this, 
								"File already exists, how to proceed?", "File already exists", 
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
						switch (result) {
						case 0:
							allowReplace = true;
							askReplace = false;
							break;
						case 1:
							allowReplace = true;
							break;
						case 2:
							allowReplace = false;
							break;
						case 3:
							allowReplace = false;
							askReplace = false;
							break;
						default:
							break;
						}
					}
				}
				if (!fileArray.get(i).exists() || allowReplace) {
					if (saveFile(fileArray.get(i), dataItemArray.get(i))) {
						taskOutput.append("OK\n");
					} else {
						autoClose = false;
						taskOutput.append("FAILED\n");
					}
				} else {
					read += dataItemArray.get(i).getKeyLength();
					setProgress((int) ((read / (double)maxSize)*100));
					taskOutput.append("SKIPPED\n");
				}

				int endPosition = taskOutput.getDocument().getLength();
				Rectangle bottom = taskOutput.modelToView(endPosition);
				bottom.width = 0;
				taskOutput.scrollRectToVisible(bottom);
			}
			if (fileArray.size() == 1 && openFileOnComplete) {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					try {
						desktop.open(fileArray.get(0).getAbsoluteFile());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(DownloadDialog.this, 
								"Could not open the file\n" + 
										fileArray.get(0).getAbsolutePath() + 
								"\nTry opening it manually");
					}
				}
			}
			return null;
		}

		public boolean saveFile(File file, DataItem dataItem) {
			InputStream bis = null;
			FileOutputStream fo = null;
			byte[] buff = new byte[20971520];
			int len = 0;
			int inbuff = 0;
			if (file.exists() && file.isDirectory()) {
				taskOutput.append("Destination name was directory, file skipped ");
				return false;
			}
			
			try {
				bis = dataItem.getData();
				fo = new FileOutputStream(file);
				while((len = bis.read(buff, 0, 20971520)) != -1) {
					inbuff = len;
					read += inbuff;
					int value2 = (int)(((double)read/(double)maxSize)*100.0);
					setProgress(value2);
					while ((inbuff < 20971520)) {
						len = bis.read(buff, inbuff, 20971520-inbuff);
						if (len == -1) {
							break;
						}
						inbuff += len;
						read += len;
						
						setProgress((int)(((double)read/(double)maxSize)*100.0));
					}
					fo.write(buff, 0, inbuff);
				}
			} catch (InvalidKeyException e) {
				autoClose = false;
				taskOutput.append(" FAILED");
				e.printStackTrace();
				new IllegalKeyDialog();
				return false;
			} catch (HolviException e) {
				return false;
			} catch (HolviEncryptionException e) {
				if (e.getId() == 0) {
					openFileOnComplete = false;
					JOptionPane.showMessageDialog(DownloadDialog.this, e.getMessage());
				}
				return false;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			} finally {
				if (fo != null) {
					try {
						fo.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			return true;
		}

		@Override
		public void done() {
			taskOutput.append("Done!\n");
			if (autoClose) {
				dispose();
			}
		}

		public void addFile(DataItem dataItem, File file) {
			fileArray.add(file);
			dataItemArray.add(dataItem);
			maxSize += dataItem.getKeyLength();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		} 		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	public DownloadDialog() {
		setTitle("Download in progress...");
		task = new DownloadTask();
		task.addPropertyChangeListener(this);
	}

	/**
	 * Add new data item to {@link DownloadTask} related.
	 * @param dataItem data item to be retrieved
	 * @param file file where the data will be saved
	 */
	public void addFile(DataItem dataItem, File file) {
		task.addFile(dataItem, file);
	}

	/**
	 * Start {@link DownloadTask}.
	 */
	public void startTask() {
		task.execute();
	}

	public void openFileOnComplete() {
		task.openFileOnComplete = true;
	}
}
