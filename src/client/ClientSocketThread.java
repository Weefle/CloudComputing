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

public class ClientSocketThread extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// Receive
	InputStream is;


	// Send
	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	// Data file

	DataFile m_dtf;
	ISocketListener iSocketListener;

	public ClientSocketThread(ISocketListener iSocketListener) {
this.iSocketListener = iSocketListener;
				m_dtf = new DataFile();
	}

	public void setSocket(String serverIp, int port) {
		try {
			socket = new Socket(serverIp, port);
			// Connect to server
			System.out.println("Connected: " + socket);

			os = socket.getOutputStream();
			is = socket.getInputStream();


			SendDataThread sendDataThread = new SendDataThread();
			sendDataThread.start();
			iSocketListener.showDialog("CONNECTED TO SERVER", "INFOR");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Can't connect to server");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//this.sendString("VIEW_ALL_FILE");
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				connectServerFail();
				e.printStackTrace();
				break;
			}
		}
		closeSocket();
	}

	void readData() throws Exception {

			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();

			if (obj instanceof String) {
				readString(obj);

			} else if (obj instanceof DataFile) {
				readFile(obj);
			}
	}

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

	void readFile(Object obj) throws IOException{
		DataFile dtf = (DataFile) obj;
		m_dtf.data = dtf.data;
		m_dtf.lastTime = dtf.lastTime;
		m_dtf.size = dtf.size;
		m_dtf.name = dtf.name.replace("C:\\temp\\","C:\\client\\");
		m_dtf.saveFile(m_dtf.name);
	}

	class SendDataThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!isStop) {
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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

	void sendString(String str) {
		System.out.println("SENDING STRING	" + str);
		sendType = SEND_TYPE.SEND_STRING;
		message = str;
	}

	void sendFile(String fileName) {
		System.out.println("SENDING FILE	");
		this.fileName = fileName;
		sendType = SEND_TYPE.SEND_FILE;
	}

	// void send Message
	public synchronized void sendMessage(Object obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			// only send text
			if (obj instanceof String) {
				String message = obj.toString();
				oos.writeObject(message);
				oos.flush();
			}
			// send attach file
			else if (obj instanceof DataFile) {
				oos.writeObject(obj);
				oos.flush();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void connectServerFail() {
		// TODO Auto-generated method stub
		isStop = true;
		closeSocket();
	}

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
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
