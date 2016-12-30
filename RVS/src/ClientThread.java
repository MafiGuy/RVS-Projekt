/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lena
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ClientThread extends Thread {

    private volatile Semaphore sem;
    private boolean _terminate;
    private Socket clientSocket;
    private Server server;

    private PrintWriter out;
    private BufferedReader in;

    public ClientThread(Semaphore pSem, Server pServer, Socket pClientSocket) {
        sem = pSem;
        clientSocket = pClientSocket;
        server = pServer;
        _terminate = false;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void terminate() {
        _terminate = true;
    }

    @Override
    public void run() {

        //Hauptschleife des Threads
        while (!_terminate) {
            try {
                //System.out.println(out);
                String inputString = in.readLine();
                if (inputString != null) {

                    //aufteilen des inputs in den ersten Buchstaben (Befehlbuchstaben)
                    // und rest, der ggf als argument o.ä. dient
                    String rest = inputString.length() > 1 ? inputString.substring(1) : "";
                    char firstChar = inputString.charAt(0);

                    /**
                     * switch nach anfangsbuchstaben in jedem Fall werden erst
                     * die kritischen Datenstrukturen gesperrt und nach dem
                     * Aufruf der methode wieder freigegeben sodass nur ein
                     * Thread gleichzeitig auf die kritishcen Datenstrukturen
                     * zzugreifen kann und sinnlose ausgaben so nicht möglich
                     * sind
                     */
                    switch (firstChar) {
                        case 'W':

                            //Semaphore wird gesperrt
                            try {
                                sem.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                            }
                            
                            //Methodenaufruf
                            sendSince(rest);
                            
                            //freigeben der Semaphore
                            sem.release();
                            break;

                        case 'T':

                            try {
                                sem.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                            }
                            sendFromTopic(rest);
                            sem.release();
                            break;
                        case 'L':

                            try {
                                sem.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                            }
                            sendLastModifiedTopics(rest);
                            sem.release();
                            break;
                        case 'P':

                            try {
                                sem.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                            }
                            System.out.println(rest);
                            sendToAll(newMessages());
                            sem.release();
                            break;
                        case 'X':
                            closeConnection();
                            break;
                        default:
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                closeConnection();
                System.out.println("Thread wurde beendet");
            }

        }

    }
    
   
    //nachrichten seit dem übergebenen Zeitpunkt senden
    public void sendSince(String since) {
        if (since != null && !since.equals("") && since.charAt(0) == ' ') {
            since = since.trim();
            if (!since.equals("")) {
                try {
                    //parsen des Zeitpunktes
                    long s = Long.parseLong(since);
                    //abfragen der nachrichten vom server
                    ArrayList<Message> messagesSince = this.server.getMessagesSince(s);
                    this.sendMessages(messagesSince);

                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                    out.println("unpassende Zeitangabe");
                    out.println(e.getMessage());
                    out.println("Befehl muss (diesmal richtig) wiederholt werden");

                }
            }

        } else {
        }
    }

    //alle Nachrichten eines Themas senden
    public void sendFromTopic(String topic) {
        topic = topic.trim();
        //nachrichten zum Thema aus dem Server rauslesen
        ArrayList<Message> mtt = server.getMessageToTopic(topic);
        //und senden
        this.sendMessages(mtt);
    }

    //sendet die n letzten geänderten Themen
    public void sendLastModifiedTopics(String number) {
        number = number.trim();
        try {
            //interpretation der Anzahl
            int n = Integer.parseInt(number);
            ArrayList<TopicTime> topicTime;
            //wenn 0, alles wiedergeben
            if (n == 0) {
                topicTime = server.getTopicTime();
            } else {
                //ansonsten n wiedergeben
                topicTime = server.getnTopicTime(n);
            }
            //senden
            sendTimeTopics(topicTime);
        } catch (NumberFormatException e) {
            System.out.println("Argument:" + number + "konnte nicht interpretiert werden");
            out.println("Argument:" + number + "konnte nicht interpretiert werden");
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }

    }
    
    //sendet die übergebene Liste an Kombinationen aus
    //Themenn und Zieten Protokollgerecht
    public void sendTimeTopics(ArrayList<TopicTime> topicTime) {
        try {
            
            //Ausgabe der Anzahl
            out.println(topicTime.size());
            
            //für jede Kombination einzelne Ausgabe
            for (TopicTime t : topicTime) {
                sendTimeTopic(t);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }
    }

    //sendet die Übergebene Kombination aus Thema und Zeit Protokollgerecht
    public void sendTimeTopic(TopicTime topicTime) {
        try {
            out.println("" + topicTime.time + " " + topicTime.topic);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }
    }

    //beenden der Verbindung
    public void closeConnection() {
        try {
            this.clientSocket.close();
            this._terminate = true;
        } catch (IOException e) {
            System.out.println("Socket konnte nicht geschlossen werden");
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }
    }

    
    //neue Nachrichten einlesen
    public ArrayList<Message> newMessages() {
        try {
            //einlesen der ersten Zeile mit anzahl der Nachrochten
            String input = in.readLine();

            while (input == null || input.equals("")) {
                input = in.readLine();
            }

            try {
                //für die Anzahl der angegebenen Nachrichten eine Neue nachricht hinzufügen und einlesen
                ArrayList<Message> messages = new ArrayList<>();
                int numberOfMessages = Integer.parseInt(input);
                for (int i = 0; i < numberOfMessages; i++) {
                    Message m = newMessage();
                    messages.add(m);
                }
                
                return messages;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                out.println(e.getMessage());
                out.println("Anzahl der Nachrihcten konte nicht interpretiert werden: " + input);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
            out.println("Lesefehler");
        }
        return null;
    }

    //einlesen einer neuen Nachricht
    public Message newMessage() {
        try {
            
            //lese solange bis ein input kommt
            String input = in.readLine();
            while (input == null || input.equals("")) {
                input = in.readLine();
            }

            
            try {
                //Zeilenanzahl einlesen
                int numberOfLines = Integer.parseInt(input);
                String[] mLines = new String[numberOfLines - 1];
                //einlesen der Zeile mit thema und Zeit 
                String topicInput = in.readLine();
                while (topicInput == null || topicInput.equals("")) {
                    topicInput = in.readLine();
                }
                //Aufteilen nach Zeit und Thema
                String[] tLine = topicInput.split(" ");
                topicInput = tLine[1];
                long time = Long.parseLong(tLine[0]);
                
                //Alle Zeilen einlesen
                for (int i = 0; i < numberOfLines - 1; i++) {
                    String lineInput = in.readLine();
                    while (lineInput == null || lineInput.equals("")) {
                        lineInput = in.readLine();
                    }

                    System.out.println(lineInput);
                    mLines[i] = lineInput;
                }
                //Neue Nachricht erstellen und speichern
                Message message = new Message(time, mLines, topicInput);
                server.addMessage(message);
                //und zurückgeben
                return message;

            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                out.println(e.getMessage());
                out.println("Anzahl der Zeilen konte nicht interpretiert werden: " + input);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
            out.println("Lesefehler");
        }
        return null;
    }
    
    //Ausgabe aller übergebenen Nachrichten Protokollgerecht
    public void sendMessages(ArrayList<Message> pMessages) {
        try {
            //Ausgabe der Zahl der Nachrichten
            out.println(pMessages.size());
            //jede Nachricht wird einzeln ausgegeben
            for (Message m : pMessages) {
                sendMessage(m);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    
    //sendet die übergebene Nachricht Protokollgerecht
    public void sendMessage(Message pMessage) {
        try {
            //Zeilenausgabe zur Nachricht
            ArrayList<String> lines = pMessage.getLines();
            out.println(lines.size());
            //Ausgabe der Zeit und des Themas
            out.println(pMessage.getDate().getTime() + " " + pMessage.getTopic());
            
            //Ausgabe der Zeilen
            for (String l : lines) {
                out.println(l);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    //schickt die übergebenen Nachrichten an alle Clients
    public void sendToAll(ArrayList<Message> messages) {
        server.sendToAll(messages);
    }

    //Schickt neue Nachrichten (wird vom server bei sendall aufgerufen)
    public void sendNew(ArrayList<Message> messages) {
        out.println("N");
        sendTimeTopics(toTopicTime(messages));
    }

    
    public ArrayList<TopicTime> toTopicTime(ArrayList<Message> messages) {
        ArrayList<TopicTime> tt = new ArrayList<>(messages.stream().map(m -> new TopicTime(m.getTopic(), m.getTime())).collect(Collectors.toList()));

        // sortieren
        return tt;
    }

}
