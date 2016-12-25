/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lena
 */
import java.util.*;

public class Message {

    private ArrayList<String> lines;
    private String topic;
    private Date time;

    public Message(String[] pLines, String pTopic) {
        this.lines = new ArrayList<String>();
        this.time = new Date();
        for (String line : pLines) {
            this.lines.add(line);
        }
        this.topic = pTopic;
    }

    public String getProtokollString() {
        String e = "";
        e = e + this.lines.size() + System.lineSeparator();
        e = e + this.time.getTime();
        e = e + this.topic + System.lineSeparator();
        for (String line : this.lines) {
            e = e + line + System.lineSeparator();
        }
        return e;
    }

    public Date getDate() {
        return time;
    }

    public long getTime() {
        return time.getTime();
    }
    
    public String getTopic(){
        return this.topic;
    }
    
    public ArrayList<String> getLines(){
        return lines;
    }

}
