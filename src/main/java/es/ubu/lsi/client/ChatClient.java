package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz para el cliente de chat. Define la firma de los métodos de envío de
 * mensajes, desconexión y arranque.
 * 
 * @author Ignacio Puebla
 */
public interface ChatClient {
	/**
	 * Inicia la conexión del cliente con el servidor.
	 * 
	 * @return true si la conexión es exitosa, false en caso contrario.
	 */
	boolean start();

	/**
	 * Envía un mensaje al servidor.
	 * 
	 * @param msg Mensaje a enviar.
	 */
	void sendMessage(ChatMessage msg);

	/**
	 * Desconecta al cliente del servidor.
	 */
	void disconnect();
}
