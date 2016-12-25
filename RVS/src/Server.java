/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lena
 */
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Server {

    private static ArrayList<Message> messages;
    private static int port;
    private static ArrayList<Socket> clients;
    

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("bitte Port angeben");
            
            //SOCKETFOOO!!! ICH HAB KEINE AHNUNG WIE DAS GEHT!!!
        } else {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("bitte Port angeben und kein andren foo");
            }
            try {

                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println(serverSocket.getInetAddress().getHostAddress());

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    

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
            return (m.getTime() > since);
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

}
