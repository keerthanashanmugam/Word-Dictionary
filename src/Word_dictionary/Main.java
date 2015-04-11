package Word_dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		TernarySearchTree t = dataBase.Make_Dictionary();
		Serialization.seriaize(t);
		TernarySearchTree s=(TernarySearchTree) Serialization.readDictionary();
		System.out.println("yes");
		Scanner in = new Scanner(System.in);
	/*	TernarySearchTree s = new TernarySearchTre																																													e();
		s.calInsert("beatroot",new Node('s') );
		s.calInsert("root", new Node('d'));
		s.calInsert("Mohammedan",new Node('s'));*/
		Node n =s.search(s.root, "tooth_root", 0);			                                                                                                           
		Node m =s.search(s.root, "monkey", 0);
		if(n!=null)																												
		n.print();
		n.print();
		int y=0;String str;
		while(y==0){
			str = in.next();
			n = s.search(s.root, str, 0);
			n.print();
		}
	//	s.findWords();
		//s.printTree(s.root);
	}

}
