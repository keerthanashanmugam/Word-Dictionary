package Word_dictionary;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		char key;
		String meaning;
		ArrayList<String> synonyms,antonyms;
		int flag;
		Node lochild;
		Node highchild;
		Node equalchild;
		public Node(char d){
			lochild=null;
			highchild=null;
			equalchild=null;
			flag=0;
			meaning=null;
			synonyms=null;
			antonyms = null;
			key=d;
		}
		Node(){
			lochild=null;
			highchild=null;
			equalchild=null;
			flag=0;
			meaning=null;
			synonyms= new ArrayList<String>();
			antonyms= new ArrayList<String>();
			
		}
		public void copy(Node m){
			lochild=m.lochild;
			highchild=m.highchild;
			equalchild=m.equalchild;
			flag = m.flag;
			meaning=m.meaning;
			synonyms=m.synonyms;
			antonyms = m.antonyms;
		}
		public void print(){
			System.out.println(meaning);
			System.out.println(antonyms);
			System.out.println(synonyms);
	
		}

}
