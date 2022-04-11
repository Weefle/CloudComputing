package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileWorker {

	//Méthode pour supprimer un fichier ou un dossier
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

	//Méthode permettant la suppression récursive d'un dossier
	public static void deleteDirectoryStream(Path path) throws IOException {
		Files.walk(path)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	//Méthode pour la création d'un dossier
	public static void createFolder(String folderName){
		System.out.println("CREATING FOLDER	");
		File file
				= new File(folderName);

			file.mkdir();

	}

}
