package org.holvi.client.tree;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.holvi.client.DropHandler;
import org.holvi.client.BeaverGUI;
import org.holvi.client.dialog.DownloadDialog;
import org.holvi.client.dialog.UploadDialog;
import org.holvi.sci.DataItem;

/**
 * View for {@link ClusterHierarchyModel}. <p>
 * Implements {@link DropTargetListener} and {@link MouseListener} to 
 * handle drag and drop operations.
 *
 */
public class ClusterTree extends JTree implements MouseListener, DropTargetListener {
	private static final long serialVersionUID = 1964455734870239768L;
	private ClusterHierarchyModel model;
	private BeaverGUI gui;
	@SuppressWarnings("unused")
	private DropTarget dropTarget;
	private static final ImageIcon leafIcon = new ImageIcon(BeaverGUI.class.getResource("icons/document.png"));
	private static final ImageIcon folderIcon = new ImageIcon(BeaverGUI.class.getResource("icons/folder.png"));
	public ClusterTree(ClusterHierarchyModel model, BeaverGUI gui) {
		super(model);
		this.model = model;
		this.gui = gui;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		setLayout(new BorderLayout());
		setDragEnabled(true);
		setRootVisible(false);
		getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		setTransferHandler(new DropHandler(gui));
		setShowsRootHandles(false);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(leafIcon);
		renderer.setClosedIcon(folderIcon);
		renderer.setOpenIcon(folderIcon);
		setCellRenderer(renderer);
		addMouseListener(this);
		dropTarget = new DropTarget(this, this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row=this.getRowForLocation(e.getX(),e.getY());  
		if(row==-1) {//When user clicks on the "empty surface"  
			this.clearSelection();
		} else {
			if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
				int x = e.getX();
				int y = e.getY();
				JTree tree = (JTree)e.getSource();
				TreePath path = tree.getPathForLocation(x, y);
				ClusterTreeNode node = null;
				try {
					node = (ClusterTreeNode) path.getLastPathComponent();
				} catch (Exception exc) {
					exc.printStackTrace();
				}

				if (node != null && node instanceof DataItemNode) {
					File file;
					DataItem di = (DataItem) node.getElement();
					File tempdir = new File(System.getProperty("java.io.tmpdir"), "holvi-temp" +  Long.toString(System.nanoTime()));
					tempdir.deleteOnExit();
					tempdir.mkdirs();
					file = new File(tempdir, di.getName());
					file.deleteOnExit();

					DownloadDialog dialog = new DownloadDialog();
					dialog.addFile(di, file);
					dialog.setVisible(true);
					dialog.openFileOnComplete();
					dialog.startTask();

				}

				if (node != null && node instanceof ClusterNode) {
					node.removeAllChildren();
					model.nodeStructureChanged(node);
					try {
						model.setRoot(node);
						model.loadChildren((ClusterNode)node);
						gui.updateTitle(node.toString());
						gui.allowUpload(node != model.getHomeNode());
					} catch (IOException exc) {
						// TODO Auto-generated catch block
					}
				}
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) nodePopupEvent(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) nodePopupEvent(e);
	}

	private void nodePopupEvent(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		JTree tree = (JTree)e.getSource();
		TreePath path = tree.getPathForLocation(x, y);
		if (path == null)
			return; 
		ClusterTreeNode node = (ClusterTreeNode)path.getLastPathComponent();
		setSelectionRow(model.getIndexOfChild(model.getRoot(), node));
		JPopupMenu popup = node.popupMenu(gui);
		popup.show(tree, x, y);
	}

	public ClusterTreeNode getSelection() {
		return (ClusterTreeNode) getSelectionPath().getLastPathComponent();
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		int action = dtde.getDropAction();
		if (model.getRoot() == model.getHomeNode()) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(action);
		}
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		int action = dtde.getDropAction();
		if (model.getRoot() == model.getHomeNode()) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(action);
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			if (dtde.getSource() == this || model.getHomeNode() == model.getRoot()) {
				dtde.rejectDrop();
			}
			Transferable droppedItem = dtde.getTransferable();
			handleDrop(dtde, droppedItem);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("DnD not initalized properly, please try again.");
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	private void handleDrop(DropTargetDropEvent dropEvent, Transferable droppedItem) throws UnsupportedFlavorException, IOException {
		if (droppedItem.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dropEvent.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
			acceptFileListFlavorDrop(dropEvent, droppedItem);
		} else {
			DataFlavor[] flavors = droppedItem.getTransferDataFlavors();
			boolean handled = false;
			for (int i = 0; i < flavors.length; i++) {
				if (flavors[i].isRepresentationClassReader()) {
					dropEvent.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
					Reader reader = flavors[i].getReaderForText(droppedItem);
					BufferedReader br = new BufferedReader(reader);
					List<File> filelist= createFileArray(br);
					handled = true;
					sendFiles(filelist, dropEvent);
					break;
				}
			}
			if(!handled){
				dropEvent.rejectDrop();
			} 
		} 
	}

	public void sendFiles(List<File> filelist, DropTargetDropEvent dropEvent) {
		final Iterator<File> i = filelist.iterator();
		ClusterNode root = (ClusterNode) model.getRoot();

		UploadDialog dialog = new UploadDialog(gui, root);

		while (i.hasNext()) {
			File f = (File) i.next();
			dialog.addFile(f);
		}
		dialog.setVisible(true);
		dialog.startTask();

		dropEvent.dropComplete(true);
	}

	private void acceptFileListFlavorDrop(DropTargetDropEvent dropTargetEvent, Transferable droppedItem) throws UnsupportedFlavorException, IOException {
		@SuppressWarnings("unchecked")
		List<File> list = (List<File>) droppedItem.getTransferData(DataFlavor.javaFileListFlavor);
		sendFiles(list, dropTargetEvent);
	}

	private static String ZERO_CHAR_STRING = "" + (char)0;
	private static List<File> createFileArray(BufferedReader bReader)
	{
		List<File> list = new ArrayList<File>();
		try { 

			java.lang.String line = null;
			while ((line = bReader.readLine()) != null) {
				try {
					// kde seems to append a 0 char to the end of the reader
					if(ZERO_CHAR_STRING.equals(line)) continue; 
					java.io.File file = new java.io.File(line);
					list.add(file);
				} catch (Exception ex) {
				}
			}
		} catch (IOException ex) {
		}
		return list;
	}

}
