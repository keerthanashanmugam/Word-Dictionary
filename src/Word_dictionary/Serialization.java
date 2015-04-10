package Word_dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serialization {

	public void seriaize(Object o) throws IOException{
		FileOutputStream fos = new FileOutputStream("Dictionary.txt");
		ObjectOutputStream ois = new ObjectOutputStream(fos);
		ois.writeObject(o);
		ois.flush();
		ois.close();
		fos.close();
	}
	public Object readDictionary() throws IOException, ClassNotFoundException{
		FileInputStream fin = new FileInputStream("Dictionary.txt");
		ObjectInputStream oin = new ObjectInputStream(fin);
		Object o = oin.readObject();
		oin.close();
		fin.close();
		return o;
	}
}

