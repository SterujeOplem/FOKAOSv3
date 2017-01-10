package process_management;

import java.lang.Exception;
import java.util.ArrayList;
import memory.*;


public class ProcessManager{


    public static ArrayList<PCB> sAllProcessList = new ArrayList<PCB>();
    public static ArrayList<PCB> sFirstGroupList = new ArrayList<PCB>();
    public static ArrayList<PCB> sSecondGroupList = new ArrayList<PCB>();

    public Memory getMemory() {
        return mMemory;
    }

    private Memory mMemory;

    public ProcessManager(Memory mem){
        mMemory = mem;
    }

    public PCB InitProcess(PCB process) throws Exception
    {
        /** Unikalnosc nazw **/
        for(PCB it : sAllProcessList){
            if(it.getProcessName().equals(process.getProcessName())){
                //System.out.println("Istnieje juz obiekt o nazwie" + process.getProcessName());
                throw  new Exception("Istnieje juz obiekt o nazwie " + process.getProcessName());
            }
        }

        PCB p1 = new PCB(process);
        sAllProcessList.add(p1);

        if(p1.getPgid() == 1) { sFirstGroupList.add(p1); }
        else if(p1.getPgid() == 2){ sSecondGroupList.add(p1); }
        else {}
        try {
            if(ProcessManager.sAllProcessList.size() == 1){
                process.setPrevAll(process);
            }
            if(ProcessManager.sFirstGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sSecondGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sAllProcessList.size() > 1) {
                process.setPrevAll(getPrvFromAll(process));
                sAllProcessList.get(0).setPrevAll(sAllProcessList.get(sAllProcessList.size()-1));
            }
            if (ProcessManager.sFirstGroupList.size()> 1) {
                if(process.getPgid() == 1) process.setPrevInGroup(getPrvFromG1(process));
                sFirstGroupList.get(0).setPrevInGroup(sFirstGroupList.get(sFirstGroupList.size()-1));
            }
            if(ProcessManager.sSecondGroupList.size()> 1) {
                if(process.getPgid() == 2) process.setPrevInGroup(getPrvFromG2(process));
                sSecondGroupList.get(0).setPrevInGroup(sSecondGroupList.get(sSecondGroupList.size()-1));
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        //!!!


        if(mMemory.isSize(p1.getMemRequired(),p1))
            p1.setMemAdr(mMemory.memoryAlloc(p1.getMemRequired(), p1));
        else
        {
            p1.setMemUsed(PCB.NO_MEMORY);
            System.out.println("Brak pamieci dla procesu "+p1.getProcessName());
        }
        p1.setMemAdr(mMemory.memoryAlloc(p1.getMemRequired(), p1));
        return p1;
    }

    public PCB InitProcess(String name,int group,int memAssigned) throws Exception
    {

        for(PCB it : sAllProcessList){
            if(it.getProcessName().equals(name)){
                // System.out.println("Istnieje juz obiekt o nazwie" + name);
                throw  new Exception("Istnieje juz obiekt o nazwie " + name);
            }
        }
        PCB process = new PCB(name,group,memAssigned);
        sAllProcessList.add(process);
        if(process.getPgid() == 1) { sFirstGroupList.add(process); }
        else { sSecondGroupList.add(process); }
        try {
            if(ProcessManager.sAllProcessList.size() == 1){
                process.setPrevAll(process);
            }
            if(ProcessManager.sFirstGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sSecondGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sAllProcessList.size() > 1){
                process.setPrevAll(getPrvFromAll(process));
                sAllProcessList.get(0).setPrevAll(sAllProcessList.get(sAllProcessList.size()-1));
            }
            if (ProcessManager.sFirstGroupList.size()> 1) {
                if(process.getPgid() == 1) process.setPrevInGroup(getPrvFromG1(process));
                sFirstGroupList.get(0).setPrevInGroup(sFirstGroupList.get(sFirstGroupList.size()-1));
            }
            if(ProcessManager.sSecondGroupList.size()> 1){
                if(process.getPgid() == 2) process.setPrevInGroup(getPrvFromG2(process));
                sSecondGroupList.get(0).setPrevInGroup(sSecondGroupList.get(sSecondGroupList.size()-1));
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        //!!!
        if(mMemory.isSize(process.getMemRequired(),process))
            process.setMemAdr(mMemory.memoryAlloc(process.getMemRequired(), process));
        else
        {
            process.setMemUsed(PCB.NO_MEMORY);
            System.out.println("Brak pamieci dla procesu "+process.getProcessName());
        }

        return process;
    }

    public PCB InitProcessAsNew(String name, int group, int memAssigned) throws Exception//nie ma ani pamieci ani pliku z programem czysta prowizorka ;)
    {

        for(PCB it : sAllProcessList){
            if(it.getProcessName().equals(name)){
                // System.out.println("Istnieje juz obiekt o nazwie" + name);
                throw  new Exception("Istnieje juz obiekt o nazwie " + name);
            }
        }
        PCB process = new PCB(name,group,memAssigned);
        sAllProcessList.add(process);
        if(process.getPgid() == 1) { sFirstGroupList.add(process); }
        else { sSecondGroupList.add(process); }
        try {
            if(ProcessManager.sAllProcessList.size() == 1){
                process.setPrevAll(process);
            }
            if(ProcessManager.sFirstGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sSecondGroupList.size() == 1)
            {
                process.setPrevInGroup(process);
            }
            if(ProcessManager.sAllProcessList.size() > 1){
                process.setPrevAll(getPrvFromAll(process));
                sAllProcessList.get(0).setPrevAll(sAllProcessList.get(sAllProcessList.size()-1));
            }
            if (ProcessManager.sFirstGroupList.size()> 1) {
                if(process.getPgid() == 1) process.setPrevInGroup(getPrvFromG1(process));
                sFirstGroupList.get(0).setPrevInGroup(sFirstGroupList.get(sFirstGroupList.size()-1));
            }
            if(ProcessManager.sSecondGroupList.size()> 1){
                if(process.getPgid() == 2) process.setPrevInGroup(getPrvFromG2(process));
                sSecondGroupList.get(0).setPrevInGroup(sSecondGroupList.get(sSecondGroupList.size()-1));
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        //!!!
        process.setMemUsed(PCB.NO_MEMORY);
        //System.out.println("Brak pamieci dla procesu "+process.getProcessName());

        return process;
    }

    public boolean ExistProcess(int pid) throws Exception {
        for (PCB x : sAllProcessList) {
            if (x.getPid() == pid) {
                return true;
            }
        }
        return false;
    }

    public boolean ExistProcess(PCB process) {
        if(sAllProcessList.contains(process)){
            return true;
        }
        else{
            return false;
        }
    }

    public void ProcessTerminate(PCB process) {
        try {
            if(ExistProcess(process)) {

                if (process.getPgid() == 1) {
                    getNextFromG1(process).setPrevInGroup(getPrvFromG1(process));
                    sFirstGroupList.remove(process);
                } else {
                    getNextFromG2(process).setPrevInGroup(getPrvFromG2(process));
                    sSecondGroupList.remove(process);
                }
                getNextFromAll(process).setPrevAll(getPrvFromAll(process));
                sAllProcessList.remove(process);
            }

        }catch(Exception e){
            System.out.println(e.getMessage() + "Blad w processterminate");
        }
    }

    public static void ProcessTerminate(int pid){
        try {
            PCB process = findProcess(pid);
            sAllProcessList.remove(process);
            if(process.getPgid() == 1) {
                sFirstGroupList.remove(process);
            }
            else{
                sSecondGroupList.remove(process);
            }

        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return;
        }
    }

    public static PCB findProcess(int pid) throws Exception{
        for(PCB it : sAllProcessList){
            if(it.getPid() == pid){
                return it;
            }
        }
        throw new Exception("PID Exception.Proces nie istnieje!");
    }

    public static PCB findProcess(String name) throws Exception{
        for(PCB it : sAllProcessList){
            if(it.getProcessName().equals(name)){
                return it;
            }
        }
        throw new Exception("Name Exception.Proces nie istnieje!");
    }

    public PCB getPrvFromAll(PCB process) throws Exception {
        int idx = ProcessManager.sAllProcessList.indexOf(process);
        if (idx <= 0){ return sAllProcessList.get(sAllProcessList.size()-1);}
        return ProcessManager.sAllProcessList.get(idx -1 );
    }

    public PCB getPrvFromG1(PCB process) throws Exception {
        int idx = ProcessManager.sFirstGroupList.indexOf(process);
        if (idx <= 0){return  sFirstGroupList.get(sFirstGroupList.size()-1);}
        return ProcessManager.sFirstGroupList.get(idx - 1);
    }

    public PCB getPrvFromG2(PCB process) throws Exception {
        int idx = ProcessManager.sSecondGroupList.indexOf(process);
        if (idx <= 0){return  sSecondGroupList.get(sSecondGroupList.size()-1);}
        return ProcessManager.sSecondGroupList.get(idx - 1);
    }

    public PCB getNextFromAll(PCB process) {
        int idx = ProcessManager.sAllProcessList.indexOf(process);
        if((idx+1)>= sAllProcessList.size()) return ProcessManager.sAllProcessList.get(0);
        return ProcessManager.sAllProcessList.get(idx + 1);
    }

    public PCB getNextFromG1(PCB process) {
        int idx = ProcessManager.sFirstGroupList.indexOf(process);
        if((idx+1)>= sFirstGroupList.size()) return ProcessManager.sFirstGroupList.get(0);
        return ProcessManager.sFirstGroupList.get(idx + 1);
    }

    public PCB getNextFromG2(PCB process) {
        int idx = ProcessManager.sSecondGroupList.indexOf(process);
        if((idx+1)>= sSecondGroupList.size()) return ProcessManager.sSecondGroupList.get(0);
        return ProcessManager.sSecondGroupList.get(idx + 1);
    }

    /********** Debug ********/
    public void printAllProcess(){
        for(PCB it : sAllProcessList){
            System.out.println(it.getProcessName() +" " + it.getPid() +" "+ it.getPgid() +" "+ it.getPrevAll().getProcessName());
        }
    }
    public void printAllProcessInG1(){
        for(PCB it : sFirstGroupList){
            System.out.println(it.getProcessName() + it.getPid() + it.getPgid());
        }
    }
    public void printAllProcessInG2(){
        for(PCB it : sSecondGroupList){
            System.out.println(it.getProcessName() + it.getPid() + it.getPgid());
        }
    }
}
