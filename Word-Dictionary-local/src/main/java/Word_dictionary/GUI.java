package Word_dictionary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.jws.soap.SOAPBinding.Style;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;



public  class GUI extends JFrame{

	/**
	 * 
	 */
	
	
	 
	private static final long serialVersionUID = 1L;
	JButton searchButton;
	JTextField query;
	JPanel SuggestionPanel;
	JTextPane text;
	JPanel mainpanel=new JPanel();
	TernarySearchTree t;
	Bktree BK;
	JLayeredPane layer=new JLayeredPane();
	JList Wordlist;
	DefaultListModel<String> listModel=new DefaultListModel<String>();
	
	public GUI(){
		
	}
	public GUI(TernarySearchTree s,Bktree bk) throws IOException{
		t=s;
		BK = bk;
		initcomponents(s,bk);
		
	}
	
	public void initcomponents(TernarySearchTree s,Bktree bk) throws IOException{
		t=s;
		BK = bk;
		
		System.out.println("entered");
		this.setPreferredSize(new Dimension(600, 400));
		this.setLayout(new BorderLayout());
		this.add(layer,BorderLayout.CENTER);
		layer.setBounds(0, 0, 600, 400);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		GroupLayout layout = new GroupLayout(mainpanel);
		mainpanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		Component textBox = getTextBox();
		Component SearchButton = getSearchButton();
		Component Scrollpane=gettextPane();
			
		layout.setHorizontalGroup(
				layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(textBox,150,150,150)
								.addComponent(SearchButton,100,100,100))
						.addComponent(Scrollpane)
						);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
						.addComponent(textBox,30,30,30)
						.addComponent(SearchButton,30,30,30))
						.addComponent(Scrollpane)
						);	
	//	getContentPane().add(new JPanelWithBackground("index.jpeg"));
		mainpanel.setBackground(Color.BLUE);
        mainpanel.setBounds(0, 0, 600, 400);
        mainpanel.setOpaque(true);
        SuggestionPanel=getSuggestionPanel();
        SuggestionPanel.setBackground(Color.GREEN);
        Insets insets=query.getInsets();
        System.out.print(insets.left+" " +insets.right+ " "+ insets.top + " " + insets.bottom);
        SuggestionPanel.setBounds(10, 40, 150, 300);
        SuggestionPanel.setOpaque(true);
        SuggestionPanel.setVisible(false);
        layer.add(mainpanel, new Integer(0), 0);
        layer.add(SuggestionPanel, new Integer(1), 0);
        pack();
        setVisible(true);
	}
	
	private JPanel getSuggestionPanel(){
		SuggestionPanel=new JPanel();
		SuggestionPanel.setVisible(false);
		Wordlist =new JList<String>(listModel);
		JScrollPane pane = new JScrollPane(Wordlist);
		DefaultListSelectionModel m = new DefaultListSelectionModel();
	    m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    m.setLeadAnchorNotificationEnabled(false);
	    Wordlist.setSelectionModel(m);

	    Wordlist.addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	    	  	if (e.getValueIsAdjusting() == false) {
	    	  		System.out.print(e.getSource());
	    	  		
	    	  	}
	    	  } 
	    });
	    Wordlist.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			        System.out.println("Sending ACTION_PERFORMED to ActionListener");
			        String s=query.getText();
			        try {
			        	
						Givetext(s);
						
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        e.consume();
			        } 
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
				if(e.getKeyCode()== KeyEvent.VK_DOWN){
					if(Wordlist.getSelectedIndex()<20){
						int i=Wordlist.getSelectedIndex();
						System.out.println(i);
						String s=(String)Wordlist.getModel().getElementAt(i+1);
						query.setText(s);
						if(i==-1)
							Wordlist.setSelectedIndex(1);
					}
				}
				else if(e.getKeyCode()== KeyEvent.VK_UP){
					if(Wordlist.getSelectedIndex()<20 && Wordlist.getSelectedIndex()>0){
						int i=Wordlist.getSelectedIndex();
						System.out.println(i);
						String s=(String)Wordlist.getModel().getElementAt(i-1);
						query.setText(s);
						if(i==-1)
							Wordlist.setSelectedIndex(0);
					}
				}
			}
		});
	    		   
		
		SuggestionPanel.setLayout(new BorderLayout());
		SuggestionPanel.setPreferredSize(new Dimension(20,20));
		Insets query_insets=query.getInsets();
		Dimension size=query.getPreferredSize();
		SuggestionPanel.setBounds(query_insets.left, query_insets.top+size.height,SuggestionPanel.getPreferredSize().width , SuggestionPanel.getPreferredSize().height);
		SuggestionPanel.add(pane,BorderLayout.CENTER);
		return SuggestionPanel;
	}
	
	
	private JButton getSearchButton() {
		searchButton=new JButton();
		searchButton.setText("Search");
		searchButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent event) {
				String s=query.getText();
				try {
					Givetext(s);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});	
		return searchButton;
	}
	
	private JTextField getTextBox() {
		query=new JTextField();
		
		query.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				try{
					if(e.getKeyCode()!=KeyEvent.VK_ENTER){
						JTextField textentered=(JTextField)e.getSource();
						String s=textentered.getText();
						if(s!=null)
        					GiveSuggestions(s);
					}
        		}
        		catch (BadLocationException badLocationException) {
        		      System.err.println("Oops");
        		    }
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			        System.out.println("Sending ACTION_PERFORMED to ActionListener");
			        String s=query.getText();
			        try {
			        	
						Givetext(s);
						
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        e.consume();
			        } 
			}
		});	
		
		/*
		DocumentListener documentListener = new DocumentListener() {
	        @Override
	        public void insertUpdate(DocumentEvent de) {
	        		try{
	        			if(query.getText()!=null)
	        					GiveSuggestions(query.getText());
	        		}
	        		catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        		    }
	        }

	        @Override
	        public void removeUpdate(DocumentEvent de) {
	        	try{
	        		if(query.getText()!=null)
	        			GiveSuggestions(query.getText());
	        		}
	        	catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        	}
	        }

	        @Override
	        public void changedUpdate(DocumentEvent de) {
	        	try{
	        		if(query.getText()!=null)
	        			GiveSuggestions(query.getText());
	        		}
	        	catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        	}
	        }
	    };
		query.getDocument().addDocumentListener(documentListener);
		*/	
		return query;
	}
	public void GiveSuggestions(String s) throws BadLocationException{
		if(query.getText()==null){
			SuggestionPanel.setVisible(false);return;
		}
		System.out.print(s);
		ArrayList<String> lst=t.autoComplete(s);
		int x=lst.size() < 20 ? lst.size(): 20;
		System.out.print(x);
		
		listModel.removeAllElements();
		int i;
		for(i=0;i<x;i++){
			listModel.addElement(lst.get(i));
			
		}
	//	System.out.println(listModel);
		
		System.out.println(Wordlist.getModel());
		    
		SuggestionPanel.setVisible(true);
		
	}
	
	
	StyleContext context = new StyleContext();
	StyleContext context1 = new StyleContext();
    StyledDocument document = new DefaultStyledDocument();

    MutableAttributeSet style = context.getStyle(StyleContext.DEFAULT_STYLE);
    
    
    MutableAttributeSet style1 =context1.getStyle(StyleContext.DEFAULT_STYLE);
   
    
	public void Givetext(String s) throws BadLocationException{
		SuggestionPanel.setVisible(false);
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
	    StyleConstants.setFontSize(style, 14);
	    StyleConstants.setSpaceAbove(style, 4);
	    StyleConstants.setSpaceBelow(style, 4);
	    StyleConstants.setBold(style, true);
	    StyleConstants.setUnderline(style, true);
	    StyleConstants.setItalic(style, true);
	    
	    StyleConstants.setAlignment(style1, StyleConstants.ALIGN_RIGHT);
	    StyleConstants.setFontSize(style1, 14);
	    StyleConstants.setSpaceAbove(style1, 4);
	    StyleConstants.setSpaceBelow(style1, 4);
	    StyleConstants.setBold(style1, false);
	    StyleConstants.setUnderline(style1, false);
	    Node search_result = t.search(t.root, s, 0);
	    
	    if(search_result == null ){
	   
	    	document.remove(0,document.getLength());
	    	ArrayList<String> correct = new ArrayList<String>();
	    	BK.search(BK.Root, s, correct, 1);

		    try {
		    	String meaning = "";
		    	
		    	document.insertString(0, "Suggested Words"+":\n", style);
		    	System.out.println(correct);
		    	ListIterator<String> lst = correct.listIterator();
		    	int i;
		    	while(lst.hasNext()){
		    		String oneWord = lst.next();
		    		
		    		document.insertString(document.getLength(),"->",style);
		    		document.insertString(document.getLength(),oneWord+"\n",style1);
		    		
		    	}
		    			    	
		    }
		    catch (BadLocationException badLocationException) {
		      System.err.println("Oops");
		    }
	    	
	    }
	    else if( search_result.meaning.isEmpty()  ){
	    		
	    	document.remove(0,document.getLength());
	    	ArrayList<String> correct = new ArrayList<String>();
	    	BK.search(BK.Root, s, correct, 1);

		    try {
		    	String meaning = "";
		    	
		    	document.insertString(0, "Suggested Words"+":\n", style);
		    	System.out.println(correct);
		    	ListIterator<String> lst = correct.listIterator();
		    	while(lst.hasNext()){
		    		String oneWord = lst.next();
		    		document.insertString(document.getLength(),"->",style);
		    		document.insertString(document.getLength(),oneWord+"\n",style1);
		    	}
		    			    	
		    }
		    catch (BadLocationException badLocationException) {
		      System.err.println("Oops");
		    }
	    	
	    }
	    else{
	    document.remove(0,document.getLength());
	    try {
	    	String meaning = "";
	    	
	    	document.insertString(0, "Meaning"+":\n", style);
	    	int i;
	    	ListIterator<String> lst = search_result.meaning.listIterator();
	    	while(lst.hasNext()){
	    		String oneMeaning = lst.next();
	    		String[] words=oneMeaning.split(";");
	    		document.insertString(document.getLength(),"->",style);
	    		document.insertString(document.getLength(),words[0]+"\n",style1);
	    		document.insertString(document.getLength(),"Example"+":\n",style);
	    		for(i=1;i<words.length;i++){
	    			document.insertString(document.getLength(),words[i]+"\n",style1);
	    			
	    		}
	    	}
	    	
	  //  	document.insertString(document.getLength(), " cfdd\n", null);
	  //  	document.insertString(document.getLength(), s+" \n", style1);
	    	
	    }
	    catch (BadLocationException badLocationException) {
	      System.err.println("Oops");
	    }
	    }
//		JScrollPane scrollpane = new JScrollPane(text,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
//	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		this.getContentPane().add(scrollpane,BorderLayout.CENTER);
	    text.setDocument(document);
	    text.setEditable(false);
	}
	public JScrollPane gettextPane() throws IOException{
		text=new JTextPane();
		JScrollPane scrollpane = new JScrollPane(text);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setPreferredSize(new Dimension(250,145));
		scrollpane.setMinimumSize(new Dimension(10,10));
		scrollpane.setViewportView(text);
		
		text.setEditable(false);
		text.setVisible(true); 
		text.setOpaque(true);
		
		
		return scrollpane;
	}
}
