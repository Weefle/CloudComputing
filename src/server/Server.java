package server;

import client.ClientSocketThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

public class Server implements ISocketServerListener {
	public static final int NUM_OF_THREAD = 10;
	public final static int SERVER_PORT = 10;
	public static ServerHandler handler;

	public static void main(String[] args) throws IOException {
		Server server = new Server();
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
		ServerSocket serverSocket = null;
		try {
			System.out.println("Binding to port " + SERVER_PORT + ", please wait  ...");
			serverSocket = new ServerSocket(SERVER_PORT);
			Path dir = Paths.get("C:\\temp");
			new Thread(new WatchDir(dir)).start();
			//new Thread(new MainWatch()).start();
			System.out.println("Server started: " + serverSocket);
			System.out.println("Waiting for a client ...");
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					System.out.println("Client accepted: " + socket);

					handler = new ServerHandler(socket, server);
					executor.execute(handler);
				} catch (IOException e) {
					System.err.println(" Connection Error: " + e);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}
		}
	}

	@Override
	public void connectFail() {
		// TODO Auto-generated method stub
		System.out.println("Connect to client fail, client was disconnected.");
	}

	@Override
	public void showProgessBarPercent(long i) {
		// TODO Auto-generated method stub
		System.out.println("Sent: " + i + "%");
	}

	@Override
	public void showDialog(String message, String type) {
		System.out.println(type + " : " + message);
	}
}
