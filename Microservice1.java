import java.io.*;
import java.net.*;

public class Microservice1 {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter outToBaaS;
    private BufferedReader inFromBaaS;

    public Microservice1(String baasAddress, int baasPort) throws IOException {
        socket = new Socket(baasAddress, baasPort);
        outToBaaS = new PrintWriter(socket.getOutputStream(), true);
        inFromBaaS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void startService(int servicePort) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {
            System.out.println("Microservice1 is running on port " + servicePort);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        // Process the input and send to BaaS
                        System.out.println("Received from ApiGateway: " + inputLine); // Logging received message
                        String outputLine = "Microservice1:" + inputLine.toUpperCase();
                        System.out.println("Sending to BaaS: " + outputLine); // Logging sending message to BaaS
                        outToBaaS.println(outputLine);

                        // Wait for the response from BaaS
                        String responseFromBaaS = inFromBaaS.readLine();
                        System.out.println("Received from BaaS: " + responseFromBaaS); // Logging response from BaaS
                        if (responseFromBaaS != null) {
                            System.out.println("Sending to ApiGateway: " + responseFromBaaS); // Logging sending message to ApiGateway
                            out.println(responseFromBaaS);
                        } else {
                            out.println("BaaS is not responding");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Exception caught when trying to listen on port " + servicePort + " or listening for a connection");
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Microservice1 service = new Microservice1("localhost", 5678);
            service.startService(1235);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}