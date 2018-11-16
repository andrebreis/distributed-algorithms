package logic;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {

    private String content;
    private int senderId;
    private int[] vector;

    public Message(String content) {
        this.content = content;
//        this.senderId = senderId;
//        this.vector = vector;
    }

    @Override
    public String toString() {
        return "logic.Message{ vector='" + Arrays.toString(vector) + "'" +
                ", content='" + content + '\'' +
                '}';
    }

    public int getSenderId() {
        return senderId;
    }

    public int[] getVector() {
        return vector;
    }

//    public void setVector(int[] vector) {
//        this.vector = vector;
//    }

    public void prepareMessage(int senderId, int[] vector){
        this.senderId = senderId;
        this.vector = Arrays.copyOf(vector, vector.length);
    }


//    @Override
//    public int compareTo(logic.Message message) {
//        return timestamp.compareTo(message.timestamp);
//    }
}
