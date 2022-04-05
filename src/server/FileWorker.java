package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class FileWorker {

	public static void saveFile(String fileToReceived, byte[] data) {
		// TODO Auto-generated method stub
		Path path = Paths.get(fileToReceived);
		try {
			Files.write(path, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deleteFile(String fileName) throws IOException {
		System.out.println("DELETING FILE	");
		File file
				= new File(fileName);

		if (file.exists()) {
			if(file.isDirectory()){
				deleteDirectoryStream(Paths.get(fileName));
			}else{
				file.delete();

			}
		}
	}

	public static void deleteDirectoryStream(Path path) throws IOException {
		Files.walk(path)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	public static void createFolder(String folderName){
		System.out.println("CREATING FOLDER	");
		File file
				= new File(folderName);

			file.mkdir();

	}

}
