package semaphores;

import process_management.*;
import scheduler.Scheduler;

import java.util.LinkedList;
import java.util.List;


public class Semaphore {
    private int m_Semaphorevalue;
    private int m_StartSemaphorevalue;

    public List<PCB> getM_Queue() {
        return m_Queue;
    }

    private List<PCB> m_Queue = new LinkedList<>();
    private String m_Name;//Name of semaphore

    public Semaphore(String NameOfSemaphore,int StartSemaphoreValue){
        this.m_Name=NameOfSemaphore;
        this.m_Semaphorevalue=StartSemaphoreValue;//Wartosc poczatkowa semafora - 0 -dla komunikatow 1-dla procesow
        this.m_StartSemaphorevalue=StartSemaphoreValue;
    }

    public void V(){
        // if(m_Semaphorevalue>m_StartSemaphorevalue) return; //Przypadek niepotrzebnie zwiększonego semaforu
        m_Semaphorevalue++;
        if(0 >= m_Semaphorevalue) {
            //if(m_Queue.get(0).getProgramCounter()==0)
            //m_Queue.get(0).setState(PCB.State.New);//
            //else
            m_Queue.get(0).setState(PCB.State.Ready);//Zmiana stanu procesu waiting->ready
            m_Queue.remove(0);
        }
        //   Scheduler.setVOperationOccour(); //Informacja dla Sebastiana o nowym procesie ready
    }


    public void P(PCB Process){
        //System.out.println("dffdgfddf");
        m_Semaphorevalue--;
        if(m_Semaphorevalue<0) {
            m_Queue.add(Process);
            Process.setState(PCB.State.Waiting);
        }
    }

    public int getM_Semaphorevalue(){return m_Semaphorevalue;}

    public void ShowQueueOnScreen(){
        if(!m_Queue.isEmpty())
            for(int i=0;i<m_Queue.size();i++){

                System.out.println(i+" "+m_Queue.get(i).getProcessName()+" PID "+m_Queue.get(i).getPid());
            }
        else
        {
            System.out.println(this.m_Name+" - Kolejka jest pusta");
        }
    }
}