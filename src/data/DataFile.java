package data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystemException;
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

	public void saveFile(String fileToReceived) throws IOException {
		// TODO Auto-generated method stub
		Path path = Paths.get(fileToReceived);
		if(!new File(fileToReceived).exists()) {
			Files.write(path, data);
		}
	}

}