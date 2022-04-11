package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import data.DataFile;
import data.SEND_TYPE;
import server.FileWorker;

//Déclaration de la classe principale ClientSocketThread
public class ClientSocketThread extends Thread {

	private Socket socket;
	private boolean isStop = false;

	InputStream is;


	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;


	DataFile m_dtf;
	ISocketListener iSocketListener;

	//Déclaration de la méthode d'initialisation de ClientSocketThread
	public ClientSocketThread(ISocketListener iSocketListener) {
		this.iSocketListener = iSocketListener;
		m_dtf = new DataFile();
	}

	//Déclaration de la méthode d'initialisation du socket avec pour paramètres son ip et son port
	public void setSocket(String serverIp, int port) {
		try {
			socket = new Socket(serverIp, port);
			System.out.println("Connected: " + socket);

			os = socket.getOutputStream();
			is = socket.getInputStream();


			SendDataThread sendDataThread = new SendDataThread();
			sendDataThread.start();
			iSocketListener.showDialog("CONNECTED TO SERVER", "INFOR");
		} catch (Exception e) {
			System.out.println("Can't connect to server");
		}
	}

	//Méthode runnable de la classe
	@Override
	public void run() {
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				connectServerFail();
				e.printStackTrace();
				break;
			}
		}
		closeSocket();
	}

	//Méthode permettant la lecture des objets reçus en String ou DataFile
	void readData() throws Exception {

			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);

			} else if (obj instanceof DataFile) {
				readFile(obj);
			}
	}

	//Méthode de lecture des messages reçus
	void readString(Object obj) throws IOException {
		String str = obj.toString();
		if (str.equals("STOP"))
			isStop = true;
		else if (str.contains("DELETE_FILE")) {
				str = str.replace("DELETE_FILE", "");
				FileWorker.deleteFile(str.replace("C:\\temp\\","C:\\client\\"));
			}
		else if (str.contains("CREATE_FOLDER")) {
			str = str.replace("CREATE_FOLDER", "");
			FileWorker.createFolder(str.replace("C:\\temp\\","C:\\client\\"));
		}
	}

	//Méthode de lecture des fichiers reçus
	void readFile(Object obj) throws IOException{
		DataFile dtf = (DataFile) obj;
		m_dtf.data = dtf.data;
		m_dtf.lastTime = dtf.lastTime;
		m_dtf.size = dtf.size;
		m_dtf.name = dtf.name.replace("C:\\temp\\","C:\\client\\");
		m_dtf.saveFile(m_dtf.name);
	}

	//Méthode principale du thread du client
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

	//Méthode d'envoi des messages String ou des fichiers DataFile
	private void sendData() throws IOException {
		// TODO Auto-generated method stub
		if (sendType == SEND_TYPE.SEND_STRING) {
			sendMessage(message);
		} else if (sendType == SEND_TYPE.SEND_FILE) {

			Path path = Paths.get(fileName);
			DataFile dtf = new DataFile();
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

	//Méthode d'entête d'envoi des messages
	void sendString(String str) {
		System.out.println("SENDING STRING	" + str);
		sendType = SEND_TYPE.SEND_STRING;
		message = str;
	}

	//Méthode d'entête d'envoi des fichiers
	void sendFile(String fileName) {
		System.out.println("SENDING FILE	");
		this.fileName = fileName;
		sendType = SEND_TYPE.SEND_FILE;
	}

	//Méthode d'envoi des messages et des fichiers DataFile
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
		}	}

	//Méthode d'arrêt du socket sur erreur
	private void connectServerFail() {
		isStop = true;
		closeSocket();
	}

	//Méthode d'arrêt du socket
	public void closeSocket() {
		isStop = true;
		try {
			this.sendString("STOP");

			if (is != null)
				is.close();
			if (os != null)
				os.close();
			if (socket != null)
				socket.close();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
