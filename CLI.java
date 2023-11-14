import java.io.*;
import java.net.*;
import java.util.Scanner;

public class CLI {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public CLI(String gatewayAddress, int gatewayPort) throws IOException {
        socket = new Socket(gatewayAddress, gatewayPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            System.out.println("Enter command (Send_service1, Send_service2, Exit):");
            while (!(input = scanner.nextLine()).equalsIgnoreCase("Exit")) {
                out.println(input); // Отправляем команду на сервер
                System.out.println("Server response: " + in.readLine()); // Читаем ответ от сервера и выводим его на консоль
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            CLI cli = new CLI("localhost", 8000);
            cli.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
