package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import data.DataFile;
import data.SEND_TYPE;

//Déclaration de la classe principale de ServerHandler
public class ServerHandler extends Thread {

	private Socket socket;
	private boolean isStop = false;

	InputStream is;

	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	DataFile m_dtf;

	//Déclaration de la méthode d'initialisation de ServerHandler
	public ServerHandler(Socket socket) throws Exception {
		this.socket = socket;
		os = socket.getOutputStream();
		is = socket.getInputStream();

		SendDataThread sendDataThread = new SendDataThread();
		sendDataThread.start();

		m_dtf = new DataFile();
	}

	//Exécution du runnable du thread
	@Override
	public void run() {
		System.out.println("Processing: " + socket);
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				connectClientFail();
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Complete processing: " + socket);

		closeSocket();

	}

	//Lecture des objets reçus provenant du socket
	void readData() {
		try {

			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);
			} else if (obj instanceof DataFile) {
				readFile(obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
			connectClientFail();
			closeSocket();
		}
	}

	//Lecture des messages reçus
	public String readString(Object obj) throws IOException {
		String str = obj.toString();

		if (str.equals("STOP"))
			isStop = true;
		else if (str.contains("DOWNLOAD_FILE")) {
			String[] array = str.split("--");
			sendFile(array[1]);
		}else if (str.contains("DELETE_FILE")) {
			str = str.replace("DELETE_FILE", "");
			FileWorker.deleteFile(str.replace("C:\\client\\","C:\\temp\\"));
		}

		else if (str.contains("CREATE_FOLDER")) {
			str = str.replace("CREATE_FOLDER", "");
			FileWorker.createFolder(str.replace("C:\\client\\","C:\\temp\\"));
		}

		return str;
	}

	//Lecture des fichiers reçus
	void readFile(Object obj) throws IOException {
		DataFile dtf = (DataFile) obj;
		m_dtf.data = dtf.data;
		m_dtf.lastTime = dtf.lastTime;
		m_dtf.size = dtf.size;
		m_dtf.name = dtf.name.replace("C:\\client\\","C:\\temp\\");
		m_dtf.saveFile(m_dtf.name);
	}

	//Thread principal du serveur
	class SendDataThread extends Thread {
		@Override
		public void run() {
			while (!isStop) {
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (sendType != SEND_TYPE.DO_NOT_SEND) {
					try {
						sendData();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	//Gestion de l'envoi des messages ou des fichiers à travers le socket
	private void sendData() throws IOException {
		if (sendType == SEND_TYPE.SEND_STRING) {
			sendMessage(message);
		}  else if (sendType == SEND_TYPE.SEND_FILE) {

			Path path = Paths.get(fileName);
			DataFile dtf = new DataFile();
			dtf.data = Files.readAllBytes(path);
			dtf.name = fileName;
			dtf.size = Files.size(path);
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			dtf.lastTime = new Date(attr.lastModifiedTime().toMillis());
			while(!Files.isReadable(path));
			dtf.data = Files.readAllBytes(path);

			sendMessage(dtf);

		}
		sendType = SEND_TYPE.DO_NOT_SEND;

	}

	//Entête d'envoi d'un message
	void sendString(String str) {
		System.out.println("SENDING STRING	");
		sendType = SEND_TYPE.SEND_STRING;
		message = str;
	}

	//Entête d'envoi d'un fichier
	void sendFile(String fileName) {
		System.out.println("SENDING FILE	");
		this.fileName = fileName;
		sendType = SEND_TYPE.SEND_FILE;

	}

	//Fonction d'envoi de message sous forme d'objet String ou DataFile
	public synchronized void sendMessage(Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			if (obj instanceof String) {
				String message = obj.toString();
				oos.writeObject(message);
				oos.flush();
			}
			else if (obj instanceof DataFile) {
				oos.writeObject(obj);
				oos.flush();
			}
		} catch (Exception e) {
		}
	}

	//Arrêt sur erreur socket
	private void connectClientFail() {
		isStop = true;
		closeSocket();
	}

	//Arrêt du socket
	private void closeSocket() {
		isStop = true;
		try {
			this.sendString("STOP");
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (socket != null)
				socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
