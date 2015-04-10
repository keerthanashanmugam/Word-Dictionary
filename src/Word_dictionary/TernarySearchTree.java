package Word_dictionary;

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
	
}
