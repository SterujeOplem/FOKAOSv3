package communication;

import memory.Memory;
import process_management.*;
import filesystem.FileSystem;

public class Communication {

    Memory mMemory;
    private static int sMessageOrdinal = 0;

    public Communication(Memory mem){
        mMemory = mem;
    }
    /**
     * Wysyła komunikat do procesu o podanej nazwie.
     * Zgłaszane Wyjątki:
     * - w sytuacji gdy szukany proces nie istnieje*/
    public void send(String receiverName,String text,PCB mCurrentProcess) throws Exception
    {
        PCB receiver=null;
        if(mCurrentProcess.getPgid()==1)
        {
            receiver=findProcessInFirstGroup(receiverName);
        }
        if(mCurrentProcess.getPgid()==2)
        {
            receiver=findProcessInSecondGroup(receiverName);
        }
        if(receiver == null)
        {
            System.out.println("Proces nie istnieje.");
            return;
        }

        Message msg=new Message(mCurrentProcess,text);


        /**Dołączenie  komunikatu do kolejki */
        receiver.mQueue.add(msg);

        //boolean wasWaiting = receiver.getState() == PCB.State.Waiting;
        receiver.mMessageSemaphore.V();
        //if(wasWaiting && receiver.getState() != PCB.State.Waiting)

    }

    /**
     * Zwraca komunikat ze swojej kolejki procesów jednocześnie go z niej usuwając. */
    public void read(PCB mCurrentProcess)
    {
        if(!mCurrentProcess.getmWasWaitng())
            mCurrentProcess.mMessageSemaphore.P(mCurrentProcess);
        if(mCurrentProcess.getState() == PCB.State.Waiting)
        {
            mCurrentProcess.setmWasWaitng(true);
            return;
        }
        mCurrentProcess.setmWasWaitng(false);
        Message message;
        message = (Message) mCurrentProcess.mQueue.remove();

//        /**Alokoacja pamięci dla komunikatu, zwraca adres początku pamięci*/
//        int adres= mMemory.memoryAlloc(message.getAllSize(), message.getSender());
//        if(adres==-1)
//          return;
//        /**Zapis komunikatu do pamięci*/
//        mMemory.memorySetString(adres,message.getSenderName()+message.getText());



        System.out.println(message.getSenderName() + " " +message.getText());
        FileSystem fs = FileSystem.getInstance();
        try {
            String filename = "Wiadomosc" + sMessageOrdinal++;
            fs.createFile(filename);
           fs.writeToFile(filename, (message.getSenderName() + " " + message.getText()).toCharArray());
       }
        //!!!
        catch(Exception e)
        {}



    }


    /**
     * Zwraca PCB procesu z pierwszej grupy na podstawie nazwy
     * W przypadku gdy proces nie istnieje zwracana wartość null*/
    public static PCB findProcessInFirstGroup(String processName)
    {
        PCB toReturn = null;
        for(PCB x : ProcessManager.sFirstGroupList) {
            if(x.getProcessName().equals(processName)) {
                toReturn= x;
            }
        }
        return toReturn;
    }

    /**
     * Zwraca PCB procesu z drugiej grupy na podstawie nazwy
     * W przypadku gdy proces nie istnieje zwracana wartość null*/
    public static PCB findProcessInSecondGroup(String processName)
    {
        PCB toReturn = null;
        for(PCB x : ProcessManager.sSecondGroupList) {
            if(x.getProcessName().equals(processName)) {
                toReturn= x;
            }
        }
        return toReturn;
    }
}