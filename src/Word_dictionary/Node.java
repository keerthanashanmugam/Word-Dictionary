package Word_dictionary;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Node implements Serializable {
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		char key;
		List<String> meaning;
		Set<String> synonyms,antonyms;
		int flag;
		Node lochild;
		Node highchild;
		Node equalchild;
		String origWord;
		public Node(char d){
			lochild=null;
			highchild=null;
			equalchild=null;
			flag=0;
			meaning=new ArrayList<String>();
			synonyms=new HashSet<String>();
			antonyms =new HashSet<String>();
			origWord=null;
			key=d;
		}
		Node(){
			lochild=null;
			highchild=null;
			equalchild=null;
			flag=0;
			meaning=new ArrayList<String>();
			synonyms=new HashSet<String>();
			antonyms=new HashSet<String>();
			
		}
		public void copy(Node m){
			//lochild=m.lochild;
			//highchild=m.highchild;
			//equalchild=m.equalchild;
			//flag = m.flag;
			meaning.add(m.meaning.get(0));
			for(String s:m.synonyms)
				synonyms.add(s);
			for(String s:m.antonyms)
				antonyms.add(s);
			
			origWord=m.origWord;
		}
		public void print(){
			System.out.println(meaning);
			System.out.println(antonyms);
			System.out.println(synonyms);
	
		}

}
