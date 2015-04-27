package Word_dictionary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.Toolkit;

import org.omg.CORBA.portable.InputStream;




public class GUI_Duplicate  implements ActionListener{
	static TernarySearchTree s;static Bktree bk ;
	 static SwingWorker Ternaryworker= new SwingWorker<String[], Void>() {
	        @Override
	        public String[] doInBackground() throws InterruptedException, ClassNotFoundException, IOException {
	            System.out.print("work1 started");
	        	s=(TernarySearchTree) Serialization.readDictionary1("Dictionary.txt");
	            System.out.print("work1 over");
	        	return null;
	        }
	        public void done() {
	            //Remove the "Loading images" label.
	            //animator.removeAll();
	            //loopslot = -1;
	        	System.out.println("done");
	            if(BkTreeworker.isDone()){
					try {
						changeGUI();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	        } 
	        
	        
	 };
	
	 static SwingWorker BkTreeworker = new SwingWorker<String[], Void>() {
	        @Override
	        public String[] doInBackground() throws InterruptedException, ClassNotFoundException, IOException {
	        	System.out.print("work2 started");
	        	bk = (Bktree) Serialization.readDictionary2("BKtree.txt");
	            System.out.print("work2 over");
	        	return null;
	        }
	        
	        public void done() {
	            //Remove the "Loading images" label.
	            //animator.removeAll();
	            //loopslot = -1;
	        	System.out.println("done");
	            if(Ternaryworker.isDone()){
					try {
						changeGUI();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	        } 
	 };
	
	 public static  void changeGUI() throws IOException{
		g.dispose();
		
		g=new GUI(s,bk);
		g.setTitle("WordDictionary");
		 Image image = new ImageIcon("DicIcon.png").getImage();

	        g.setIconImage(image);
		g.setVisible(true);
	 }
	 static JLabel statusLabel;
	 static GUI g;
	 public static void createGUI() throws IOException {
	        //Animate from right to left if offset is negative.
	       
	        //Custom component to draw the current image
	        //at a particular offset.
	       // removeAll(); 
		 	g=new GUI();

	//	 	g.setIconImage(new ImageIcon(getClass().getResource("/resource/icon.png")).getImage());
		 	g.setBackground(Color.white);
	        System.out.print("again");
	        g.setLayout(new BorderLayout());
	        g.setTitle("Word Dictionary");
	        //Put a "Loading Images..." label in the middle of
	        //the content pane.  To center the label's text in
	        //the applet, put it in the center part of a
	        //BorderLayout-controlled container, and center-align
	        //the label's text.
	        statusLabel = new JLabel("Loading Dictionary...",
	                                 JLabel.CENTER);
	        g.add(statusLabel, BorderLayout.CENTER);
	        g.setSize(new Dimension(600,400));
	        g.setVisible(true);
	        
	       // setContentPane(g.getContentPane());
	    }
	 
	public  static void main(String[] args){
		try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                	System.out.println("yes");
                    try {
						createGUI();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            });
        } catch (Exception e) { 
            System.err.println("createGUI didn't successfully complete");
        }
		 Ternaryworker.execute();
	        BkTreeworker.execute();
	        Image image = new ImageIcon("DicIcon.png").getImage();

	        g.setIconImage(image);
	        g.setVisible(true);
	      //  g.setSize(300, 300);
	}
	
	public void actionPerformed(ActionEvent e) {
        //If still loading, can't animate.
        if (!Ternaryworker.isDone() || !BkTreeworker.isDone()) {
            return;
        }

       // loopslot++;
/*
        if (loopslot >= nimgs) {
            loopslot = 0;
            off += offset;

            if (off < 0) {
                off = width - maxWidth;
            } else if (off + maxWidth > width) {
                off = 0;
            }
        }
*/	System.out.print("startsdfsd");
        try {
			startaction();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        
    }
	public void startaction() throws IOException{
		if(Ternaryworker.isDone() && BkTreeworker.isDone()){
			System.out.print("start");
			g.initcomponents(s,bk);
			
		}
		
	}
}