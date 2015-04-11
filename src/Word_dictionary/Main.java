package Word_dictionary;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		TernarySearchTree t = dataBase.Make_Dictionary();
		Serialization.seriaize(t);
		TernarySearchTree s=(TernarySearchTree) Serialization.readDictionary();
		
		Node n =s.search(s.root, "tooth_root", 0);
		if(n!=null)
		n.print();
	//	s.printTree(s.root);
	}

}
