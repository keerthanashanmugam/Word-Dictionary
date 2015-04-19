package Word_dictionary;

import java.awt.Component;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jws.soap.SOAPBinding.Style;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
	TernarySearchTree t;
	public GUI(TernarySearchTree s){
		t=s;
		initcomponents();
	}
	public void initcomponents(){
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		GroupLayout layout = new GroupLayout(getContentPane());
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		Component textBox = getTextBox();
		Component SearchButton = getSearchButton();
		Component textpane=gettextPane();
		layout.setHorizontalGroup(
				layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(textBox,40,70,150)
								.addComponent(SearchButton,10,50,100))
						.addComponent(textpane)
						);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
						.addComponent(textBox,5,30,30)
						.addComponent(SearchButton,5,15,30))
						.addComponent(textpane)
						);	
				
		
	}
	
	
	private JButton getSearchButton() {
		searchButton=new JButton();
		searchButton.setText("Search");
		searchButton.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent event) {
				String s=query.getText();
				Givetext(s);
			}
		});	
		return searchButton;
	}
	
	private JTextField getTextBox() {
		query=new JTextField();
		query.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				
			}
		});	
		DocumentListener documentListener = new DocumentListener() {
	        @Override
	        public void insertUpdate(DocumentEvent de) {
	        		try{
	        		GiveSuggestions(de.getDocument().getText(0, de.getLength()));
	        		}
	        		catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        		    }
	        }

	        @Override
	        public void removeUpdate(DocumentEvent de) {
	        	try{
	        		GiveSuggestions(de.getDocument().getText(0, de.getLength()));
	        		}
	        	catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        	}
	        }

	        @Override
	        public void changedUpdate(DocumentEvent de) {
	        	try{
	        		GiveSuggestions(de.getDocument().getText(0, de.getLength()));
	        		}
	        	catch (BadLocationException badLocationException) {
	        		      System.err.println("Oops");
	        	}
	        }
	    };
		query.getDocument().addDocumentListener(documentListener);	
		return query;
	}
	public void GiveSuggestions(String s) throws BadLocationException{
		
		
		
	}
	StyleContext context = new StyleContext();
	StyleContext context1 = new StyleContext();
    StyledDocument document = new DefaultStyledDocument();

    MutableAttributeSet style = context.getStyle(StyleContext.DEFAULT_STYLE);
    
    
    MutableAttributeSet style1 =context1.getStyle(StyleContext.DEFAULT_STYLE);
   
    
	public void Givetext(String s){
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
	    StyleConstants.setFontSize(style, 14);
	    StyleConstants.setSpaceAbove(style, 4);
	    StyleConstants.setSpaceBelow(style, 4);
	    StyleConstants.setBold(style, true);
	    StyleConstants.setUnderline(style, true);
	
	    
	    StyleConstants.setAlignment(style1, StyleConstants.ALIGN_RIGHT);
	    StyleConstants.setFontSize(style1, 14);
	    StyleConstants.setSpaceAbove(style1, 4);
	    StyleConstants.setSpaceBelow(style1, 4);
	    StyleConstants.setBold(style1, false);
	    StyleConstants.setUnderline(style1, false);
	    t.search(n, word, index);
	    try {
	    	
	    	document.insertString(0, "Meaning"+":", style);
	    	document.insertString(document.getLength(), " cfdd\n", null);
	    	document.insertString(document.getLength(), s+" \n", style1);
	    	
	    }
	    catch (BadLocationException badLocationException) {
	      System.err.println("Oops");
	    }
	    text.setDocument(document);
	    text.setEditable(false);
	}
	public JTextPane gettextPane(){
		text=new JTextPane();
		text.setEditable(false);
		return text;
	}
}
