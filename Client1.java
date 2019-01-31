import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client1 {
    private static InetAddress ip;
    private static int peerId;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static HashMap<Integer, Socket> connections = new HashMap<>();


    public static void main(String[] args) throws IOException {
        //String hostname;
        ServerSocket ss = null;
        int socket=0;
        try {
            ip = InetAddress.getLocalHost();
            //hostname = ip.getHostName();
            System.out.println("\tYour current IP address : " + ip);
            //System.out.println("Your current Hostname : " + hostname);
            Scanner scanner = new Scanner(System.in);
            boolean flag = true;
            do {
                System.out.print("Enter port number: ");
                socket = scanner.nextInt();
                try {
                    ss = new ServerSocket(socket);
                    flag = false;
                } catch (IOException e) {
                    System.out.println("Unable to connect on the given port number. Try again!");
                }

            } while (flag);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        peerId = rand.nextInt(65536);
        System.out.println("\tMY PEER ID: " + peerId);
        startServer(ss);
        startSender(socket);
        sendPING();
    }
//----------------------------------------------------------PING-------------------------------------------------------------------------------
    private static void sendPING() {
        (new Thread(() -> {
            while (true) {
                try {
                    System.out.println("\tRouting table @ " + dateFormat.format(new Date()) + " : ");
                    if (connections.size() > 0) {
                        for (Map.Entry<Integer, Socket> s : connections.entrySet()) {
                            //System.out.println(s.getValue().getInetAddress());
                            PrintWriter outputWriter=new PrintWriter(new OutputStreamWriter(s.getValue().getOutputStream()));
                            //outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                            //outputWriter.flush();
                            System.out.println("\t" + s.getValue().getInetAddress().toString().split("/")[1] + "," + s.getKey());
                        }
                    } else {
                        System.out.println("No records found!");
                    }
                    Thread.sleep(30000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }
//-------------------------------------------------------CLIENT------------------------------------------------------------------------------------
    private static void startSender(int socket) {
        (new Thread(() -> {
            String address;
            try {
                System.out.println("\tEnter peer ip address:");
                Scanner input = new Scanner(System.in);
                address = input.nextLine();
                Socket s = new Socket(address, socket);
                BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String msg = null;
                while (msg == null) {
                    msg = bf.readLine();
                    System.out.println(msg);
                    if (msg.contains("qwerty")) {
                        connections.put(Integer.parseInt(msg.split(",")[1]), s);
                    }
                }
                /*BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                Scanner inputKeyboard = new Scanner(System.in);*/
                PrintWriter outputWriter = new PrintWriter(s.getOutputStream());
                outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                outputWriter.flush();

                System.out.println("\tconnected\n\there");
                while (true) {
                    try {
                        outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                        outputWriter.flush();
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("\tpeer not available...");
            } finally {
                startSender(socket);
            }
        })).start();
    }
//--------------------------------------------------------SERVER----------------------------------------------------------------------------------
    private static void startServer(ServerSocket ss) {
        (new Thread(() -> {
            int pid = 0;
            Socket s = null;
            try {
                System.out.println("\tServer here...");
                s = ss.accept();
                if (connections.size() < 6) {
                    //send ping response here
                    PrintWriter outputWriter = new PrintWriter(s.getOutputStream());
                    outputWriter.println(ip.toString().split("/")[1] + "," + peerId + ",qwerty");
                    outputWriter.flush();
                    System.out.println("\t" + s.getInetAddress() + " has joined");
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String line;
                    startServer(ss);
                    line = in.readLine();
                    pid = Integer.parseInt(line.split(",")[1]);
                    connections.put(pid, s);
                    while ((line = in.readLine()) != null) {
                        System.err.println("Got It! ->\t" + line);
                    }
                } else {
                    s.close();
                    System.out.println("closed incoming connection as routing table is full");
                }
            } catch (SocketException se) {
                System.out.println("Connection lost with peer " + pid);
                connections.remove(pid);
                try {
                    assert s != null;
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Peer " + pid + " removed from routing table");
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
    }
}
