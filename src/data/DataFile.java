package data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class DataFile implements Serializable {

	public byte[] data;
	public Date lastTime;
	public String name;
	public long size;


	public DataFile() {
		data = new byte[0];
		lastTime = new Date();
		size = 0;
		name = "";
	}

	public void saveFile(String fileToReceived) throws IOException {
		// TODO Auto-generated method stub
		Path path = Paths.get(fileToReceived);

		if(!Files.exists(path)){
			new File(fileToReceived).getParentFile().mkdirs();
			Files.write(path, data);
		}

		}


}