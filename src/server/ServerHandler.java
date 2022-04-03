package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import data.DataFile;
import data.SEND_TYPE;

public class ServerHandler extends Thread {

	private Socket socket;
	private boolean isStop = false;

	// Receive
	InputStream is;
	private ISocketServerListener iSocketServerListener;

	// SEND
	OutputStream os;
	SEND_TYPE sendType = SEND_TYPE.DO_NOT_SEND;
	String message;
	String fileName;

	private long fileSize;
	private String fileNameReceived;
	private long currentSize;
	DataFile m_dtf;
	File[] files;

	public ServerHandler(Socket socket, ISocketServerListener iSocketServerListener) throws Exception {
		this.socket = socket;
		os = socket.getOutputStream();
		is = socket.getInputStream();

		this.iSocketServerListener = iSocketServerListener;
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

	void readData() throws Exception {
		try {

			//System.out.println("Receiving...");
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
		iSocketServerListener.showDialog(str, "STRING INFOR");

		if (str.equals("STOP"))
			isStop = true;
		else if (str.equals("VIEW_ALL_FILE")) {
			/*File[] fis = fileWorker.getAllFileName();
			Gson gson = new Gson();
			String ss = gson.toJson(fis, File[].class);
			String data = "ALL_FILE";
			this.sendString(data+ss);*/
			/*for (File file : files) {
				data += "--" + file.toString();
			}*/
			/*if(!Arrays.equals(files, fis)){
				files = fis;
				this.sendString(data+ss);
			}*/

		} else if (str.contains("SEARCH_FILE")) {
			/*String[] searches = str.split("--");

			String[] files = fileWorker.searchFile(searches[1]);
			String data = "ALL_FILE";
			for (String file : files) {
				data += "--" + file;
			}
			this.sendString(data);*/
		} else if (str.contains("DOWNLOAD_FILE")) {
			String[] array = str.split("--");
			sendFile(array[1]);
		}else if (str.contains("DELETE_FILE")) {
			str = str.replace("DELETE_FILE", "");
			FileWorker.deleteFile(str.replace("C:\\client\\","C:\\temp\\"));
		}
		else if (str.contains("START_SEND_FILE")) {
			this.sendType = SEND_TYPE.START_SEND_FILE;
		} else if (str.contains("SEND_FILE")) {
			String[] fileInfor = str.split("--");
			System.out.println(fileInfor[1]);
			fileNameReceived = fileInfor[1].replace("C:\\client\\","C:\\temp\\");
			fileSize = Integer.parseInt(fileInfor[2]);
			System.out.println("File Size: " + fileSize);
			currentSize = 0;
			m_dtf.clear();
			if (!new File(fileNameReceived).exists())
				this.sendString("START_SEND_FILE");
			/*else
				this.sendString("ERROR--FILE");*/
		} else if (str.contains("END_FILE")) {
			m_dtf.saveFile(fileNameReceived);
		}
		else if (str.contains("CREATE_FOLDER")) {
			str = str.replace("CREATE_FOLDER", "");
			FileWorker.createFolder(str.replace("C:\\client\\","C:\\temp\\"));
		}

		return str;
	}

	void readFile(Object obj) {
		DataFile dtf = (DataFile) obj;
		int percent = (int) (fileSize);
		m_dtf.data = dtf.data;
		iSocketServerListener.showProgessBarPercent(percent);
		/*DataFile dtf = (DataFile) obj;
		currentSize += 1024;

		int percent = (int) (currentSize * 100 / fileSize);
		m_dtf.appendByte(dtf.data);
		iSocketServerListener.showProgessBarPercent(percent);*/
	}

	class SendDataThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!isStop) {
				try {
					Thread.sleep(100);
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
			File source = new File(fileName);
			InputStream fin;
			try {
				fin = new FileInputStream(source);
				long lenghtOfFile = source.length();
				// Send message : fileName + size
				sendMessage("SEND_FILE" + "--" + fileName + "--" + lenghtOfFile);
				fin.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (sendType == SEND_TYPE.START_SEND_FILE) {
			/*File source = new File(FileWorker.URL_FOLDER + "\\" + fileName);
			InputStream fin;
			long lenghtOfFile = source.length();
			byte[] buf = new byte[1024];
			long total = 0;
			int len;
			try {
				fin = new FileInputStream(source);
				while ((len = fin.read(buf)) != -1) {
					total += len;
					DataFile dtf = new DataFile();
					dtf.data = buf;
					sendMessage(dtf);
					iSocketServerListener.showProgessBarPercent(total * 100 / lenghtOfFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);*/
			File myFile = new File(fileName);
			Path path = Paths.get(myFile.getPath());

			long lenghtOfFile = myFile.length();

			DataFile dtf = new DataFile();
			dtf.data = Files.readAllBytes(path);
			sendMessage(dtf);
			iSocketServerListener.showProgessBarPercent((int) (lenghtOfFile));

			sendMessage("END_FILE--" + fileName + "--" + lenghtOfFile);

		}
		sendType = SEND_TYPE.DO_NOT_SEND;
		/*File[] fis = fileWorker.getAllFileName();
		Gson gson = new Gson();
		String ss = gson.toJson(fis, File[].class);
		String data = "ALL_FILE";
		this.sendString(data+ss);*/
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
			iSocketServerListener.showDialog("Closed Server Socket", "INFOR");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			//iSocketServerListener.showDialog("Connection Failed", "ERROR");
		}
	}

}
