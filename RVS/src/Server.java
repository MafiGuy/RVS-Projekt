/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lena
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.*;

public class Server {

    //Hauptklasse
    //Hier werden die Nachrichten, die Clients und die Threads verwaltet
    private volatile static Semaphore sem;
    //Semaphore, um zu sichern dass kritische Datenstrukturen nur von einem Thread gleichzeitig benutzt werdden

    private volatile static ArrayList<Message> messages;

    //port
    private static int port;

    private volatile static ArrayList<Socket> clients;
    private static ServerSocket serverSocket;
    private volatile static ArrayList<ClientThread> threads;

    public static void main(String[] args) {
        //main Methode erstellt sich erstmal ein server-objekt
        Server server = new Server();

        //initialisierung der Datenstrukturen
        server.clients = new ArrayList<>();
        server.messages = new ArrayList<>();
        server.threads = new ArrayList<>();
        server.sem = new Semaphore(1);

        //wenn kein Port angegeben, wird versucht der Port 8080 zu nehmen
        if (args.length < 1) {
            args = new String[1];
            args[0] = "8080";
            System.out.println("Da kein Port angegeben wurde, wird 8080 als Port genutzt");
        }

        try {
            server.port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //wenn ein Port übergeben dieser aber Fehlerhaft ist, wird Port 8080 genutzt
            System.out.println("Da fehlerhafter Port übergeben, wird 8080 als Port genutzt");
            port = 8080;
        }

        try {

            //erstellen und binden des ServerSockets
            server.serverSocket = new ServerSocket();
            server.serverSocket.bind(new InetSocketAddress(port));
            System.out.println(server.serverSocket.getInetAddress().getHostAddress());

            //Hauptschleife
            while (true) {
                //Serversocket wartet auf anfragen und erstellt, wenn da, sowohl Serversocket als auch Thread
                Socket clientSocket = server.serverSocket.accept();
                server.clients.add(clientSocket);
                ClientThread t = new ClientThread(sem, server, clientSocket);
                System.out.println("neuen Client registriert");
                server.threads.add(t);
                //starten des Threads
                t.start();

            }
        } catch (IOException e) {
            System.out.println("folgender Fehler ist aufgetreten:");
            System.out.println(e.getMessage());
            System.out.println("Programm muss neu gestartet werden");

        }

    }

    //Nachrichtenverwaltung ab hier
    //gibt die Nachrichten seit dem Zeitpunkt since als UTS zurück
    public ArrayList<Message> getMessagesSince(long since) {

        ArrayList<Message> result;
        result = new ArrayList<>(
                this.messages.stream().filter(m -> {
                    return (m.getTime() >= since); //filtern der Nachrichten nach der Zeit (größer als die übergabe)
                }).collect(Collectors.toList()));

        return result;
    }

    //gibt die Nachrichten mit dem übergebenen Thema zurück
    public ArrayList<Message> getMessageToTopic(String topic) {
        ArrayList<Message> result = new ArrayList<>(
                this.messages.stream().filter(m -> {
                    return (m.getTopic().equals(topic)); //filtern der Nachrichten nach Thema (gleich dem Argument)
                }).collect(Collectors.toList()));

        //Hier muss noch nach Zeit absteigen sortiert werden..
        return result;
    }

    //gibt n Nachrichten zu dem Thema zurück
    public ArrayList<Message> getnMessgaesToTopic(String topic, int n) {
        ArrayList<Message> mtt = this.getMessageToTopic(topic);

        // hier muss noch sortiert werden
        if (mtt.size() <= n) {
            return mtt;
            //wenn weniger Nachrichte vorhanden sind als n angegeben, werden alle zurückgegeben
        } else {
            ArrayList<Message> e = new ArrayList<>();
            for (int i = 0; i <= n; i++) { //nehme n Nacherichten 
                e.add(mtt.get(i));
            }
            return e;
        }
    }

    //gibt alle vorhandenen Themen wieder
    public ArrayList<String> getTopics() {
        ArrayList<String> topics;
        topics = new ArrayList<>(
                this.messages.stream().map(m
                        -> //mapen der nachrichtenn auf den dazugehörenden Themen
                        m.getTopic())
                .collect(Collectors.toList()));

        ArrayList<String> topicsWithoutDouble = new ArrayList<String>();

        for (String s : topics) { //Ohne doppelte Themen
            if (!topicsWithoutDouble.contains(s)) {
                topicsWithoutDouble.add(s);
            }
        }
        return topicsWithoutDouble;
    }

    //gibt zu dem übergegeben thema die Zeit der letzten Nachricht zurück
    public long getTimeLastMessage(String topic) {

        //alle Nachrichten zu dem Thema
        ArrayList<Message> mToTopic = this.getMessageToTopic(topic);

        Message latest = null;
        //heraussuchen der letzten nachricht
        for (Message m : mToTopic) {
            if (latest == null || latest.getTime() <= m.getTime()) {
                latest = m;
            }
        }

        //wenn Nachricht vorhanden, dann Rückgabe der Zeit dieser
        if (latest != null) {
            return latest.getTime();
        }

        //ansonsten, falls keine Nachricht vorhanden, gebe -1 als Fehlerangabe zurück
        return -1;
    }

    //gibt alle Kombination zu Themenn und deren änbderungszeitpunkten zurück
    public ArrayList<TopicTime> getTopicTime() {
        ArrayList<TopicTime> e = new ArrayList<>();
        for (Message m : messages) {
            TopicTime x = new TopicTime(m.getTopic(), m.getTime());
            e.add(x);
        }
        // hier noch sortieren!       
        // neueste topics zu erst undso
        return e;
    }

    //gibt die n neuesten Zeit-Themen kombination zurück
    public ArrayList<TopicTime> getnTopicTime(int n) {
        ArrayList<TopicTime> e = getTopicTime();
        if (e.size() > n) {
            for (int i = n; i < e.size(); i++) {
                e.remove(i);
            }
        }
        return e;
    }

    //hinzufügen einer neuen Nachricht
    public void addMessage(Message pMessage) {
        if (pMessage != null) {
            messages.add(pMessage);
        }
    }

    
    //methode um alle Sockets zu schließen
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

    public void sendToAll(ArrayList<Message> messages) {
        for (ClientThread thread : threads) {
            thread.sendNew(messages);
        }
    }

}
