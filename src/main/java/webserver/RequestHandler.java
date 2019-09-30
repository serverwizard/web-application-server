package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             OutputStream out = connection.getOutputStream()) {

            String requestPath = null;
            String params = null;

            String firstLine = br.readLine();
            String uri = firstLine.split(" ")[1];

            int index = uri.indexOf("?");
            if (index != -1) {
                requestPath = uri.substring(0, index);
                params = uri.substring(index + 1);
            } else {
                requestPath = uri;
            }

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body;

            if (requestPath.equals("/index.html")) {
                body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            } else if (requestPath.equals("/user/form.html")) {
                body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            } else if (requestPath.startsWith("/user/create")) {
                Map<String, String> paramPairs = HttpRequestUtils.parseQueryString(params);

                DataBase.addUser(new User(paramPairs.get("userId"),
                        paramPairs.get("password"),
                        paramPairs.get("name"),
                        paramPairs.get("email")));

                body = "User Create Success".getBytes();
            } else {
                body = "Hello World".getBytes();
            }
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos,
                                   int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos,
                              byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
