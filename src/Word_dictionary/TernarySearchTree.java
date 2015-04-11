package Word_dictionary;
import java.io.Serializable;
import java.util.ArrayList;

public class TernarySearchTree implements Serializable{
	
	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = -8465116787318420577L;
	Node root;
	
	public TernarySearchTree(){
		root=null;
	}

	public void calInsert(String word,Node real_node){
		root = this.insert(root, word.toLowerCase(), real_node);
		
	}
	public Node insert(Node r,String word,Node real_node){
		
		if(r==null){
			r = new Node(word.charAt(0));
			if(word.length()>1)
				{
				r.equalchild = insert(r.equalchild,word.substring(1),real_node);
				}
			else
				{
					
					r.copy(real_node);
					r.flag=1;
					
				}
		}
		else{
			if(r.key==word.charAt(0)){
				if(word.length()>1)
					r.equalchild = insert(r.equalchild,word.substring(1),real_node);
				else
					{
					
						r.copy(real_node);
						r.flag=1;
						
					}
			}
			else{
				if(r.key<word.charAt(0)){
					r.highchild = insert(r.highchild,word.substring(0),real_node);
				}
				else{
					r.lochild =insert(r.lochild,word.substring(0),real_node);
				}
			}
		}
		return r;
		
	}
	
	public void printTree(Node r){
		if(r==null)
			return;
		System.out.println(r.key);
		printTree(r.lochild);
		printTree(r.equalchild);
		printTree(r.highchild);
	}

	public void findWords(){
		ArrayList<String> lst = new ArrayList<String>();
		
		getListFrom(root, lst,"",'\0');
		System.out.println(lst.size()+"==================== "+lst);
		// comment addes
	}
	public Node search(Node n,String word,int index){
		
		if(n==null)
			return null;
		else if(word.charAt(index)<n.key)
			return this.search(n.lochild, word, index);
		else if(word.charAt(index)>n.key)
			return this.search(n.highchild, word, index);
		else
		{
			if(index == word.length()-1)
				return n;
			return this.search(n.equalchild, word, index+1);
		}
	}
	
	public void getListFrom(Node n,ArrayList<String> lst,String s,char c){
		
		if(n == null  )
			return;

		if(c!='\0')
			s += c;
		if(n.flag == 1)
				lst.add(s+n.key);
		
		getListFrom(n.lochild,lst,s,'\0');

		getListFrom(n.equalchild,lst,s,n.key);
		getListFrom(n.highchild,lst,s,'\0');
		return ;
	}
	
	public ArrayList<String> autoComplete(String word,int index){
		
		Node n = this.search(root, word, 0);
		System.out.println(n.key);
		ArrayList<String> lst = new ArrayList<String>();
		word = word.substring(0, word.length()-1);
		
		
		getListFrom(n,lst,word,'\0');
		return lst;
		
	}
	
}
