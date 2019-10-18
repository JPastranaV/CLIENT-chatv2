package com.socket;

import java.io.Serializable;

public class Message implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    public String type, sender, content, recipient, smiley;
    
     public Message(String type, String sender, String content, String recipient){
        this.type = type; this.sender = sender; this.content = content; this.recipient = recipient;
    }
    
    
    public Message(String type, String sender, String content, String recipient, String smiley){
        this.type = type; this.sender = sender; this.content = content; this.recipient = recipient;
        this.smiley = smiley;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', recipient='"+recipient+"'}";
    }
    
    public String getSmiley() {
        return smiley;
    }

    public void setSmiley(String smiley) {
        this.smiley = smiley;
    }

}
