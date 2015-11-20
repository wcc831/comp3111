package hk.ust.cse.hunkim.questionroom.polling;

import java.util.Date;

/**
 * Created by onzzz on 19/11/2015.
 */
public class Polling implements Comparable<Polling> {

    private String name;
    private String[][] options = new String[5][2];
    private long timestamp;

    private Polling(){

    }

    public Polling(String name, int numOfOption){
        this.name = name;
        this.timestamp = new Date().getTime();
        for (int i=0; i<5; i++){
            for (int j=0; j<2; j++){
                this.options[i][j] = "";
            }
        }
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

    @Override
    public int compareTo(Polling other) {
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
