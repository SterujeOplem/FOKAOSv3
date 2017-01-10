package communication;

import process_management.*;


public class Message {

    PCB mSender;
    public String mText;

    Message (PCB sender, String text)
    {
        this.mSender=sender;
        if(text.length()>8)
        {
            this.mText=text.substring (0, 7); //ograniczenie komunikatu do 8 znaków
        }
        else
        {
            this.mText=text;
        }
    }

    public PCB getSender() {
        return this.mSender;
    }

    public String getSenderName() {
        return this.mSender.getProcessName();
    }

    public String getText(){
        return this.mText;
    }

    public int getTextSize(){
        return this.mText.length();
    }

    public int getSenderSize(){
        return this.mSender.getProcessName().length();
    }

    public int getAllSize() { return this.mText.length()+this.mSender.getProcessName().length();}


}
