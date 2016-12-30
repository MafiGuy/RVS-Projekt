/**
 *
 * Klasse zur Verwaltung einer Nachricht
 * Eine Nachricht enhält Zeit, Thema und Zeilen mit Inhalt
 */
import java.util.*;

public class Message {

    //Zeilen der Nachricht
    private ArrayList<String> lines;
    //Thema
    private String topic;
    //Zeit
    private Date time;
    
   //Wenn man eine Nachricht erzhlt ohne eine Zeit anzugebene, 
    //wird eine neue Nachricht mit der momentanen Zeit erstellt
    public Message(String[] pLines, String pTopic) {
        this.lines = new ArrayList<>();
        this.time = new Date(); //erstellen eines Dates mit der momentanen Zeit
        this.lines.addAll(Arrays.asList(pLines));
        this.topic = pTopic;
    }

    //Konstruktor mit angegebener Zeit
    public Message(long time, String[] pLines, String pTopic) {
        this.lines = new ArrayList<>();
        this.time = new Date();
        this.lines.addAll(Arrays.asList(pLines));
        this.topic = pTopic;
        this.time = new Date(time);
    }


    
    //getter
    public Date getDate() {
        return time;
    }

    public long getTime() {
        return time.getTime();
    }

    public String getTopic() {
        return this.topic;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

}
