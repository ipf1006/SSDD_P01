package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz para el servidor de chat. Define la firma de los métodos de
 * arranque, multidifusión, eliminación de cliente y apagado.
 * 
 * @author Ignacio Puebla
 */
public interface ChatServer {

	/**
	 * Inicia el servidor y lo deja a la espera de conexiones de clientes.
	 */
	void startup();

	/**
	 * Detiene el servidor y cierra todas las conexiones activas.
	 */
	void shutdown();

	/**
	 * Envía un mensaje a todos los clientes conectados.
	 * 
	 * @param message Mensaje a difundir.
	 */
	void broadcast(ChatMessage message);

	/**
	 * Elimina un cliente de la lista de clientes conectados.
	 * 
	 * @param id Identificador del cliente a eliminar.
	 */
	void remove(int id);
}
