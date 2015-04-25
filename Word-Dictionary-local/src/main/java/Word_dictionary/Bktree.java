package Word_dictionary;



import java.io.Serializable;
import java.util.List;


class BkNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String word;
	BkNode[] Children;
	public BkNode(String x){
		x=x.toLowerCase();
		Children=new BkNode[60];
		word=x;
	}
}



public class Bktree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BkNode Root;
	public void add(String word){
		word=word.toLowerCase();
		if(Root==null)
			Root=new BkNode(word);
		else{
			int d=getdistance(Root.word,word);
			if(d==0){return ;}
			
			else{
				BkNode temp=Root;
				while(temp.Children[d]!=null){
					if(d==0){return;}
					temp=temp.Children[d];
					d=getdistance(temp.word,word);
				}
				temp.Children[d]=new BkNode(word);
			}
		}
	}
	public int min(int a ,int b){
		return a<b?a:b;
	}
	
	
	public int getdistance(String word1,String word2){
		int m=word1.length(),n=word2.length();
		int[][] distances=new int[m+1][n+1];
		int i,j,k;
		
		for(i=0;i<m+1;i++){
			for(j=0;j<n+1;j++){
				if(i==0){
					distances[i][j]=j;continue;
				}
				if(j==0){
					distances[i][j]=i;continue;
				}
				if(word1.charAt(i-1)==word2.charAt(j-1))
					k=0;
				else k=1; 
				distances[i][j]=min(min(distances[i-1][j]+1,distances[i][j-1]+1),distances[i-1][j-1]+k);
			}
		}
		return distances[word1.length()][word2.length()];
	}
	
	public void search(BkNode node,String word,List<String> correctwords,int tolerance){
		
		int k=getdistance(node.word, word);
	//	System.out.println(node.word);
		if(k<=tolerance)
			correctwords.add(node.word);
		int mindist=0,maxdist=0;
		if(k-tolerance>=0)
			mindist=k-tolerance;
		maxdist=k+tolerance;
		for(BkNode b:node.Children)
			if(b!=null){
				if(getdistance(b.word, node.word)>=mindist && getdistance(b.word, node.word)<=maxdist)
					search(b,word,correctwords,tolerance);
			}
	}
}
