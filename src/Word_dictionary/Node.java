package Word_dictionary;

public class Node {
		char key;
		String meaning;
		String[] synonyms;
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
			key=d;
		}

}
