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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClientThread extends Thread {

    private boolean _terminate;
    private Socket clientSocket;
    private Server server;

    private PrintWriter out;
    private BufferedReader in;

    public ClientThread(Server pServer, Socket pClientSocket) {
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

        while (!_terminate) {
            try {
                //System.out.println(out);
                String inputString = in.readLine();
                if (inputString != null) {
                    //System.out.println(in.readLine());

                    System.out.println(inputString);
                    String rest = inputString.length() > 1 ? inputString.substring(1) : "";
                    char firstChar = inputString.charAt(0);
                    switch (firstChar) {
                        case 'W':

                            System.out.println("First input: w");

                            System.out.println(rest);
                            sendSince(rest);
                            break;

                        case 'T':

                            System.out.println("First input: t");

                            System.out.println(rest);
                            sendFromTopic(rest);
                            break;
                        case 'L':

                            System.out.println("First input: l");
                            System.out.println(rest);
                            sendLastModifiedTopics(rest);
                            break;
                        case 'P':

                            System.out.println("First input: p");
                            System.out.println(rest);
                            //System.out.println(in.readLine());                            
                            sendToAll(newMessages());
                            break;
                        case 'X':
                            closeConnection();
                            break;
                        default:
                    }
                }

                String inputLine, outputLine;

                // Initiate conversation with client
                /**
                 * KnockKnockProtocol kkp = new KnockKnockProtocol(); outputLine
                 * = kkp.processInput(null); out.println(outputLine);
                 *
                 * while ((inputLine = in.readLine()) != null) { outputLine =
                 * kkp.processInput(inputLine); out.println(outputLine); if
                 * (outputLine.equals("Bye.")) { break; }
                 */
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

    }

    public void sendSince(String since) {
        if (since != null && !since.equals("") && since.charAt(0) == ' ') {
            since = since.substring(1);
            if (!since.equals("")) {
                try {
                    long s = Long.parseLong(since);
                    ArrayList<Message> messagesSince = this.server.getMessagesSince(s);
                    this.sendMessages(messagesSince);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        } else {
        }
    }

    public void sendFromTopic(String topic) {
        topic = topic.trim();
        ArrayList<Message> mtt = server.getMessageToTopic(topic);
        this.sendMessages(mtt);
    }

    public void sendLastModifiedTopics(String number) {
        number = number.trim();
        try{
            int n = Integer.parseInt(number);
            ArrayList<TopicTime> topicTime;
            if(n == 0){
                topicTime = server.getTopicTime();
            }
            else{
                topicTime = server.getnTopicTime(n);
            }
            sendTimeTopics (topicTime);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }

    }

    public void sendTimeTopics(ArrayList<TopicTime> topicTime) {
        try {
            out.println(topicTime.size());
            for (TopicTime t : topicTime) {
                sendTimeTopic(t);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }
    }

    public void sendTimeTopic(TopicTime topicTime) {
        try {
            out.println("" + topicTime.time +" " +topicTime.topic);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            out.println(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            this.clientSocket.close();
            this._terminate = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<Message> newMessages() {
        try {
            String input = in.readLine();

            while (input == null || input.equals("")) {
                input = in.readLine();
            }

            System.out.println(input);

            try {
                ArrayList<Message> messages = new ArrayList<>();
                int numberOfMessages = Integer.parseInt(input);
                for (int i = 0; i < numberOfMessages; i++) {
                    Message m = newMessage();
                    messages.add(m);
                }
                return messages;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Message newMessage() {
        try {
            String input = in.readLine();

            while (input == null || input.equals("")) {
                input = in.readLine();
            }

            System.out.println(input);

            try {
                int numberOfLines = Integer.parseInt(input);
                String[] mLines = new String[numberOfLines - 1];
                String topicInput = in.readLine();
                while (topicInput == null || topicInput.equals("")) {
                    topicInput = in.readLine();
                }

                System.out.println(topicInput);
                String[] tLine = topicInput.split(" ");
                topicInput = tLine[1];
                long time = Long.parseLong(tLine[0]);

                for (int i = 0; i < numberOfLines - 1; i++) {
                    String lineInput = in.readLine();
                    while (lineInput == null || lineInput.equals("")) {
                        lineInput = in.readLine();
                    }

                    System.out.println(lineInput);
                    mLines[i] = lineInput;
                    //System.out.println("Zeile" + i + " " + lineInput);
                }

                Message message = new Message(time, mLines, topicInput);
                server.addMessage(message);
                System.out.println("clientSocket:" + message.getProtokollString());
                return message;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void sendMessages(ArrayList<Message> pMessages) {
        try {
            //out.println("test in sendMessages");
            out.println(pMessages.size());
            for (Message m : pMessages) {
                sendMessage(m);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(Message pMessage) {
        try {
            //out.println("test in sendMessage");
            ArrayList<String> lines = pMessage.getLines();

            out.println(lines.size());

            out.println(pMessage.getDate().getTime() + " " + pMessage.getTopic());
            for (String l : lines) {
                out.println(l);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    public void sendString(String string) {
        try {
            out.println(string);
            System.out.println(string);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendToAll(ArrayList<Message> messages) {
        server.sendToAll(messages);
    }
    
    public void sendNew(ArrayList<Message> messages) {
        out.println("N");
        sendTimeTopics(toTopicTime(messages));
    }
    
    public ArrayList<TopicTime> toTopicTime (ArrayList<Message> messages){
        ArrayList<TopicTime> tt = new ArrayList<>(messages.stream().map( m -> new TopicTime(m.getTopic(), m.getTime())).collect(Collectors.toList()));
        
        // sortieren
        
        return tt;
    }

}
