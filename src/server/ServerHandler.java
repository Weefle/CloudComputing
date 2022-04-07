package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import data.DataFile;
import data.SEND_TYPE;

public class ServerHandler extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// Receive
	InputStream is;

	// SEND
	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	DataFile m_dtf;

	public ServerHandler(Socket socket) throws Exception {
		this.socket = socket;
		os = socket.getOutputStream();
		is = socket.getInputStream();

		SendDataThread sendDataThread = new SendDataThread();
		sendDataThread.start();

		m_dtf = new DataFile();
	}

	@Override
	public void run() {
		System.out.println("Processing: " + socket);
		// TODO Auto-generated method stub
		while (!isStop) {
			try {
				readData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				connectClientFail();
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Complete processing: " + socket);

		closeSocket();

	}

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
			// TODO: handle exception
			e.printStackTrace();
			connectClientFail();
			closeSocket();
		}
	}

	public String readString(Object obj) throws IOException {
		String str = obj.toString();

		if (str.equals("STOP"))
			isStop = true;
		/*else if (str.equals("VIEW_ALL_FILE")) {
			File[] fis = fileWorker.getAllFileName();
			Gson gson = new Gson();
			String ss = gson.toJson(fis, File[].class);
			String data = "ALL_FILE";
			this.sendString(data+ss);
			for (File file : files) {
				data += "--" + file.toString();
			}
			if(!Arrays.equals(files, fis)){
				files = fis;
				this.sendString(data+ss);
			}

		} */ else if (str.contains("DOWNLOAD_FILE")) {
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

	void readFile(Object obj) throws IOException, FileSystemException {
		DataFile dtf = (DataFile) obj;
		m_dtf.data = dtf.data;
		m_dtf.name = dtf.name.replace("C:\\client\\","C:\\temp\\");
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
		}  else if (sendType == SEND_TYPE.SEND_FILE) {

			Path path = Paths.get(fileName);
			DataFile dtf = new DataFile();
			dtf.data = Files.readAllBytes(path);
			dtf.name = fileName;

			dtf.data = Files.readAllBytes(path);

			sendMessage(dtf);

		}
		sendType = SEND_TYPE.DO_NOT_SEND;

	}

	void sendString(String str) {
		System.out.println("SENDING STRING	");
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
		send(obj, os);
	}

	public static void send(Object obj, OutputStream os) {
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

	private void connectClientFail() {
		// TODO Auto-generated method stub
		isStop = true;
		closeSocket();
	}

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
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
