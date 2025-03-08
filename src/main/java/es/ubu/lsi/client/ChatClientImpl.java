package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;

/**
 * Implementación del cliente de chat.
 * 
 * @author Ignacio Puebla
 */
public class ChatClientImpl implements ChatClient {
	private String server;
	private String username;
	private static final int PORT = 1500; // Puerto fijo;
	private boolean carryOn = true;
	private int id;
	private Socket socket;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private Set<String> blockedUsers = new HashSet<>(); // Lista de usuarios bloqueados

	/**
	 * Constructor del cliente de chat.
	 * 
	 * @param server   Dirección del servidor.
	 * @param username Apodo del usuario.
	 */
	public ChatClientImpl(String server, String username) {
		this.server = server;
		this.username = username;
	}

	@Override
	public boolean start() {
		try {
			socket = new Socket(server, PORT);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			System.out.println("Conectado al servidor en " + server + ":" + PORT);

			// Envia el nombre de usuario inmediatamente después de conectarse
			outputStream.writeObject(new ChatMessage(-1, MessageType.MESSAGE, username));
			outputStream.flush();

			new Thread(new ChatClientListener()).start();
			return true;
		} catch (IOException e) {
			System.err.println("Error al conectar con el servidor: " + e.getMessage());
			return false;
		}
	}

	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			outputStream.writeObject(msg);
			outputStream.flush();
		} catch (IOException e) {
			System.err.println("Error al enviar mensaje: " + e.getMessage());
		}
	}

	@Override
	public void disconnect() {
		carryOn = false;
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Error al cerrar conexión: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Ingrese el servidor (IP o nombre de máquina, por defecto localhost): ");
			String server = scanner.nextLine().trim();
			if (server.isEmpty()) {
				server = "localhost";
			}
			System.out.print("Ingrese su nombre de usuario: ");
			String username = scanner.nextLine().trim();
			if (username.isEmpty()) {
				System.out.println("El nombre de usuario no puede estar vacío.");
				return;
			}

			ChatClientImpl client = new ChatClientImpl(server, username);
			if (!client.start()) {
				return; // El programa finaliza inmediatamente, evitando que el cliente continúe
						// ejecutándose sin conexión.
			}

			while (client.carryOn) {
				String message = scanner.nextLine();
				if (message.equalsIgnoreCase("logout")) {
					client.sendMessage(new ChatMessage(client.id, MessageType.LOGOUT, ""));
					client.disconnect();
					break;
				} else if (message.startsWith("ban ")) {
					String userToBan = message.substring(4).trim();
					client.blockedUsers.add(userToBan);
					client.sendMessage(new ChatMessage(client.id, MessageType.MESSAGE,
							client.username + " ha baneado a " + userToBan));
					System.out.println("Has baneado a " + userToBan);
				} else if (message.startsWith("unban ")) {
					String userToUnban = message.substring(6).trim();
					client.blockedUsers.remove(userToUnban);
					client.sendMessage(new ChatMessage(client.id, MessageType.MESSAGE,
							client.username + " ha desbloqueado a " + userToUnban));
					System.out.println("Has desbloqueado a " + userToUnban);
				} else {
					client.sendMessage(
							new ChatMessage(client.id, MessageType.MESSAGE, "[" + client.username + "]: " + message));
				}
			}
		}
	}

	/**
	 * Clase interna para escuchar mensajes del servidor.
	 */
	private class ChatClientListener implements Runnable {
		@Override
		public void run() {
			try {
				while (carryOn) {
					ChatMessage msg = (ChatMessage) inputStream.readObject();
					String timestamp = sdf.format(new Date());
					// La primera palabra del mensaje es el nombre del usuario
					String sender = msg.getMessage().split(" ")[0].replace("[", "").replace("]:", "");
					if (!blockedUsers.contains(sender)) {
						System.out.println("[" + timestamp + "] " + msg.getMessage());
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Sesión cerrada: (" + e.getMessage() + ")");
			}
		}
	}
}
