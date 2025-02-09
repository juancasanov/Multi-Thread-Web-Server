package org.example;

import java.net.*;

public class ServidorWeb {
    public static void main(String[] argv) throws Exception {
        int puerto = 7890;
        ServerSocket serverSocket = new ServerSocket(puerto);

        while (true) {
            System.out.println("Server waiting for connection...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection accepted");

            SolicitudHttp solicitud = new SolicitudHttp(socket);
            Thread hilo = new Thread(solicitud);
            hilo.start();
        }
    }
}
