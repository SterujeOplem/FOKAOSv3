/*
package memory;

public class Memory_main {

    public static void main(String[] args) {


/////////////////////////////////////////////////////////////////////DOBRY KOD///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //Inicjujemy pamięć
        Memory totalMemory = new Memory();

        // Inicjujemy pierwszy blok FSB
        FSB RAM = new FSB();
        RAM.setBegin(0);
        RAM.setSize(256);

        //Dodajemy go do wolnej listy
        totalMemory.listFree.add(RAM);

//////////////////////////////////////////////////////////////////////STREFA TESTÓW///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        totalMemory.memoryAlloc(24);
        totalMemory.memoryAlloc(48);

        System.out.println("-------------------------------------------------------------------");

        totalMemory.memoryPrint();

        System.out.println("-------------------------------------------------------------------");

        totalMemory.memoryDealloc(48, 24);
        totalMemory.sortList(totalMemory.listFree);

        totalMemory.memoryPrint();

        System.out.println("-------------------------------FIN---------------------------------");


    }

}
*/