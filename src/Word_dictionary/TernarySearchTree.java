package Word_dictionary;
import java.util.ArrayList;

public class TernarySearchTree {
	Node root;
	
	public TernarySearchTree(){
		root=null;
	}
	public void insert(Node r,String word){
		if(r==null){
			r=new Node(word.charAt(0));
			if(word.length()>1)
				insert(r.equalchild,word.substring(1));
			else
				r.flag=1;
		}
		else{
			if(r.key==word.charAt(0)){
				if(word.length()>1)
					insert(r.equalchild,word.substring(1));
				else
					r.flag=1;
			}
			else{
				if(r.key<word.charAt(0)){
					insert(r.highchild,word.substring(0));
				}
				else{
					insert(r.lochild,word.substring(0));
				}
			}
		}
		
	}
	
	public Node search(Node n,String word,int index){
		
		if(n==null)
			return null;
		else if(word.charAt(index)<n.key)
			return this.search(n.highchild, word, index);
		else if(word.charAt(index)>n.key)
			return this.search(n.lochild, word, index);
		else
		{
			if(index == word.length()-1)
				return n;
			return this.search(n.equalchild, word, index+1);
		}
	}
	
	public void getListFrom(Node n,ArrayList<String> lst,String s){
		
		if(n == null)
			lst.add(s);
		getListFrom(n.lochild,lst,s);
		getListFrom(n.equalchild,lst,s+n.key);
		getListFrom(n.highchild,lst,s);
		
	}
	
	public ArrayList<String> autoComplete(String word,int index){
		
		Node n = this.search(root, word, 0);	
		ArrayList<String> lst = new ArrayList<String>();
		getListFrom(n,lst,word);
		return lst;
		
	}
	
}
