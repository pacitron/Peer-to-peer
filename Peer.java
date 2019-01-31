import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Peer {
    private static InetAddress ip;
    private static int peerId;
    private static int searchId;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static HashMap<Integer, Socket> connections = new HashMap<>();
    private static HashMap<Integer, Long> pingMessageMap = new HashMap<>();
    long messageID = 0;


    public static void main(String[] args) throws IOException {
        //String hostname;
        ServerSocket ss = null;
        try {
            ip = InetAddress.getLocalHost();
            // hostname = ip.getHostName();
            System.out.println("\tYour current IP address : " + ip);
            // System.out.println("Your current Hostname : " + hostname);
            Scanner scanner = new Scanner(System.in);
            int socket;
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
            Random rand = new Random();
            peerId = rand.nextInt(65536);

            System.out.println("\tMY PEER ID: " + peerId);
            startServer(ss);
            startClient(socket);
            sendPING();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void sendPING() {
        (new Thread(() -> {
            while (true) {
                try {
                    System.out.println("\tRouting table @ " + dateFormat.format(new Date()) + " : ");
                    if (connections.size() > 0) {
                        for (Map.Entry<Integer, Socket> s : connections.entrySet()) {
                            System.out.println(s.getValue().getInetAddress());
                            PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(s.getValue().getOutputStream()));
                            outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                            outputWriter.flush();
                            pingMessageMap.put(s.getKey(), System.currentTimeMillis());
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

    private static void search() {
        (new Thread(() -> {
            while (true) {

                Random rand = new Random();
                searchId = rand.nextInt(65536);
                
            }
        })).start();
    }

    private static void startClient(int socket) {
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
//                    System.out.println(msg);
                    if (msg.contains("qwerty")) {
                        connections.put(Integer.parseInt(msg.split(",")[1]), s);
                    }
                }
                /*BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                Scanner inputKeyboard = new Scanner(System.in);*/
                PrintWriter outputWriter = new PrintWriter(s.getOutputStream());
                outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                outputWriter.flush();

                System.out.println("\tconnected here");
                startClient(socket);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (pingMessageMap.containsKey(Integer.parseInt(line.split(",")[1]))) {
                        long timeTaken = System.currentTimeMillis() - pingMessageMap.get(Integer.parseInt(line.split(",")[1]));
                        System.err.println("fromPeerID:fromIP:toPeerID:toIP:latency\t" + peerId + ":" + ip.toString().split("/")[1] + ":" + line.split(",")[1] + ":" + line.split(",")[0] + ":" + timeTaken);
                        pingMessageMap.remove(Integer.parseInt(line.split(",")[1]));
                    } else {
                        System.err.println("Got It! ->\t" + line);
                    }
                }
                /*while (true) {
                    try {
                        outputWriter.println(ip.toString().split("/")[1] + "," + peerId);
                        outputWriter.flush();
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("\tpeer not available...");
            }
        })).start();
    }

    private static void startServer(ServerSocket serverSocket) {
        (new Thread(() -> {
            int pid = 0;
            Socket socket = null;
            try {
                System.out.println("\tServer here...");
                socket = serverSocket.accept();
                if (connections.size() < 6) {
                    //send ping response here
                    PrintWriter outputWriter = new PrintWriter(socket.getOutputStream());
                    outputWriter.println(ip.toString().split("/")[1] + "," + peerId + ",qwerty");
                    outputWriter.flush();
                    System.out.println("\t" + socket.getInetAddress() + " has joined");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    startServer(serverSocket);
                    line = in.readLine();
                    pid = Integer.parseInt(line.split(",")[1]);
                    connections.put(pid, socket);
                    while ((line = in.readLine()) != null) {
                        if (pingMessageMap.containsKey(Integer.parseInt(line.split(",")[1]))) {
                            long timeTaken = System.currentTimeMillis() - pingMessageMap.get(Integer.parseInt(line.split(",")[1]));
                            System.err.println("fromPeerID:fromIP:toPeerID:toIP:latency\t" + peerId + ":" + ip.toString().split("/")[1] + ":" + line.split(",")[1] + ":" + line.split(",")[0] + ":" + timeTaken);
                            pingMessageMap.remove(Integer.parseInt(line.split(",")[1]));
                        } else {
                            System.err.println("Got It! ->\t" + line);
                        }
                    }
                } else {
                    socket.close();
                    System.out.println("closed incoming connection as routing table is full");
                }
            } catch (SocketException se) {
                System.out.println("Connection lost with peer " + pid);
                connections.remove(pid);
                try {
                    assert socket != null;
                    socket.close();
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