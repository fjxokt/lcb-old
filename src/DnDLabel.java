import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;


@SuppressWarnings("serial")
public class DnDLabel extends JLabel 
					  implements Transferable, 	DragSourceListener, DragGestureListener, DropTargetListener {

    //marks this JButton as the source of the Drag
    private DragSource source;
    private DnDLabel[] lst;
    private String[] items;
    private static DataFlavor DnDLabelDataFlavor = new DataFlavor(DnDLabel.class, "DnDLabel");

    private TransferHandler t;

    public DnDLabel(DnDLabel[] l){
    	super();
    	
    	lst = l;
    	t = new TransferHandler(){
            public Transferable createTransferable(JComponent c){
                  return new DnDLabel(lst);
            }
    	};
    	setTransferHandler(t);
	
    	//The Drag will copy the DnDButton rather than moving it
    	source = new DragSource();
    	source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	            
    	// drop target
    	this.setDropTarget(new DropTarget(this, this));
    }
    
    public void setBuild(LoLBuild b) {
    	items = b.items;
    }
    

    // The DataFlavor is a marker to let the DropTarget know how to
 	// handle the Transferable
  	public DataFlavor[] getTransferDataFlavors() {
  		return new DataFlavor[]{new DataFlavor(DnDLabel.class, "DnDLabel")};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return true;
    }

    public Object getTransferData(DataFlavor flavor) {
        return this;
    }

    public void dragEnter(DragSourceDragEvent dsde) {}
    public void dragOver(DragSourceDragEvent dsde) {}
    public void dropActionchanged(DragSourceDragEvent dsde) {}
    public void dragExit(DragSourceEvent dse) {}
    public void dragDropEnd(DragSourceDropEvent dsde) {}

    //when a DragGesture is recognized, initiate the Drag
    public void dragGestureRecognized(DragGestureEvent dge) {
    	if (!isEnabled()) return;
    	if (DragSource.isDragImageSupported()) {
    		Icon icon = this.getIcon();
            int imgW = icon.getIconWidth(), imgH = icon.getIconHeight();
            BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) img.getGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();
           
            Point hotspot = dge.getDragOrigin();
            hotspot.x = -getWidth() + hotspot.x; hotspot.y = -getHeight() + hotspot.y;
            /*MouseEvent inputEvent = (MouseEvent)dge.getTriggerEvent();
            int x = inputEvent.getX(), y = inputEvent.getY();
            hotspot.x = x - hotspot.x; hotspot.y = y - hotspot.y;*/
            dge.startDrag(null, img, hotspot, (Transferable)getTransferData(DnDLabelDataFlavor), this);
    	}
    	else {
    		source.startDrag(dge, DragSource.DefaultMoveDrop, (Transferable)getTransferData(DnDLabelDataFlavor), this);
    	}
    }

	public void dropActionChanged(DragSourceDragEvent arg0) {}
	public void dragEnter(DropTargetDragEvent dtde) {}
	public void dragExit(DropTargetEvent dte) {}
	public void dragOver(DropTargetDragEvent dtde) {}
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	public void drop(DropTargetDropEvent e) {
		// Holds the dropped data
		Transferable t = e.getTransferable();
	    DnDLabel res = null;

	    // try to get the DnDLabel
	    try {
	      res = (DnDLabel)t.getTransferData(DnDLabelDataFlavor);
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }	    
	    
	    // position des deux DnDlabel dans la liste (connue grace au nom donné)
	    int moi = Integer.parseInt(this.getName());
	    int autre = Integer.parseInt(res.getName());
	    
	    // don't ask me why I need to do that...
	    // tooltips won't appear anymore if I don't this trick
	    JPanel pan = (JPanel)getParent();
	    for (int i=0, imax=pan.getComponentCount(); i<imax; i++) {
	    	ToolTipManager.sharedInstance().registerComponent((DnDLabel)pan.getComponent(i));
	    }
	    
	    // switching items
	    String bt = items[moi];
	    items[moi] = items[autre];
	    items[autre] = bt;
	    
	    // switch tooltips
	    bt = lst[moi].getToolTipText();
	    lst[moi].setToolTipText(lst[autre].getToolTipText());
	    lst[autre].setToolTipText(bt);

	    // and icons
	    Icon ic = lst[moi].getIcon();
	    lst[moi].setIcon(lst[autre].getIcon());
	    lst[autre].setIcon(ic);
	    
	    e.dropComplete(true);
	}

}
