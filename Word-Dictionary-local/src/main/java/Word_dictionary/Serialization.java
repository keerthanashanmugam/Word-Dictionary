package Word_dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

public class Serialization {

	public static void seriaize(Object o) throws IOException{
		FileOutputStream fos = new FileOutputStream("BKtree.txt");
		FSTObjectOutput ois = new FSTObjectOutput(fos);
		ois.writeObject(o);
		ois.flush();
		ois.close();
		fos.close();
	}
	public static Object readDictionary1(String file) throws IOException, ClassNotFoundException{
		FileInputStream fin = new FileInputStream(file);
		FSTObjectInput oin = new FSTObjectInput(fin);
		Object o = oin.readObject();
		oin.close();
		fin.close();
		return o;
	}
	public static Object readDictionary2(String file) throws IOException, ClassNotFoundException{
		FileInputStream fin = new FileInputStream(file);
		FSTObjectInput oin = new FSTObjectInput(fin);
		Object o = oin.readObject();
		oin.close();
		fin.close();
		return o;
	}
}

