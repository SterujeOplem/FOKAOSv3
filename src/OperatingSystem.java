import process_management.PCB;
import process_management.ProcessManager;
import scheduler.Scheduler;
import communication.Communication;
import filesystem.FileSystem;
import interpreter.Interpreter;
import memory.Memory;
import shellpackage.Shell;



import java.util.Scanner;

/***** MODUł GłÓWNY *****/

public class OperatingSystem{


    private FileSystem mFileSystem;
    private Interpreter mInterpreter;
    private Scheduler mScheduler;
    private Memory mMemory;
    private Communication mCommunication;
    private Shell mShell;
    private ProcessManager mProcessManager;

    private OperatingSystem(){
        mFileSystem = FileSystem.getInstance();
        mMemory = new Memory();
        mScheduler = new Scheduler(5);
        mProcessManager = new ProcessManager(mMemory);
        mCommunication = new Communication(mMemory);
        mInterpreter = new Interpreter(mProcessManager, mMemory, mFileSystem, mCommunication, mScheduler);
        mShell = new Shell(mFileSystem, mProcessManager, mMemory);
        try {
            PCB tmp = mProcessManager.InitProcess("pusty", 0, 0);
            tmp.setState(PCB.State.Ready);
        }
        catch (Exception e)
        {
            e.getMessage();
            System.out.println("Proces pusty nie został utworzony");
        }
    }

    private void run(){
        String command;
        Scanner sc = new Scanner(System.in);

        //ffd

        //mMemory.memoryListsPrint();
        //sc.nextLine();

        while(true){
            command = sc.nextLine();
            if(command.startsWith("go")){
                try
                {
                    int num = Integer.parseInt(command.split(" ")[1]);
                    for(int i =0; i < num; ++i)
                        mScheduler.findAndRun();
                }
                catch (Exception e)
                {
                    System.out.println("Błędna Komenda");
                }
            }
            else
                mShell.executeCommand(command);
        }
    }

    public static void main(String args[]){
        OperatingSystem os = new OperatingSystem();
        os.run();


    }

    /*
    private void jakisTestDamiana(){
        char[] MemAtrapa = new char[256];

        MemAtrapa[0] = 'm';
        MemAtrapa[1] = 'v';
        MemAtrapa[2] = ' ';
        MemAtrapa[3] = 'r';
        MemAtrapa[4] = 'a';
        MemAtrapa[5] = ' ';
        MemAtrapa[6] = '0';
        MemAtrapa[7] = '4';

        MemAtrapa[8] = 'm';
        MemAtrapa[9] = 'v';
        MemAtrapa[10] = ' ';
        MemAtrapa[11] = 'r';
        MemAtrapa[12] = 'b';
        MemAtrapa[13] = ',';
        MemAtrapa[14] = 'r';
        MemAtrapa[15] = 'a';

        MemAtrapa[16] = 's';
        MemAtrapa[17] = 'b';
        MemAtrapa[18] = ' ';
        MemAtrapa[19] = 'r';
        MemAtrapa[20] = 'b';
        MemAtrapa[21] = ',';
        MemAtrapa[22] = '0';
        MemAtrapa[23] = '1';

        MemAtrapa[24] = 'm';
        MemAtrapa[25] = 'l';
        MemAtrapa[26] = ' ';
        MemAtrapa[27] = 'r';
        MemAtrapa[28] = 'a';
        MemAtrapa[29] = ',';
        MemAtrapa[30] = 'r';
        MemAtrapa[31] = 'b';

        MemAtrapa[32] = 's';
        MemAtrapa[33] = 'b';
        MemAtrapa[34] = ' ';
        MemAtrapa[35] = 'r';
        MemAtrapa[36] = 'b';
        MemAtrapa[37] = ',';
        MemAtrapa[38] = '0';
        MemAtrapa[39] = '1';

        MemAtrapa[40] = 'j';
        MemAtrapa[41] = '0';
        MemAtrapa[42] = ' ';
        MemAtrapa[43] = '1';
        MemAtrapa[44] = '8'; //24

        MemAtrapa[45] = 'o';
        MemAtrapa[46] = 't';
        MemAtrapa[47] = ' ';
        MemAtrapa[48] = 'r';
        MemAtrapa[49] = 'a';


        Semaphore semaforPamieci = new Semaphore("SemaforPamieci", 0);
        Memory Memory = new Memory(semaforPamieci);

        Communication Communication = new Communication();
        FileSystem FileSystem = filesystem.FileSystem.getInstance();
        ProcessManager Manager = new ProcessManager();
        Interpreter interpreter = null;
        Scheduler Scheduler = new Scheduler(5, interpreter); //argument "interpreter" chyba zniknie ze wsględu na statyczne metody interpretera
        interpreter = new Interpreter(Manager, Memory, FileSystem, Communication, Scheduler);
        Shell Shell = new Shell(FileSystem, Manager);

        procesy testowe, normalnie interpreter dostaje je od schedulera, mają ten sam adres w pamieci (ten sam program)
        PCB proces1 = new PCB("proces1", 1, 100, 0);
        PCB proces2 = new PCB("proces2", 1, 100, 0);

        String Command;
        Scanner sc = new Scanner(System.in);


        //przepisanie z atrapy do prawdziwej pamieci
        PCB atrapa = new PCB();
        Memory.memoryAlloc(50, atrapa);
        for (int i = 0; i < 50; i++) {
            Memory.memorySet(i, MemAtrapa[i]);
        }
        //

        //test interpretera
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);

        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces2);
        interpreter.executeInstruction(proces1);
        interpreter.executeInstruction(proces2);

        while(true)
        {
            Command = sc.nextLine();
            Shell.executeCommand(Command);
        }
    }
    */
    /*
    - shell todo
    - semafory i p - zrobione
    - komunikaty miedzy grupami - może
    - scheduler - nastepny wiadomo o co chodzi - zrobione
    - wyzwac mp todo
    - wiecej programowamow
    - !!!FOKA W ASCI!!!
    - jest taka opcja todo
     */
    //2 TURA
    /*
    todo
    rozkaz XD - Sebastian
    wyzwac mp
    GTA V
    //TURA 3
    PRZYJZEC SIE USUSWANIU PROCESOW Z KONSOLI - POWODUJE PROBLEMY Z SCALANIEM BLOKOW
     */
}