package scheduler;

import interpreter.Interpreter;
import process_management.PCB;
import process_management.ProcessManager;
//import shellpackage.Shell;

/**
 * 1. POLA
 * 2. KONSTRUKOR
 * 3. METODY PRYWATNE
 * 4.METODY PUBLICZNE
 *  4.1 NEWREADY PROCES
 *  4.2 FINDANDRUN
 *  4.3 VOPERARIONOCCURE
 */
public class Scheduler {


    ///POLA////////////////////////////////////
    ///////////////////////////////////////////

    //Nad tymi staticami się jeszcze zastanowie
    private static boolean sNotExistReadyProces =false; //Flaga określająca czy jest coś do sprawdzenia
    private static boolean sNewReadyProces =false;      //Wymagane ponowne sprawdzenie Procesów na liście
    private  PCB mRunning;         //Obecnie wykonywany proces
    private  PCB mFinalTry;        //Proces który zostanie sprawdzony po jako ostatni
    private  PCB mNextTry;         //Kolejne próby
    private  int mTimeQuantum;     //Liczba rozkazów jakie nalezy wykonać zanim nastąpi przejście do następnego procesu
    private  int mRemainTimeQuantum;//Pozostały kwant czasu


    ///KONSTRUKKTORY///////////////////////////
    ///////////////////////////////////////////
    public Scheduler(int mTimeQuantum)//Dodać wyjątki
    {
        mRunning =null;
        mFinalTry =null;
        mNextTry =null;
        if(mTimeQuantum<=0)
            throw new ArithmeticException("Niedozwolony kwant czasy <=0");
        else
        {
            this.mTimeQuantum = mTimeQuantum;
            mRemainTimeQuantum = mTimeQuantum;
        }
    }

    ///METODY PRYWATNE/////////////////////////
    ///////////////////////////////////////////
    private void Run()
    {
        boolean wasTerminated=false;
        //Ustawnienie mRunning na wyszukany gotowy proces jeśli został właśnie wytypowany
        if (mRemainTimeQuantum == mTimeQuantum)//Jeśli zostałprzydzilony świeży kwant czasu
        {
            try
            {
                if(mNextTry==ProcessManager.findProcess(1))
                    mRemainTimeQuantum=1;
            }
            catch(Exception e)
            {
                System.out.println("Błąd procesu pustego w metodzie run");
            }

            mRunning = mNextTry;
            if( mRunning.getState()==PCB.State.Terminated )
            {
                mRemainTimeQuantum=1;
                wasTerminated=true;
            }
            else
            {
                mRunning.setState(PCB.State.Active);                //Przejście z stanu Ready do Run
            }
            mNextTry = mRunning.getPrevAll();
            find();
        }
        // KOLEJNOSC TAKA A NIE INNA PONIEWAZ NIE WIEM CO SIE BEDZIE DZIAŁO W EXECUTE WIĘKSZE BEZPIECZEŃSTWO
        mRemainTimeQuantum--;                                   //Zmniejszenie pozostałego kwantu czasu

        Interpreter.executeInstruction(mRunning);               //Wykonaie 1 rozkazu//Kwestia uzgodnienia jak bedzie uruchamiany proces
        inform();
        if(!wasTerminated)
        {
            if(mRunning.getState()!=PCB.State.Active)                //Wyjście jeśli proces został zablokowany lub syało się cos innego
                return;
            if(mRemainTimeQuantum==0)                                 //Sprawdzenie czy czas się nie skończył
                mRunning.setState(PCB.State.Ready );                      //Zmiana stanu na Ready aby nie było 2 procesów w stanie Active
        }
        else
        {
            mNextTry=mRunning.getPrevAll();
        }
    }


    private void find()
    {
        if(mNextTry==null)
        {
            mNextTry=ProcessManager.sAllProcessList.get(0);//Ustawienie na pierwszy Proces PROCES PUSTY
        }
        if(!sNotExistReadyProces)                           //Sprawdzenie czy są jakieś aktywne procesy
        {
            mFinalTry=mNextTry;                             //Ustawienie procesu który zostanie sprawzoy jako ostatni;
            do
            {
                if(PCB.State.Ready==mNextTry.getState()||mNextTry.getState()==PCB.State.Active)//||mNextTry.getState()==PCB.State.Terminated)//Sprawdzenie czy proces jest aktywny lub w stanie gotowości
                {
                    try {
                        if (mNextTry==ProcessManager.findProcess(1))
                        {
                            mNextTry=mNextTry.getPrevAll();         //Przeskakiwanie Procesu Pustego
                        }
                        else
                        {
                            return;                             //wyjscie z metody
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("Błąd aktualnego procesu");
                    }

                }
                else
                {

                    //      System.out.println(mNextTry.getProcessName()+" jest wstrzymany.");//Debug
                    mNextTry=mNextTry.getPrevAll();            //Przejście do sprawdzenia kolejnego

                }

            }while(mNextTry!=mFinalTry);//Sprawdzam tak długo aż nie sprawdzę wszystkich

        }
        //Jeśli nie znalazło aktywnych procesów przydziela procesor procesowi pustemu
        try {
            mNextTry = ProcessManager.findProcess(1);
        }
        catch (Exception e)
        {
            System.out.println("BLAD Procesu Pustego");
        }

        sNotExistReadyProces = true;//Ustawiam fllagę sNotExistReadyProces na true ponieważ nie ma żadanych procesów
        //System.out.println("Nie znaleziono żadnych gotowych procesów. NextTry = EmptyProces");//Debug
    }


    ///METODY PUBLICZNE////////////////////////
    ///////////////////////////////////////////
    public static void newReadyProces() //Uruchamiana gdy pojawi się nowy aktywny proces
    {

        sNewReadyProces =true;
        if(sNotExistReadyProces)
        {
            sNotExistReadyProces = false;//zmienia flagę
        }
    }


    public void findAndRun()
    {
        if (!ProcessManager.sAllProcessList.isEmpty())
        {
            if (mRunning == null)//Przypadek graniczny pierwsze uruchomienie
            {
                find();
            }
            else //Normalna Praca
            {

                if (sNewReadyProces)
                {
                    mNextTry = mRunning.getPrevAll();//Ponowne szukanie kandydata
                    find();                     //Szukanie
                    sNewReadyProces =false;
                    informNewReadyProces();
                    if(mRemainTimeQuantum==0)
                        informWhatNext();
                }
                if (mRunning.getState() != PCB.State.Active)//Przypadek gdy wyczerpał się kwant czasu lub wystąpiła operacja P
                {
                    mRemainTimeQuantum = mTimeQuantum;//Odnowienie kwantu

                    if(mNextTry.getState()!= PCB.State.Ready)
                        find();
                }
            }
            //URUCHAMIAMY WYTYPOWANY PROCES
            Run();

        }

    }
    ///GETERY i SETERY////////////////////////

    public PCB getRunningProces()//Zwraca uruchominy proces
    {
        return mRunning;
    }

    public PCB getNextTry()//Zwtaca proces który zostanie wybany jako następny
    {
        return mNextTry;
    }

    public int getTimeQuantum()//Zwaraca Ustawiony kwant czasy
    {
        return mTimeQuantum;
    }

    public int getReaminTimeQuantum(){
        return mRemainTimeQuantum;
    }

    public static boolean getNotExistReadyProces()//Ustawia flage brak aktywnych procesów (Oprócz pustego)
    {
        return sNotExistReadyProces;
    }

    public void informNewReadyProces()
    {
        //System.out.println("Nowy proces przeszedł w stan Ready");
    }
    public void informWhatNext()
    {
        System.out.println("Nastęny kandydat do uruchomienia "+mNextTry.getProcessName());

    }

    private void inform() {

        System.out.println("Obecnie wykonywany proces "+mRunning.getProcessName());
        System.out.println("Pozostały kwant czasu "+ mRemainTimeQuantum);
        System.out.println("Nastęny kandydat do uruchomienia "+mNextTry.getProcessName());
        System.out.println("--------------------------------------");
    }

}