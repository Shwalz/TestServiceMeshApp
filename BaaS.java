import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BaaS {
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, String> sessionData = new ConcurrentHashMap<>();
    private AtomicInteger clientCounter = new AtomicInteger(0);

    public BaaS(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("BaaS is running on port " + serverSocket.getLocalPort());

        while (true) {
            Socket socket = serverSocket.accept();
            int clientId = clientCounter.incrementAndGet();

            CompletableFuture.runAsync(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received from microservice " + clientId + ": " + inputLine); // Logging input

                        // Обработка сообщения от микросервиса
                        String[] parts = inputLine.split(":", 2);
                        if (parts.length == 2) {
                            String serviceId = parts[0];
                            String message = parts[1];
                            System.out.println("Service ID: " + serviceId + " Message: " + message); // Logging parts

                            // Сохраняем или обновляем данные сессии
                            sessionData.put(serviceId, message);
                            System.out.println("Current Session Data: " + sessionData); // Logging session data

                            // Симуляция отправки ответа микросервису
                            String response = String.join(" ", sessionData.values());
                            System.out.println("Sending response to microservice " + clientId + ": " + response); // Logging response
                            out.println(response);

                            sessionData.remove(serviceId);
                        } else {
                            System.out.println("Invalid message format received: " + inputLine); // Logging invalid format
                            out.println("Invalid message format");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Exception caught when trying to read or write data: " + e.getMessage());
                } finally {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        System.out.println("Exception caught when trying to close socket: " + ex.getMessage());
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        try {
            BaaS baas = new BaaS(5678);
            baas.start();
        } catch (IOException e) {
            System.out.println("Could not start BaaS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
