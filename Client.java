import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java. net. InetAddress;

public class Client {
    static InetAddress ip;
    static int PeerId;
    public static void main(String[] args) throws IOException {
        //String hostname;
        ServerSocket ss=null;
        try {
            ip = InetAddress.getLocalHost();
            //hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            //System.out.println("Your current Hostname : " + hostname);
            ss = new ServerSocket(6012);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        PeerId = rand.nextInt(50) + 1;
        startServer(ss);
        startSender();
    }

    public static void startSender() {
        (new Thread(() -> {
            String address;
            try {
                System.out.println("Enter peer ip address:");
                Scanner input=new Scanner(System.in);
                address=input.nextLine();
                Socket s = new Socket(address, 6012);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                Scanner inputKeyboard = new Scanner(System.in);
                PrintWriter outputWriter = new PrintWriter(s.getOutputStream());
                outputWriter.println(ip.toString().split("/")[1]+","+PeerId);
                outputWriter.flush();
                System.out.println("connected");
                startSender();
                System.out.println("here");
                while (true) {
                    outputWriter.println(ip.toString().split("/")[1]+","+PeerId);
                    outputWriter.flush();
                    Thread.sleep(30000);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("peer not available...");
                startSender();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })).start();
    }

    public static void startServer(ServerSocket ss) {
        (new Thread(() -> {

                try {
                    System.out.println("Server here...");
                    Socket s = ss.accept();
                    System.out.println(s.getInetAddress() + " has joined");
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line = null;
                    startServer(ss);
                    while ((line = in.readLine()) != null) {
                        System.err.println("Got It! ->\t" + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

        })).start();
    }
}