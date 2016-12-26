/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lena
 */
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Server {

    private static ArrayList<Message> messages;
    private static int port;
    private static ArrayList<Socket> clients;
    private static ServerSocket serverSocket;
    private static ArrayList<ClientThread> threads;

    public static void main(String[] args) {
        Server server = new Server();
        server.clients = new ArrayList<>();
        server.messages = new ArrayList<>();
        server.threads = new ArrayList<>();
        if(args.length <1) {
            args = new String[1];
            args[0] = "8080";
        }
        if (args.length < 1) {
            System.out.println("bitte Port angeben");

            //SOCKETFOOO!!! ICH HAB KEINE AHNUNG WIE DAS GEHT!!!
        } else {
            try {
                server.port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("bitte Port angeben und kein andren foo");
            }
            try {

                server.serverSocket = new ServerSocket();
                server.serverSocket.bind(new InetSocketAddress(port));
                System.out.println(server.serverSocket.getInetAddress().getHostAddress());

                while (true) {
                    Socket clientSocket = server.serverSocket.accept();
                    System.out.println("blubb");
                    server.clients.add(clientSocket);
                    System.out.println(clientSocket.getInetAddress().getHostAddress());
                    ClientThread t = new ClientThread(server, clientSocket);
                    server.threads.add(t);
                    t.run();
                    
                    
                    
                    //aus inem grudn klappt das multithreading noch nicht so...
                    System.out.println("nachrungehtsnochweiter");
                    PrintWriter out
                            = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("test");
                    
                    
                    
                    //closeAll();
                                      

                }
            } catch (Exception e) {
                System.out.println("Fehler");
                System.out.println(e.getMessage());
            }

        }
    }

    public String getProtokollMessages(ArrayList<Message> pMessages) {
        String e = "";
        e = e + pMessages.size() + System.lineSeparator();
        for (Message m : pMessages) {
            e = e + m.getProtokollString();
        }
        return e;
    }

    public ArrayList<Message> getMessagesSince(long since) {

        ArrayList<Message> result = new ArrayList<>(this.messages.stream().filter(m -> {
            return (m.getTime() >= since);
        }).collect(Collectors.toList()));

        return result;
    }

    public ArrayList<Message> getMessageToTopic(String topic) {
        ArrayList<Message> result = new ArrayList<>(this.messages.stream().filter(m -> {
            return (m.getTopic().equals(topic));
        }).collect(Collectors.toList()));

        //Hier muss noch nach Zeit absteigen sortiert werden..
        return result;
    }

    public ArrayList<Message> getnMessgaesToTopic(String topic, int n) {
        ArrayList<Message> mtt = this.getMessageToTopic(topic);

        // hier muss noch sortiert werden
        if (mtt.size() <= n) {
            return mtt;
        } else {
            ArrayList<Message> e = new ArrayList<Message>();
            for (int i = 0; i <= n; i++) {
                e.add(mtt.get(i));
            }
            return e;
        }
    }

    public ArrayList<String> getTopics() {
        ArrayList<String> topics = new ArrayList<String>(this.messages.stream().map(m -> m.getTopic()).collect(Collectors.toList()));
        ArrayList<String> topicsWithoutDouble = new ArrayList<String>();
        for (String s : topics) {
            if (!topicsWithoutDouble.contains(s)) {
                topicsWithoutDouble.add(s);
            }
        }
        return topicsWithoutDouble;
    }

    public long getTimeLastMessage(String topic) {
        ArrayList<Message> mToTopic = this.getMessageToTopic(topic);
        Message latest = null;
        for (Message m : mToTopic) {
            if (latest == null || latest.getTime() <= m.getTime()) {
                latest = m;
            }
        }
        if (latest != null) {
            return latest.getTime();
        }
        return -1;
    }

    public ArrayList<String> getTimeTopics() {
        ArrayList<String> e = this.getTopics();
        for (String s : e) {
            s = this.getTimeLastMessage(s) + " " + s;
        }
        return e;
    }

    public String getTimeTopicsProtokoll() {
        String e = "";
        ArrayList<String> tt = this.getTimeTopics();
        e = e + tt.size();
        for (String s : tt) {
            e = e + System.lineSeparator() + s;
        }

        //SORTIERuNG muss noch
        return e;
    }
    
    public void addMessage(Message pMessage){
        messages.add(pMessage);
        System.out.println(pMessage.getProtokollString());
    }

    public static void closeAll() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("Fehler beim close des Serversockets");
            System.out.println(e.getMessage());
        }

        try {
            for (Socket clientSocket : clients) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.out.println("Fehler beim close der ClientSockets");
            System.out.println(e.getMessage());
        }
    }

}
