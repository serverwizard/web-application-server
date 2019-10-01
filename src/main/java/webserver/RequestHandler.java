package webserver;

import db.DataBase;
import dto.HttpRequestDto;
import enums.RequestMethod;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
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

            HttpRequestDto httpRequestDto = new HttpRequestDto(br.readLine());

            Map<String, HttpRequestUtils.Pair> headers = new HashMap<>();
            String line = br.readLine();
            while (!line.isEmpty()) {
                HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                headers.put(pair.getKey(), pair);

                line = br.readLine();
            }

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body;

            if (httpRequestDto.matchBy(RequestMethod.GET, "/index.html")) {
                body = Files.readAllBytes(new File("./webapp" + httpRequestDto.getRequestPath()).toPath());
            } else if (httpRequestDto.matchBy(RequestMethod.GET, "/user/form.html")) {
                body = Files.readAllBytes(new File("./webapp" + httpRequestDto.getRequestPath()).toPath());
            } else if (httpRequestDto.matchBy(RequestMethod.POST, "/user/create")) {
                char[] cbuf = new char[Integer.parseInt(headers.get("Content-Length").getValue())];
                br.read(cbuf);

                Map<String, String> paramPairs = HttpRequestUtils.parseQueryString(String.valueOf(cbuf));
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
