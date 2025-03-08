package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementación del servidor de chat.
 * 
 * @author Ignacio Puebla
 */
public class ChatServerImpl implements ChatServer {
	private static final int DEFAULT_PORT = 1500;
	private int clientId = 0;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private int port;
	private boolean alive = true;
	private List<ServerThreadForClient> clients;

	/**
	 * Constructor del servidor de chat.
	 * 
	 * @param port Puerto en el que se ejecuta el servidor.
	 */
	public ChatServerImpl(int port) {
		this.port = port;
		clients = new ArrayList<>();
	}

	@Override
	public void startup() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Servidor iniciado en el puerto " + port);
			while (alive) {
				Socket socket = serverSocket.accept();
				ServerThreadForClient clientThread = new ServerThreadForClient(socket, clientId++);

				// Lee el primer mensaje dentro del constructor del cliente (solo contiene el
				// nombre del usuario)
				clientThread.readInitialMessage();

				clients.add(clientThread);
				System.out.println("[" + sdf.format(new Date()) + "] Nuevo cliente conectado: "
						+ clientThread.getUsername() + " desde " + socket.getInetAddress() + ":" + socket.getPort());
				System.out.println("Total de clientes conectados: " + clients.size());
				new Thread(clientThread).start();
			}
		} catch (IOException e) {
			System.err.println("Error en el servidor: " + e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		alive = false;
		for (ServerThreadForClient client : clients) {
			client.closeConnection();
		}
		clients.clear();
		System.out.println("Servidor apagado.");
	}

	@Override
	public void broadcast(ChatMessage message) {
		for (ServerThreadForClient client : clients) {
			client.sendMessage(message);
		}
	}

	@Override
	public void remove(int id) {
		clients.removeIf(client -> client.getId() == id);
		System.out.println("Cliente " + id + " eliminado.");
	}

	public static void main(String[] args) {
		ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
		server.startup();
	}

	/**
	 * Clase interna que maneja cada cliente conectado al servidor.
	 */
	private class ServerThreadForClient implements Runnable {
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;
		private int id;
		private String username;

		public ServerThreadForClient(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
			try {
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				System.err.println("Error al crear flujos para el cliente: " + e.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					ChatMessage message = (ChatMessage) inputStream.readObject();

					String logMessage = "[" + sdf.format(new Date()) + "] <Ignacio Puebla> patrocina el mensaje: "
							+ message.getMessage();
					System.out.println(logMessage);

					if (message.getType() == MessageType.LOGOUT) {
						break;
					}
					broadcast(message);
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
			} finally {
				closeConnection();
				remove(id);
			}
		}

		public void sendMessage(ChatMessage message) {
			try {
				outputStream.writeObject(message);
				outputStream.flush();
			} catch (IOException e) {
				System.err.println("Error al enviar mensaje a cliente: " + e.getMessage());
			}
		}

		public void closeConnection() {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Error al cerrar conexión del cliente: " + e.getMessage());
			}
		}

		public int getId() {
			return id;
		}

		public String getUsername() {
			return username;
		}

		public void readInitialMessage() {
			try {
				ChatMessage initialMessage = (ChatMessage) inputStream.readObject();
				this.username = initialMessage.getMessage(); // Usa el campo message para el nombre del usuario
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Error al recibir el nombre del usuario: " + e.getMessage());
				this.username = "Desconocido";
			}
		}

	}
}
