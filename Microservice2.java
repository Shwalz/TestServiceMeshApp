import java.io.*;
import java.net.*;
import java.util.*;

public class Microservice2 {
    private Socket socket;
    private PrintWriter outToBaaS;
    private BufferedReader inFromBaaS;

    public Microservice2(String baasAddress, int baasPort) throws IOException {
        // Establish a persistent connection to BaaS
        socket = new Socket(baasAddress, baasPort);
        // Set a read timeout to match Microservice1's timeout
        socket.setSoTimeout(30000); // Set a read timeout of 30 seconds
        outToBaaS = new PrintWriter(socket.getOutputStream(), true);
        inFromBaaS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void startService(int servicePort) throws IOException {
        // Start listening for client connections
        try (ServerSocket serverSocket = new ServerSocket(servicePort)) {
            System.out.println("Microservice2 is running on port " + servicePort);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received from ApiGateway: " + inputLine); // Logging received message
                        // Processing the input and sending to BaaS with the same logic as Microservice1
                        String outputLine = "Microservice2:" + reverseWords(inputLine);
                        System.out.println("Sending to BaaS: " + outputLine); // Logging sending message to BaaS
                        outToBaaS.println(outputLine);

                        // Reading the response from BaaS
                        String responseFromBaaS = inFromBaaS.readLine();
                        System.out.println("Received from BaaS: " + responseFromBaaS); // Logging response from BaaS
                        System.out.println("Sending to ApiGateway: " + responseFromBaaS); // Logging sending message to ApiGateway
                        out.println(responseFromBaaS); // Sending the response back to ApiGateway
                    }
                } catch (IOException e) {
                    System.out.println("Exception caught when trying to listen on port " + servicePort + " or listening for a connection");
                    System.out.println(e.getMessage());
                }
            }
        } // The ServerSocket will be closed automatically when the try block exits
    }

    private String reverseWords(String inputLine) {
        List<String> words = Arrays.asList(inputLine.split(" "));
        Collections.reverse(words);
        return String.join(" ", words);
    }

    public static void main(String[] args) {
        try {
            Microservice2 service = new Microservice2("localhost", 5678);
            service.startService(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
