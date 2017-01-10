package process_management;

import communication.Message;
import java.util.LinkedList;
import semaphores.*;
import scheduler.*;

//TODO Proces pusty,kiedy procesor nie ma żadnego innego procesu
public class PCB {

    public static final int NO_MEMORY =-1;

    public static int nr = 0;

    public enum State{ New, Ready, Active, Waiting, Terminated}

    private String mProcessName;

    private int mPID, mPGID; // Identyfikatory

    private State mState;    // Stan w danej chwili

    private int mRegA, mRegB, mProgramCounter; // 2 rejestry, Akumulator + ogolnego przezn. oraz licznik rozk. wskazuje na adres nast.rozkazu

    private boolean mZF; // Zero Flag

    private PCB  mPrevInGroup, mPrevAll; // Referencje, które mają działać jako wskaźniki do odpowiednich poprz. PCB

    private int mMemRequired;

    private int mMemAdr;

    private int mMemUsed;

    private boolean mWasWaitng;

    private String mProgramName;

    public LinkedList mQueue;

    public Semaphore mMessageSemaphore;



    /** Konstruktory **/

    public PCB()
    {
        nr++;
        mPID = nr;
        mPGID = 1;
        mState = State.New;
        mMemRequired = 0;
        mMemAdr = 0;
        mPrevAll = null;
        mPrevInGroup = null;
        mQueue = new LinkedList<Message>();
        mMessageSemaphore = new Semaphore("messageSemaphore", 0);
        mWasWaitng=false;
    }

    public PCB(PCB Process)
    {
        nr++;
        mProcessName = "Process" + nr;
        mState = State.New;
        mPID = nr;
        mPGID = Process.getPgid();
        mRegA = Process.getRegA();
        mRegB = Process.getRegB();
        mProgramCounter = Process.getProgramCounter();
        mZF = Process.getZf();
        mMemRequired = Process.getMemRequired();
        mMemAdr = Process.getMemAdr();
        mProcessName = Process.getProcessName();
        mPrevAll = null;
        mPrevInGroup = null;
        mQueue = new LinkedList<Message>();
        mMessageSemaphore = new Semaphore("messageSemaphore", 0);
        mWasWaitng=false;
    }

    public PCB(String processName,int pgid,int memUsed) {
        nr++;
        mProcessName = processName;
        mPID = nr;
        mPGID = pgid;
        mState = State.New;
        mMemRequired = memUsed;
        mRegA = mRegB = mProgramCounter = 0;
        mZF = false;
        mPrevAll = null;
        mPrevInGroup = null;
        mQueue = new LinkedList<Message>();
        mMessageSemaphore = new Semaphore("messageSemaphore", 0);
    }




    /** Metody, gettery, settery **/


    public int getRegA()
    {
        return mRegA;
    }

    public int getRegB()
    {
        return mRegB;
    }

    public int getProgramCounter()
    {
        return mProgramCounter;
    }

    public boolean getZf()
    {
        return mZF;
    }

    public void setRegA(int regA)
    {
        mRegA = regA;
    }

    public void setRegB(int regB)
    {
        mRegB = regB;
    }

    public void setProgramCounter(int programCounter)
    {
        mProgramCounter = programCounter;
    }

    public void setZf(boolean zf)
    {
        mZF = zf;
    }

    public int getPid()
    {
        return mPID;
    }

    public int getPgid()
    {
        return mPGID;
    }

    public String getProcessName() {
        return mProcessName;
    }

    public void setProcessName(String processName) {
        mProcessName = processName;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        if(state== State.Ready&&this!=ProcessManager.sAllProcessList.get(0)&&this.getState()!= State.Active)
            Scheduler.newReadyProces();
        mState = state;
    }

    public int getMemRequired() {
        return mMemRequired;
    }

    public void setMemRequired(int memUsed) {
        mMemRequired = memUsed;
    }

    public int getMemAdr() {
        return mMemAdr;
    }

    public void setMemAdr(int memAdr) {
        mMemAdr = memAdr;
    }

    public void setPrevInGroup(PCB prev){
        mPrevInGroup = prev;
    }
    public void setPrevAll(PCB prev){
        mPrevAll = prev;
    }
    public PCB getPrevAll(){
        return mPrevAll;
    }
    public PCB getPrevInGroup(){
        return mPrevInGroup;
    }

    public int getMemUsed() {
        return mMemUsed;
    }

    public void setMemUsed(int mMemUsed) {
        this.mMemUsed = mMemUsed;
    }

    public String getmProgramName() {
        return mProgramName;
    }

    public void setmProgramName(String mProgramName) {
        this.mProgramName = mProgramName;
    }

    public boolean getmWasWaitng() {
        return mWasWaitng;
    }

    public void setmWasWaitng(boolean mWasWaitng) {
        this.mWasWaitng = mWasWaitng;
    }

}




