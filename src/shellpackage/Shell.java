package shellpackage;

import memory.Memory;
import process_management.PCB;
import process_management.ProcessManager;
import filesystem.FileSystem;

/**
 * Created by Damian on 10.11.2016.
 */
public class Shell {

    private FileSystem mFileSystem;
    private ProcessManager mProcessManager;
    private Memory mMemory;

    public Shell(FileSystem fileSystem, ProcessManager manager, Memory memory){
        mFileSystem = fileSystem;
        mProcessManager = manager;
        mMemory = memory;
    }

    public void println(String text){
        System.out.println(text);
    }

    public void executeCommand(String text){
        /*szukanie separatora/spacji*/
        int command_separator = text.indexOf(" "); //separator = -1 jeżeli nie znaleziono separatora/spacji
        /*Zapis odczytanej komendy do zmiennej*/
        String command;
        String args = new String();
        if(command_separator != -1) {
            command = text.substring(0, command_separator);
            args = text.substring(command_separator+1, text.length());
        }
        else command = text;



        switch (command)
        {
            case "xy":
                try {
                    String[] tmp = args.split(" ");
                    String processName=tmp[0];
                    String programName=tmp[1];

                    //xy ma uruchamiac tylko procesy NEW !!!
                    if(ProcessManager.findProcess(processName).getState()==PCB.State.New) {
                        loadProgramToMemory(programName,processName);
                    }
                    else
                        System.out.println("Nie można wykonać. Stan inny od NEV.");
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case "xd":

                try
                {
                    PCB process=mProcessManager.findProcess(args);
                    process.setState(PCB.State.Terminated);
                    process.setProgramCounter(process.getMemRequired()); //licznik rozkazów idzie na koniec programu
                    if(process.getMemUsed()!=0) {
                        mMemory.memoryDealloc(process.getMemRequired(), process.getMemAdr(), process); //dealokacja pamieci
                    }
                    mProcessManager.ProcessTerminate(process);
                }
                catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }
                break;
            case "cf": //todo tworzy plik o zadanej wielkości i nazwie
                int size;
                if(args.isEmpty())break;
                size = Integer.parseInt(args.substring(args.indexOf(" ")+1, args.length()));
                try {
                    mFileSystem.createFile(args.substring(0, args.indexOf(" ")));
                } catch (FileSystem.FileSystemException e) {
                    e.printStackTrace();
                }
                println("Utworzono plik \"" + args.substring(0, args.indexOf(" ")) + "\", wielkość:  " + size + " bajtów");
                break;
            case "rf": //todo tworzy plik o zadanej wielkości i nazwie
                if(args.isEmpty())break;

                try {
                    System.out.println(mFileSystem.readFromFile(args));
                } catch (FileSystem.FileSystemException e) {
                    println(e.getMessage());
                }
                break;
            case "df":
                try {
                    mFileSystem.deleteFile(args);
                    println("Usunięto plik \"" + args + "\"");
                } catch (FileSystem.FileSystemException e) {
                    println(e.getMessage());
                }
                break;

            case "db_memory_lists"://todo
                //println("Tutaj powinna być wyświetlona pamięć operacyjna");
                mMemory.memoryListsPrint();
                break;
            case "db_memory":
                mMemory.memoryPrint();
                break;
            case "disc"://todo
                println("Tutaj powinna być wyświetlona pamięć dyskowa");
                break;
            case "currprocess"://todo
                println("Proces PID: ...");
                break;
            //runprog nazwa_prg_dysk, nazwa_procesu, grupa_procesowa
            case "runprog":
                String[] tmp = args.split(" ");
                try {
                    createProcess(tmp[0],Integer.parseInt(tmp[1]));
                    //loadProgramToMemory(tmp[0], tmp[1], Integer.parseInt(tmp[2]));
                }
                catch(Exception e){
                    System.out.println(e);
                }
                break;
            //debug/prezentacja dla J.B.
            case "db_processlist":
                if(!mProcessManager.sAllProcessList.isEmpty()) {
                    println("Lista procesów:");
                    for (PCB process : mProcessManager.sAllProcessList) {
                        println("PID " + process.getPid() + " name :" + process.getProcessName() + " state: " +
                                process.getState());
                    }
                }else{
                    println("Lista procesów obu grup jest pusta");
                }
                break;
            case "db_processlistg1":
                if(!mProcessManager.sAllProcessList.isEmpty()) {
                    println("Lista procesów pierwszej grupy:");
                    for (PCB process : mProcessManager.sFirstGroupList) {
                        println("PID " + process.getPid() + " name :" + process.getProcessName() + " state: " +
                                process.getState());
                    }
                }else{
                    println("Lista procesów pierwszej grupy jest pusta");
                }
                break;
            case "db_processlistg2":
                if(!mProcessManager.sAllProcessList.isEmpty()) {
                    println("Lista procesów drugiej grupy:");
                    for (PCB process : mProcessManager.sSecondGroupList) {
                        println("PID " + process.getPid() + " name :" + process.getProcessName() + " state: " +
                                process.getState());
                    }
                }else{
                    println("Lista procesów drugiej grupy jest pusta");
                }
                break;
            case "db_semaphorememory":
                System.out.println("Wartosc semafora Memory: " + mMemory.getmMemorySemaphore().getM_Semaphorevalue());

                mMemory.getmMemorySemaphore().ShowQueueOnScreen();
                break;
            case "db_communicates_quantity":
                try {
                    PCB process = mProcessManager.findProcess(args);
                    System.out.println("Wiadomosci czekajace w kolejce komunikatow: " + process.mQueue.size());
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case "db_list_files":
                mFileSystem.mDebugHelper.listFiles();
                break;
            case "db_disc_state":
                mFileSystem.mDebugHelper.printDiscDataState();
                break;
            case "db_sem_state":
                try {
                    PCB process = mProcessManager.findProcess(args);
                    System.out.println(process.mMessageSemaphore.getM_Semaphorevalue());
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case "help":
                System.out.println("***System Operacyjny Foka***");
                System.out.println("Dostepne komendy:");
                System.out.println("af - Dopisanie na końcu pliku");
                System.out.println("cf - Tworzenie pliku ");
                System.out.println("df - Usuwanie pliku ");
                System.out.println("rf - Odczyt pliku");
                System.out.println("wf - Zapis do pliku");
                System.out.println("db_memory - Wyswietlanie pamieci operacyjnej ");
                System.out.println("currproces - Aktualny proces przy procesorze ");
                System.out.println("go i - wykonuje i rozkazów");
                //System.out.println("ni - Nastepny rozkaz ");  ???
                System.out.println("runprog - Utworzenie procesu ");
                System.out.println("xy - Uruchomienie procesu ");
                System.out.println("xd - Usuniecie procesu ");
                System.out.println("db_processlist - Wyswietlanie listy procesow ");
                System.out.println("db_processlistg1 - Wyswietlanie listy procesow z G1 ");
                System.out.println("db_processlistg2 - Wyswietlanie listy procesow z G2 ");
                System.out.println("db_semaphorememory - Wyswitla zawartosc semafora memory");
                System.out.println("db_communicates_quantity - Wyswietlanie ilosci komunikatow w kolejce ");
                System.out.println("db_list_files - Lista plikow ");
                System.out.println("db_disc_state - Wyswietlanie stanu przestrzenii dyskowej ");
                break;
            default:
                System.out.println("Błędna komenda");
                break;
        }
    }


    void createProcess(String processName, int groupId) throws Exception {
        PCB process = mProcessManager.InitProcessAsNew(processName, groupId,0);

    }

    void loadProgramToMemory(String filename, String processName) throws Exception{
        char[] content = mFileSystem.readFromFile(filename);
        PCB newProcess = mProcessManager.findProcess(processName);
        newProcess.setmProgramName(filename);
        newProcess.setMemRequired(content.length);
        newProcess.setMemUsed(content.length);
        newProcess.setMemAdr(mProcessManager.getMemory().memoryAlloc(newProcess.getMemRequired(),newProcess));
        if(newProcess.getState()==PCB.State.New)
        {
            newProcess.setState(PCB.State.Ready);
            for(int i = 0; i < content.length; ++i)
                mMemory.memorySet(newProcess.getMemAdr() + i, content[i]);
        }
    }
}
