import java.io.*;
import java.net.*;

public class ApiGateway {
    private ServerSocket serverSocket;
    private Socket microservice1Socket;
    private Socket microservice2Socket;
    private BufferedReader inFromMicroservice1;
    private BufferedReader inFromMicroservice2;
    private PrintWriter outToMicroservice1;
    private PrintWriter outToMicroservice2;

    public ApiGateway(int port, String microservice1Host, int microservice1Port,
                      String microservice2Host, int microservice2Port) throws IOException {
        serverSocket = new ServerSocket(port);

        // Устанавливаем соединение с микросервисами
        microservice1Socket = new Socket(microservice1Host, microservice1Port);
        microservice2Socket = new Socket(microservice2Host, microservice2Port);

        inFromMicroservice1 = new BufferedReader(new InputStreamReader(microservice1Socket.getInputStream()));
        inFromMicroservice2 = new BufferedReader(new InputStreamReader(microservice2Socket.getInputStream()));
        outToMicroservice1 = new PrintWriter(microservice1Socket.getOutputStream(), true);
        outToMicroservice2 = new PrintWriter(microservice2Socket.getOutputStream(), true);
    }

    public void start() {
        System.out.println("API Gateway is running on port " + serverSocket.getLocalPort());
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String inputLine;
                while ((inputLine = inFromClient.readLine()) != null) {
                    System.out.println("Received from CLI: " + inputLine); // Logging received message
                    if (inputLine.startsWith("Send_service1")) {
                        outToMicroservice1.println(inputLine.substring("Send_service1".length()).trim());
                        String response = inFromMicroservice1.readLine();
                        System.out.println("Received from Microservice1: " + response); // Logging response from Microservice1
                        outToClient.println(response);
                    } else if (inputLine.startsWith("Send_service2")) {
                        outToMicroservice2.println(inputLine.substring("Send_service2".length()).trim());
                        String response = inFromMicroservice2.readLine();
                        System.out.println("Received from Microservice2: " + response); // Logging response from Microservice2
                        outToClient.println(response);
                    } else if (inputLine.equalsIgnoreCase("Exit")) {
                        System.out.println("Exiting API Gateway...");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception in API Gateway: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Close connections to microservices
                closeConnection(microservice1Socket);
                closeConnection(microservice2Socket);
            }
        }
    }

    private void closeConnection(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ApiGateway gateway = new ApiGateway(8000, "localhost", 1235, "localhost", 1234);
            gateway.start();
        } catch (IOException e) {
            System.out.println("Could not start API Gateway: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
