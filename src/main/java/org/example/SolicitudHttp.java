package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class SolicitudHttp implements Runnable {

    final static String CRLF = "\r\n";
    Socket socket;

    public SolicitudHttp(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proceseSolicitud() throws Exception {
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        ); //Lo que envia el navegador al servidor

        String linea = "";
        String method = "";
        String nombreArchivo = "";
        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            StringTokenizer partesLinea = new StringTokenizer(linea);
            method = partesLinea.nextToken();

            if ((method.equals("GET"))) {
                nombreArchivo = partesLinea.nextToken();
                nombreArchivo = "." + nombreArchivo;
                break;
            }
        }


        InputStream inputStream = ClassLoader.getSystemResourceAsStream(nombreArchivo);
        String lineaDeEstado = null;
        String lineaHeader = null;

        if (inputStream != null && !nombreArchivo.equals("./")) {
            File file = new File(ClassLoader.getSystemResource(nombreArchivo).toURI());
            long filesize = file.length();
            lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            lineaHeader = "Content-type: " + contentType(nombreArchivo) + CRLF +
                    "Content-Length: " + filesize + CRLF +
                    "Connection: close" + CRLF + CRLF;

            enviarString("HTTP/1.0 200 OK" + CRLF, out);
            enviarString("Content-type: " + contentType(nombreArchivo) + CRLF, out);
            enviarString("Content-Length: " + filesize + CRLF , out);
            enviarString("Connection: close" + CRLF, out);
            enviarString(CRLF, out);
            enviarBytes(inputStream, out);
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream("./404.html");
            File file = new File(ClassLoader.getSystemResource("./404.html").toURI());
            long filesize = file.length();

            enviarString("HTTP/1.0 404 NOT FOUND" + CRLF, out);
            enviarString("Content-type: text/html" + CRLF, out);
            enviarString("Content-Length: " + filesize + CRLF , out);
            enviarString("Connection: close" + CRLF, out);
            enviarString(CRLF, out);
            enviarBytes(inputStream, out);
        }

        out.flush();
        out.close();
        in.close();
        socket.close();
    }

    private void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes= 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String nombreArchivo) {
        if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }

        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }

        if (nombreArchivo.endsWith(".jpg")) {
            return "image/jpg";
        }

        return "application/octet-stream";
    }
}
