package hk.ust.cse.hunkim.questionroom.polling;

import java.util.Date;

/**
 * Created by onzzz on 19/11/2015.
 */
public class Polling implements Comparable<Polling> {

    private String key;
    private String name;
    private String[][] options = new String[10][2];
    private long timestamp;

    private Polling(){

    }

    public Polling(String name, String[] option, int numOfOption){
        this.name = name;
        this.timestamp = new Date().getTime();

        int j = 0;
        for (int i=0; i<numOfOption && j<10;){
            if (!option[j].equals("")){
                options[i][0] = option[j];
                options[i][1] = "0";
                i++;
            }
            j++;
        }
    }

    public void setVote(int i, String s){
        options[i][1] = s;
    }

    public String getVote(int i){
        return options[i][1];
    }

    public String getKey() {
        return key;
    }

    public String getName(){
        return name;
    }

    public String[][] getOptions(){
        return options;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int compareTo(Polling other) {
        if (other.timestamp == this.timestamp) {
            return 0;
        }
        return other.timestamp > this.timestamp ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Polling)) {
            return false;
        }
        Polling other = (Polling)o;
        return key.equals(other.key);
    }


    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
