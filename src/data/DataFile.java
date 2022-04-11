package data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

//Déclaration de la classe DataFile pour avoir un objet qui va définir notre fichier à envoyer
public class DataFile implements Serializable {

	public byte[] data;
	public Date lastTime;
	public String name;
	public long size;

//Déclaration de la méthode d'initialisation de DataFile
	public DataFile() {
		data = new byte[0];
		lastTime = new Date();
		size = 0;
		name = "";
	}

	//Déclaration de la méthode SaveFile permettant la sauvegarde du fichier vers la destination indiquée
	public void saveFile(String fileToReceived) throws IOException {
		Path path = Paths.get(fileToReceived);

		if(!Files.exists(path)){
			new File(fileToReceived).getParentFile().mkdirs();
			Files.write(path, data);
		}

		}


}