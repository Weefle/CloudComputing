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

	/*File[] getAllFileName() {
		File f = new File(URL_FOLDER);
		//List<String> fs = new ArrayList<>();
		//File[] files = f.listFiles();
		return f.listFiles();

	}

	String[] searchFile(String keyword) {
		File file = new File(URL_FOLDER);
		String[] files = file.list();
		ArrayList<String> fileSearches = new ArrayList<String>();
		for (String file1 : files)
			if (searchStringInFile(URL_FOLDER + "\\" + file1, keyword))
				fileSearches.add(file1);
		for (int i = 0; i < fileSearches.size(); i++)
			System.out.println("File searches : " + fileSearches.get(i));

		String[] result = new String[fileSearches.size()];
		result = fileSearches.toArray(result);
		return result;
	}

	boolean searchStringInFile(String fileName, String keyword) {
		File file = new File(fileName);

		try {
			Scanner scanner = new Scanner(file);

			// now read the file line by line...
			int lineNum = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lineNum++;
				if (line.contains(keyword)) {
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			// handle this
		}
		return false;
	}*/

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

	/*public boolean checkFile(String fileNameReceived) {
		// TODO Auto-generated method stub
		File file = new File(URL_FOLDER);
		String[] files = file.list();
		for (String file1 : files)
			if (file1.equals(fileNameReceived))
				return false;
		return true;
	}*/

	/*public String getFileName(String str) {
		String result = "";
		int len = str.length();
		for (int i = len - 1; i > 0; i--)
			if (str.charAt(i) == '\\')
				return (new StringBuilder(result)).reverse().toString();
			else
				result += str.charAt(i);

		return null;
	}*/
}
