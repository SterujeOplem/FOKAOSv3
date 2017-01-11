package memory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import semaphores.Semaphore;
import filesystem.FileSystem;
import process_management.*;

public class Memory {

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private char[] memory = new char[256]; //Nasza pamięć
    private List<FSB> listFree = new ArrayList<FSB>(); //Listy wolnych i zajetych bloków FSB
    private List<FSB> listTaken = new ArrayList<FSB>();

    public Semaphore getmMemorySemaphore() {
        return mMemorySemaphore;
    }

    private Semaphore mMemorySemaphore = new Semaphore("memorySemamphore", 0);

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public Memory(){
        FSB RAM = new FSB();
        RAM.setBegin(0);
        RAM.setSize(256);

        for (int i=0;i<memory.length;i++)
            memory[i]='#';

        listFree.add(RAM);
    }

    //Settery i gettery
    public char memoryGet(int index) {
        try {
            return (memory[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Niepoprawny parametr, rozmiar tablicy to: " + memory.length);
        }
        return '!';
    }

    public void memorySet(int index, char value) {
        try {
            memory[index] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Błąd " + e.getMessage());
        }
    }

    //SPESZYL FOR PAULINA
    //ZAkładam, ze wcześniej zaalokowaliście pamięc ;)

    public void memorySetString(int start, String value) {
        try {
            char arr[] = value.toCharArray();
            for ( int i=0;i< arr.length ; i++)
            {
                memory[start] = arr[i];
                start++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Błąd " + e.getMessage());
        }
    }

    public boolean isSize(int size)
    {
        for(int i=0;i<listFree.size();i++)
        {
            if(size<=listFree.get(i).getSize())
                return  true;
        }
        return false;
    }
    public boolean isSize(int size, PCB process)
    {
        for(int i=0;i<listFree.size();i++)
        {
            if(size<=listFree.get(i).getSize())
                return  true;
        }
        mMemorySemaphore.P(process);
        return false;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

//    Alokacja pamięci, first fit
//    ARG : Ilość pamięci do zaalokowana
//    RET : Pierwszy indeks tablicy-pamięci

    //int memoryAlloc(int size,PCB Process) {

    public int memoryAlloc(int size, PCB process) {



        for (FSB blok : listFree) {

            if (blok.getSize() >= size) {

                int pom = blok.getBegin();

                listManage(size,blok.getBegin(),listTaken);

                blok.setBegin(blok.getBegin() + size);
                blok.setSize(blok.getSize() - size);

                if (blok.getSize() == 0) {
                    listFree.remove(blok);
                }
                //  mMemorySemaphore.V();
                return pom;
            }

        }
        mMemorySemaphore.P(process);
        System.out.println("Nie ma wolnej pamięci!");
        return -1;
        //Przeniesienie P na koniec zmiana wartosci początkowej semafora na 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //zarządzanie listami bloków
    private void listManage(int size, int start, List<FSB> list) {

        FSB buffer = new FSB();

        buffer.setBegin(start);
        buffer.setSize(size);

        list.add(buffer);

        sortList(list);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

//    dealokacja pamięci ze scalaniem bloków
//    ARG : size : Ilość pamięci do zdealokowania
//    ARG : start : Początek bloku
//    RET : NO_RET

    //void memoryDealloc(int size, int start,PCB Process) {
    public void memoryDealloc(int size, int start, PCB process) {

        //mMemorySemaphore.P(process);//Tego nie powinno byc
        sortList(listFree);
        sortList(listTaken);

        for (int i = 0; i < listTaken.size(); i++) {

            if (listTaken.get(i).getBegin() == start && listTaken.get(i).getSize() == size) {

                listManage(listTaken.get(i).getSize(), listTaken.get(i).getBegin(), listFree);

                listTaken.remove(i);
                break;
            }

//            if(listTaken.get(i).getSize() == 0)
//            {
//                listTaken.remove(i);
//            }

        }

        for (int i = 0; i < listFree.size() - 1; i++) {
            sortList(listFree);
            sortList(listTaken);//profilaktyka
            if (listFree.get(i).getBegin() + listFree.get(i).getSize() == listFree.get(i + 1).getBegin()) {

                FSB pom = new FSB();
                pom.setBegin(listFree.get(i).getBegin());
                pom.setSize(listFree.get(i).getSize() + listFree.get(i + 1).getSize());

                listFree.remove(i + 1);
                listFree.remove(i);
                listFree.add(pom);
                i--;

//                if(listFree.get(i).getSize() == 0)
//                {
//                    listFree.remove(i);
//                }

            }

        }

        for (int i = start; i < start + size; ++i)
            memory[i] = '#';

        sortList(listFree);
        sortList(listTaken);
        while (!(mMemorySemaphore.getM_Queue().isEmpty()) && isSize(mMemorySemaphore.getM_Queue().get(0).getMemRequired())) {
            try {
                PCB proc = mMemorySemaphore.getM_Queue().get(0);
                if (isSize(proc.getMemRequired(), proc)) {
                    mMemorySemaphore.V();
                    char[] content = FileSystem.getInstance().readFromFile(proc.getmProgramName());
                    proc.setMemAdr(memoryAlloc(proc.getMemRequired(), proc));
                    for (int i = 0; i < content.length; ++i)
                        memorySet(proc.getMemAdr() + i, content[i]);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());

            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Wypisywanie zawartości list FSB
    // RET : print : NO_RET
    public void memoryListsPrint() {

        if(!listTaken.isEmpty())
        {
            System.out.println("Lista wolnych");
            for (FSB blok : listFree) {
                System.out.println("Początek: " + blok.getBegin() + " Koniec: " + (blok.getBegin()+blok.getSize()-1) + " Rozmiar: " + blok.getSize());
            }
        }
        else
        {
            System.out.println("Lista wolnych jest pusta.");
        }

        if(listTaken.size()>1)
        {
            System.out.println("Lista zajetych");
            for (FSB blok : listTaken) {
                if(blok.getSize()==0)
                {
                   // System.out.println("Początek: 0 Koniec: 0 Rozmiar: 0");
                }
                else
                {
                    System.out.println("Początek: " + blok.getBegin() + " Koniec: " + (blok.getBegin()+blok.getSize()-1) + " Rozmiar: " + blok.getSize());
                }

            }
        }

        else
        {
            System.out.println("Lista zajetych jest pusta.");
        }
    }

    public void memoryPrint(){
        for(int i = 0; i < memory.length; ++i){
            if(i%16==0&&i!=0) System.out.println();
            if(i%64==0&&i!=0) System.out.println();
            if(i%8==0&&!(i%16==0))
            {
                System.out.print(" ");
            }
            if(i%16==0)
                System.out.print(i+"\t|");
            System.out.print(memory[i]);
            if(i%16==15) System.out.print("| "+i);

        }
        System.out.println();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Funkcja sortujaca listę FSB
    // ARG : lista do posortowania
    // RET : NO_RET
    private void sortList(List<FSB> list) {
        Collections.sort(list, new Comparator<FSB>() {
            @Override
            public int compare(FSB o1, FSB o2) {
                return o1.getBegin() - o2.getBegin();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}
