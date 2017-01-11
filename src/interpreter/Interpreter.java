package interpreter;

import process_management.PCB;
import process_management.ProcessManager;
import scheduler.Scheduler;
import communication.Communication;
import filesystem.FileSystem;
import memory.Memory;

/**
 * Created by Damian on 03.11.2016.
 */
public class Interpreter {

    private static int RA, RB, p_counter;
    private static int mem_addr, prog_limit;
    private static boolean ZF;
    private static int last_result;

    /**Referencje*/
    private static Communication sCommunication; ///komunikacja
    private static Memory sMemory; //pamięć
    private static FileSystem sFileSystem; //Fsystem
    private static ProcessManager sProcessManager; //sProcessManager procesow
    private static Scheduler sScheduler;

    public Interpreter(ProcessManager Manager, Memory Memory, FileSystem FileSystem, Communication Communication, Scheduler Scheduler){
        sCommunication = Communication;
        sMemory = Memory;
        sFileSystem = FileSystem;
        sProcessManager = Manager;
        sScheduler = Scheduler;

        mem_addr = 0;
        p_counter = 0;
        prog_limit = 0;
    }

    private static void pCounterSteps(int n){
        p_counter +=n;
        //System.out.println("       Licznik rozkazów + " + n + " : " + p_counter);
    }

    private static void pullRegisters(PCB proces){
        RA = proces.getRegA();
        RB = proces.getRegB();
        p_counter = proces.getProgramCounter();
        ZF = proces.getZf();
        mem_addr = proces.getMemAdr();
        prog_limit = proces.getMemRequired();
    }

    private static void pushRegisters(PCB proces){
        proces.setRegA(RA);
        proces.setRegB(RB);
        proces.setProgramCounter(p_counter);
        proces.setZf(ZF);
    }

    private static String readFromMemory(Memory Memory, int mem_addr, int quantity){
        String Buffer = new String();

        for(int i = 0; i < quantity; i++){
            //Buffer += sMemory[mem_addr + i];
            Buffer += Memory.memoryGet(mem_addr + i);
        }

        return Buffer;
    }

    private static String readFromMemoryUntil(Memory Memory, int mem_addr, char marker){
        String Buffer = new String();
        int i = 0;
        //while(sMemory[mem_addr + i] != marker){
        while(Memory.memoryGet(mem_addr + i) != marker){
            //Buffer += sMemory[mem_addr + i];
            Buffer += Memory.memoryGet(mem_addr + i);
            i++;
        }

        return Buffer;
    }//dołożyć obsługę wyjątków (brak markera)

    public static void executeInstruction(PCB process){
        if(process.getPgid() == 0) return;

        pullRegisters(process);
        System.out.println("Adress w pamieci "+(mem_addr + p_counter));
        String instruction = readFromMemory(sMemory, mem_addr + p_counter, 2);
        pCounterSteps(3); //rozkaz + spacja = 3 -> p_counter_3Step();

        String arg1, arg2;
        //ExecuteCommand("test1");
        //System.out.println("Rozkaz: " + instruction);
        switch (instruction){
            case "mv":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(3);
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   mv " + arg1 + ", " + arg2);
                mv(arg1, arg2);
                break;
            case "ad":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(3);
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   ad " + arg1 + ", " + arg2);
                ad(arg1, arg2);
                break;
            case "sb":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(3);
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   sb " + arg1 + ", " + arg2);
                sb(arg1, arg2);
                break;
            case "ml":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(3);
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   ml " + arg1 + ", " + arg2);
                ml(arg1, arg2);
                break;
            case "dv":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(3);
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   dv " + arg1 + ", " + arg2);
                dv(arg1, arg2);
                break;
            case "jp":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                System.out.println("   jp " + arg1);
                jp(arg1);
                break;
            case "j0":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                System.out.println("   j0 " + arg1);
                pCounterSteps(2);
                j0(arg1);
                break;
            case "j1":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                System.out.println("   j1 " + arg1);
                pCounterSteps(2);
                j1(arg1);
                break;
            case "cf":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                pCounterSteps(arg1.length() + 1); //+1 bo "$"
                System.out.println("   cf " + arg1);
                cf(arg1);
                break;
            case "wf":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, ',');
                pCounterSteps(arg1.length() + 1); //+1 bo ","
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);

                if(arg2.equals("ra")||arg2.equals("rb")) {
                    wf(arg1, arg2);
                }else
                {
                    String tmp=readFromMemoryUntil(sMemory,mem_addr+p_counter,'$');
                    pCounterSteps(tmp.length()+1);
                    arg2+=tmp;
                    wfString(arg1,arg2);
                }
                System.out.println("   wf " + arg1 + ", " + arg2);
                break;
            case "af":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, ',');
                pCounterSteps(arg1.length() + 1); //+1 bo ","
                arg2 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);

                if(arg2.equals("ra")||arg2.equals("rb")) {
                    af(arg1, arg2);
                }else
                {
                    String tmp=readFromMemoryUntil(sMemory,mem_addr+p_counter,'$');
                    pCounterSteps(tmp.length()+1);
                    arg2+=tmp;
                    afString(arg1,arg2);
                }
                System.out.println("   af " + arg1 + ", " + arg2);
                break;
            case "rf":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                pCounterSteps(arg1.length() + 1); //+1 bo "$"
                System.out.println("   rf " + arg1);
                rf(arg1);
                break;
            case "ot":
                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
                pCounterSteps(2);
                System.out.println("   ot " + arg1);
                ot(arg1);
                break;
            case "xc":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                pCounterSteps(arg1.length() + 1); //+1 bo ","
                //arg2 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                //pCounterSteps(arg2.length() + 1); //+1 bo "$"
                //System.out.println("   xc " + arg1 + ", " + arg2);
                System.out.println("   xc " + arg1);
                try {
                    xc(arg1, process);
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
                break;
//            case "xd":
//                arg1 = readFromMemory(sMemory, mem_addr + p_counter, 2);
//                pCounterSteps(arg1.length() + 1);
//                System.out.println("   xd " + arg1);
//                xd();
//                break;
            case "xr":
                System.out.println("   xr " + process.getProcessName());
                xr(process);
                if(process.getState() == PCB.State.Waiting)
                    pCounterSteps(-2);
                pCounterSteps(-1);
                break;
            case "xs":
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, ',');
                pCounterSteps(arg1.length() + 1); //+1 bo ","
                arg2 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                pCounterSteps(arg2.length() + 1); //+1 bo "$"
                System.out.println("   xs " + arg1 + ", " + arg2);
                xs(arg1, arg2, process);
                break;
            case "xy": //uruchomienie procesu z (new na ready)
                arg1 = readFromMemoryUntil(sMemory, mem_addr + p_counter, ',');
                pCounterSteps(arg1.length() + 1); //+1 bo "$"
                arg2 = readFromMemoryUntil(sMemory, mem_addr + p_counter, '$');
                pCounterSteps(arg2.length() + 1); //+1 bo "$"
                //System.out.println("    xy " + arg1);
                System.out.println("    xy " + arg1+", "+ arg2);
                xy(arg1,arg2);
                break;
            case "xz":
                System.out.println("   xz " + process.getProcessName());
                xz(process);
                pCounterSteps(-1);
                break;
            default:
                System.out.println("Wykryto nieznany rozkaz: " + instruction);
                break;
        }

        if(last_result == 0) ZF = true;
        else ZF = false;

        pushRegisters(process);

        if(p_counter >= prog_limit){
            System.out.println("Proces " + process.getProcessName() + " się zakończył");
            process.setState(PCB.State.Terminated);
            /**jakiś process kil or something like dat**/
        }
        printRegister();
    }

    private static void printRegister()
    {
        System.out.println("Rejestr A: "+ sScheduler.getRunningProces().getRegA());
        System.out.println("Rejestr B: "+ sScheduler.getRunningProces().getRegB());
        System.out.println("Licznik Rozkazów : "+ Integer.toHexString(sScheduler.getRunningProces().getProgramCounter()));
    }
    private static void mv(String arg1, String arg2){

        switch (arg1){
            case "ra":
                if(arg2.equals("rb")) {
                    RA = RB;
                }else {
                    try{
                        RA = Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu mv ra");
                    }
                }
                last_result = RA;
                break;
            case "rb":
                if(arg2.equals("ra")) {
                    RB = RA;
                }else {
                    try{
                        RB = Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu mv rb");
                    }
                }
                last_result = RB;
                break;
            default:
                System.out.println("Błędne argumenty rozkazu mv");
                break;
        }

    }

    private static void ad(String arg1, String arg2){
        switch (arg1){
            case "ra":
                if(arg2.equals("rb")) {
                    RA += RB;
                }else {
                    try{
                        RA += Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu ad ra");
                    }
                }
                last_result = RA;
                break;
            case "rb":
                if(arg2.equals("ra")) {
                    RB += RA;
                }else {
                    try{
                        RB += Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu ad rb");
                    }
                }
                last_result = RB;
                break;
            default:
                System.out.println("Błędne argumenty rozkazu ad");
                break;
        }
    }

    private static void sb(String arg1, String arg2){
        switch (arg1){
            case "ra":
                if(arg2.equals("rb")) {
                    RA -= RB;
                }else {
                    try{
                        RA -= Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu sb ra");
                    }
                }
                last_result = RA;
                break;
            case "rb":
                if(arg2.equals("ra")) {
                    RB -= RA;
                }else {
                    try{
                        RB -= Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu sb rb");
                    }
                }
                last_result = RB;
                break;
            default:
                System.out.println("Błędne argumenty rozkazu sb");
                break;
        }
    }

    private static void ml(String arg1, String arg2){
        switch (arg1){
            case "ra":
                if(arg2.equals("rb")) {
                    RA *= RB;
                }else {
                    try{
                        RA *= Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu ml ra");
                    }
                }
                last_result = RA;
                break;
            case "rb":
                if(arg2.equals("ra")) {
                    RB *= RA;
                }else {
                    try{
                        RB *= Integer.parseInt(arg2, 16);
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu ml rb");
                    }
                }
                last_result = RB;
                break;
            default:
                System.out.println("Błędne argumenty rozkazu ml");
                break;
        }
    }

    private static void dv(String arg1, String arg2){
        switch (arg1){
            case "ra":
                if(arg2.equals("rb")) {
                    if(RB != 0) RA /= RB;
                    else System.out.println("Błędny drugi argument rozkazu dv ra/rb. Dzielenie przez 0");
                }else {
                    try{
                        if(Integer.parseInt(arg2, 16) != 0) RA /= Integer.parseInt(arg2, 16);
                        else System.out.println("Błędny drugi argument rozkazu dv ra/wartość. Dzielenie przez 0");
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu mv ra");
                    }
                }
                last_result = RA;
                break;
            case "rb":
                if(arg2.equals("ra")) {
                    if(RA != 0) RB /= RA;
                    else System.out.println("Błędny drugi argument rozkazu dv rb/ra. Dzielenie przez 0");
                }else {
                    try{
                        if(Integer.parseInt(arg2, 16) != 0) RB /= Integer.parseInt(arg2, 16);
                        else System.out.println("Błędny drugi argument rozkazu dv rb/wartość. Dzielenie przez 0");
                    }catch (NumberFormatException exception){
                        System.out.println("Błędny drugi argument rozkazu mv rb");
                    }
                }
                last_result = RB;
                break;
            default:
                System.out.println("Błędne argumenty rozkazu mv");
                break;
        }
    }

    private static void jp(String address){
        p_counter = Integer.parseInt(address, 16);
    }

    private static void j0(String address){
        if(!ZF) {
            p_counter = Integer.parseInt(address, 16);
        }
    }

    private static void j1(String address){
        if(ZF) {
            p_counter = Integer.parseInt(address, 16);
        }
    }

    private static void cf(String filename){
        try {
            sFileSystem.createFile(filename);
            System.out.println("Utworzono plik: " + filename);
        } catch (FileSystem.FileSystemException e) {
            System.out.println("Błąd w trakcie tworzenia pliku!");
        }
    }

    private static void wf(String filename, String arg){
        try {
            switch (arg){
                case "ra":
                    sFileSystem.writeToFile(filename, Integer.toString(RA).toCharArray());
                    break;
                case "rb":
                    sFileSystem.writeToFile(filename, Integer.toString(RB).toCharArray());
                    break;
                default:
                    System.out.println("Błędne argumenty rozkazu wf");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Błąd w trakcie zapisu do pliku");
        }
    }

    private static void wfString(String filename, String arg){
        try {
            sFileSystem.writeToFile(filename,arg.toCharArray());
        } catch (Exception e) {
            System.out.println("Błąd w trakcie zapisu do pliku");
        }
    }

    private static void af(String filename, String arg){
        try {
            switch (arg){
                case "ra":
                    sFileSystem.appendToFile(filename, Integer.toString(RA).toCharArray());
                    break;
                case "rb":
                    sFileSystem.appendToFile(filename, Integer.toString(RB).toCharArray());
                    break;
                default:
                    System.out.println("Błędne argumenty rozkazu af");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Błąd w trakcie zapisu do pliku");
        }
    }

    private static void afString(String filename, String arg){
        try {
            sFileSystem.appendToFile(filename,arg.toCharArray());
        } catch (Exception e) {
            System.out.println("Błąd w trakcie zapisu do pliku");
        }
    }

    private static void rf(String filename){
        char[] data;
        try {
            data = sFileSystem.readFromFile(filename);
            System.out.println("Wczytano plik: " + filename);
            System.out.println("Zawartość pliku: " + String.valueOf(data));
        } catch (FileSystem.FileSystemException e) {
            System.out.println("Błąd w trakcie odczytywania pliku");
        }
    }//nie testowane

    private static void ot(String arg){
        switch (arg){
            case "ra":
                System.out.println(Integer.toString(RA));
                break;
            case "rb":
                System.out.println(Integer.toString(RB));
                break;
            case "ZF":
                System.out.println(Boolean.toString(ZF));
                break;
            default:
                System.out.println(arg);
                break;
        }
    }

    //!!!
    private static void xc(String name, PCB process) throws Exception {
        //char[] content = sFileSystem.readFromFile(programName);
        //PCB newProcess =
        sProcessManager.InitProcessAsNew(name, process.getPgid(), 0);


        //newProcess.setmProgramName(programName);
        //if(newProcess.getMemUsed()!=PCB.NO_MEMORY)
        //{
        //   for(int i = 0; i < content.length; ++i)
        //  sMemory.memorySet(newProcess.getMemAdr() + i, content[i]);
        //}


        //System.out.println
        //process.setState(PCB.State.Ready);
        //newProcess.setState(PCB.State.Ready);
    } //TODO dodać poprawną wielkosc programu, zweryfikować

//    private static void xd(){
//        try {
//            PCB process =sScheduler.getRunningProces();
//            process.setProgramCounter(process.getMemRequired()); //licznik rozkazów idzie na koniec programu
//            sMemory.memoryDealloc(process.getMemRequired(), process.getMemAdr(), process); //dealokacja pamieci
//            sProcessManager.ProcessTerminate(sScheduler.getRunningProces());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }//nie testowane

    private static void xr(PCB process){
        //System.out.println("Komunikat procesu " + process.getProcessName() + ": " + sCommunication.read(process));
        sCommunication.read(process);//samo wypisuje na ekran
    }//nie testowane

    private static void xs(String receiverName, String text, PCB currentProcess){
        try {
            sCommunication.send(receiverName, text, currentProcess);
        } catch (Exception e) {
            e.printStackTrace();
        }
//Rozpatrzy sytacje gdy  nie ma pamieci dla  komunikatu

    }// nie testowane

    private static void xy(String name, String programName){
        try {
            //if(ProcessManager.findProcess(name).getState()==PCB.State.New)
            //ProcessManager.findProcess(name).setState(PCB.State.Ready);
            //xy ma uruchamiac tylko procesy NEW !!!
            if(sProcessManager.findProcess(name).getState()==PCB.State.New) {
                char[] content = sFileSystem.readFromFile(programName);
                PCB newProcess = sProcessManager.findProcess(name);
                newProcess.setmProgramName(programName);
                newProcess.setMemRequired(content.length);

                //tu sie rozstrzygnie czy ma dostac waiting
                newProcess.setMemAdr(sProcessManager.getMemory().memoryAlloc(newProcess.getMemRequired(), newProcess));

                //jak nie dostanie waiting to tu sie zmieni na ready
                if (newProcess.getState() == PCB.State.New) {
                    newProcess.setState(PCB.State.Ready);
                    for (int i = 0; i < content.length; ++i)
                        sMemory.memorySet(newProcess.getMemAdr() + i, content[i]);
                }
                else
                {
                    System.out.println("Proces zostal juz wczesniej uruchomiony");
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }//nie testowane

    private static void xz(PCB process){
        try {
            process.setState(PCB.State.Terminated);
                 process.setProgramCounter(process.getMemRequired()); //licznik rozkazów idzie na koniec programu
                  sMemory.memoryDealloc(process.getMemRequired(), process.getMemAdr(), process); //dealokacja pamieci
        } catch (Exception e) {
            e.getMessage();
        }
    }//nie testowane

}
