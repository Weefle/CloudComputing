package data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("serial")
public class DataFile implements Serializable {

	public byte[] data;
	public String name;


	public DataFile() {
		data = new byte[0];
		name = "";
	}

	public void saveFile(String fileToReceived) {
		// TODO Auto-generated method stub
		Path path = Paths.get(fileToReceived);
		try {
			Files.write(path, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}