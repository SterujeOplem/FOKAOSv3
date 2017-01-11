package filesystem;

import memory.Memory;
import process_management.PCB;
import process_management.ProcessManager;

import java.util.BitSet;
import java.util.HashMap;

/**
 * Created by ja on 2016-10-31.
 */

/**
 * Klasa jest sinngletonem, instancje otrzymujemy standardowo przez get
 * */
public class FileSystem {

    /***** SYMBOLIC CONSTANTS *****/
    private static final int SECTOR_SIZE = 30;
    private static final int SECTORS_QUANTITY = 32;
    private static final boolean SECTOR_FREE = false;
    private static final boolean SECTOR_OCCUPIED = true;
    private static final int ALL_SECTORS_OCCUPIED = -1;

    /***** MEMBERS *****/
    private BitSet mFreeSectors = new BitSet(32);
    private Sector[] mHardDrive = new Sector[32];
    private HashMap<String,File> mRootDirectory = new HashMap<String,File>();
    private static FileSystem sInstance = null;

    //debug help - may be moved to other place
    public FileSystemDebugHelper mDebugHelper = new FileSystemDebugHelper();

    public static FileSystem getInstance(){
        if(sInstance == null){
            sInstance = new FileSystem();
        }
        return sInstance;
    }

    private FileSystem(){
        for(int i =0; i < SECTORS_QUANTITY; ++i){
            mHardDrive[i] = new Sector();
        }

        try{
            createFile("p1");
            writeToFile("p1", ("mv ra,06" +
                    "mv rb,ra" +
                    "sb rb,01" +
                    "ml ra,rb" +
                    "sb rb,01" +
                    "j0 18" +
                    "cf plik$" +
                    "wf plik,wynik 6! $" +
                    "af plik,wynosi $" +
                    "af plik,ra" +
                    "rf plik$" +
                    "xz"
                    ).toCharArray());
            createFile("p2");
            writeToFile("p2", ("mv ra,00" +
                    "mv rb,00" +
                    "jp 25" +
                    "mv ra,16" +
                    "mv rb,08" +
                    "xc program$" +
                    "xy program,p3$" +
                    //"mv ra,02"+
                    //"mv ra,02"+
                    //"mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
//                    "mv ra,02"+
                    "xs program,hello$" +
                    "xs program,hello2$" +
                    "xz"
                    ).toCharArray());
            createFile("p3");
            writeToFile("p3",("mv ra,02" +
                    "mv rb,01" +
                    "ml ra,rb" +
                    "j1 00" +
                    //!!!
                    //"mv ra,02"+
                    //"mv ra,02"+
                    //"mv ra,02"+
                    //"mv ra,02"+
                    //"mv ra,02"+
                    //"mv ra,02"+
                    "xr" +
                    "cf plik2$" +
                    "xz"
                    ).toCharArray());
        }
        catch(Exception e){}

    }

    /***** CLASS INTERFACE/API *****/


    /**
     * Zapisuje dane do pliku - w przypadku gdy plik istniał wcześniej, plik musi być utworzony przd wywołaniem.
     * Zgłaszane Wyjątki:
     * - w sytuacji gdy plik nie istnieje
     * - w sytuacji gdy nie ma na dysku dość miejsca by zapisać plik
     * W przypadku braku miejsca mozna zapisyawc to co sie zmieści i zglosić, ze cos sie nie zmieściło
     * albo nie zaczynać zapisu i zglosić, że sie nie zmieści - jak widać powyżej wybrano to drugie.*/
    public void writeToFile(String filename, char[] data) throws FileSystemException{
        File currentFile = mRootDirectory.get(filename);
        if(currentFile == null) throw new FileSystemException("File does not exist");
        clearFileContent(currentFile);

        int sectorsRequired = (int) Math.ceil(data.length/(double)SECTOR_SIZE);

        if(getAvailableSectorsQuantity() + 1 < sectorsRequired) //+1 - every file receives one on creating
            throw new FileSystemException("Not enough free space on drive to save file");

        int remainingDataToWrite = data.length;
        for(int i = 0; i < sectorsRequired; ++i){
            int nextSector;
            if(i==0) {
                nextSector = currentFile.getFirstSectorIndex();
            }
            else {
                nextSector = getFreeSectorIndex();
                mFreeSectors.set(nextSector, SECTOR_OCCUPIED);
            }

            mHardDrive[currentFile.getLastSectorIndex()].setNextSector(nextSector);

            int currentChunkSize;
            if(remainingDataToWrite >= SECTOR_SIZE) {
                currentChunkSize = SECTOR_SIZE;
                remainingDataToWrite -= SECTOR_SIZE;
            }
            else{
                currentChunkSize = remainingDataToWrite;
                remainingDataToWrite = 0;
            }

            char[] tmpData = new char[currentChunkSize];
            for(int j = 0; j < currentChunkSize; ++j)
                tmpData[j] = data[i * SECTOR_SIZE + j];

            mHardDrive[nextSector].setData(tmpData);

            currentFile.setLastSectorIndex(nextSector);
        }

        currentFile.setSize(data.length);
    }

    /**
     * Czyta całą zawartość pliku
     * Zgłaszane wyjątki:
     * - w sytuacji gdy plik nie istnieje
     * */
    public char[] readFromFile(String filename) throws FileSystemException{
        File currentFile = mRootDirectory.get(filename);
        if(currentFile == null) throw new FileSystemException("File does not exist");

        int sectorsRequired = (int) Math.ceil(currentFile.getSize()/(double)SECTOR_SIZE);
        int remainingDataToRead = currentFile.getSize();

        //tymczasowy bufor, trochę nieralistyczny ale dogadany z dr. B.
        char[] dataToReturn = new char[currentFile.getSize()];

        int currentSector = currentFile.getFirstSectorIndex();
        for(int i = 0; i < sectorsRequired; ++i){
            char[] currentSectorData = mHardDrive[currentSector].getData();
            if(remainingDataToRead >= SECTOR_SIZE){
                for(int j = 0; j < SECTOR_SIZE; ++j)
                    dataToReturn[i * SECTOR_SIZE +j] = currentSectorData[j];
                remainingDataToRead -= SECTOR_SIZE;
            }
            else{
                for(int j =0; j < remainingDataToRead; ++j)
                    dataToReturn[i * SECTOR_SIZE +j] = currentSectorData[j];
                remainingDataToRead = 0;
            }

            currentSector = mHardDrive[currentSector].getNextSector();
        }

        return dataToReturn;
    }

    /**
     * Tworzy plik, przy okazji rezerwując dla niego jeden sektor na dane
     * Zgłaszane wyjątki:
     * - w sytuacji, gdy plik o takiej nazwie już istnieje - sam plik pozostaje wtedy bez zmian
     * - w sytuacji, gdy wszystkie sektory są już zajęte
     * */
    public void createFile(String filename) throws FileSystemException{
        if(mRootDirectory.get(filename) != null)
            throw new FileSystemException("File already exist on disc");

        int firstSectorIndex = getFreeSectorIndex();
        if(firstSectorIndex == ALL_SECTORS_OCCUPIED)
            throw new FileSystemException("All disc sectors occupied - file can not be created");

        mFreeSectors.set(firstSectorIndex,SECTOR_OCCUPIED);
        File newFile = new File(filename,firstSectorIndex,firstSectorIndex, 0);
        mRootDirectory.put(filename,newFile);
    }

    /**
     * Uswua plik - zarówno odnośnik do niego w katalogu jak i miejsce w którym znajdowała się jego zawartość
     * Zgłaszane wyjątki:
     * - w sytuacji gdy plik do usunięcia nie istnieje
     * */
    public void deleteFile(String filename) throws FileSystemException{
        File fileToDelete = mRootDirectory.get(filename);
        if(fileToDelete == null) throw new FileSystemException("File to delete does not exist");

        clearFileContent(fileToDelete);
        mRootDirectory.remove(filename);
    }

    /**
     * Dodaje podany w argumencie content na końcu pliku, nie modyfikując dotychczasowej zawartości
     * Zgłaszane wyjątki:
     * - w przypadku, gdy podany plik nie istnieje
     * - w przypadku gdy nie wa wystarczającej ilości miejsca by dodać do pliku żądane dane
     * */
    public void appendToFile(String filename, char[] data) throws FileSystemException{
        File currentFile = mRootDirectory.get(filename);
        if(currentFile == null)
            throw new FileSystemException("File does not exist");

        int freeSpaceInLastSector = SECTOR_SIZE - currentFile.getSize() % (SECTOR_SIZE + 1);
        int sectorsRequired = (int) Math.ceil((data.length - freeSpaceInLastSector)/(double)SECTOR_SIZE);
        if(getAvailableSectorsQuantity() < sectorsRequired)
            throw new FileSystemException("Not enough free space to append requested data to file");

        int currentSector = currentFile.getLastSectorIndex();
        char[] tmpData = mHardDrive[currentSector].getData();

        int srcDataIndex = 0;
        for(int i = 0; i < freeSpaceInLastSector && i < data.length; ++i) {
            tmpData[SECTOR_SIZE - freeSpaceInLastSector + i] = data[srcDataIndex++];
        }

        mHardDrive[currentSector].setData(tmpData);

        int remainingDataToWrite = data.length - freeSpaceInLastSector;

        for(int i = 0; i < sectorsRequired; ++i){
            int nextSector = getFreeSectorIndex();
            mHardDrive[currentSector].setNextSector(nextSector);

            int currentChunkSize;
            if(remainingDataToWrite >= SECTOR_SIZE) {
                currentChunkSize = SECTOR_SIZE;
                remainingDataToWrite -= SECTOR_SIZE;
            }
            else{
                currentChunkSize = remainingDataToWrite;
                remainingDataToWrite = 0;
            }

            tmpData = new char[currentChunkSize];
            for(int j = 0; j < currentChunkSize; ++j)
                tmpData[j] = data[srcDataIndex++];

            mHardDrive[nextSector].setData(tmpData);
            currentFile.setLastSectorIndex(nextSector);
        }

        currentFile.setSize(currentFile.getSize() + data.length);
    }

    /**
     * Zwraca rozmiar pliku na dysku
     * W przypadku gdy plik nie istnieje zwracana wartość wynosi -1*/
    public int getFileSize(String filename){
        File f = mRootDirectory.get(filename);
        return f != null ? f.getSize() : -1;
    }


    /***** AUXILIARY METHODS *****/

    private int getFreeSectorIndex(){
        for(int i = 0; i < SECTORS_QUANTITY; ++i)
            if(mFreeSectors.get(i) == SECTOR_FREE) return i;
        return ALL_SECTORS_OCCUPIED;
    }

    private void clearFileContent(File fileToClear){

        while(fileToClear.getFirstSectorIndex() != fileToClear.getLastSectorIndex()){
            int nextSectorIndex = mHardDrive[fileToClear.getFirstSectorIndex()].getNextSector();
            mHardDrive[fileToClear.getFirstSectorIndex()].clearSector();
            mFreeSectors.set(fileToClear.getFirstSectorIndex(),SECTOR_FREE);
            fileToClear.setFirstSectorIndex(nextSectorIndex);
        }

        mHardDrive[fileToClear.getLastSectorIndex()].clearSector();
    }

    private int getAvailableSectorsQuantity(){
        int freeSectors = 0;
        for(int i = 0; i < SECTORS_QUANTITY; ++i)
            if(mFreeSectors.get(i) == SECTOR_FREE)
                ++freeSectors;
        return freeSectors;
    }

    /***** HELP/TESTING FUNCTIONS THAT PROBABLY WILL BE IMPLEMENTED IN OTHER PLACE *****/

    public static void main(String[] args){

        //FileSystem fs = new FileSystem();
        FileSystem fs = FileSystem.getInstance();
        //try {
            /*
            fs.createFile("test");
            //fs.createFile("test");
            //fs.deleteFile("test");
            //fs.deleteFile("test");
            //fs.createFile("test");
            fs.writeToFile("test",new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaabb").toCharArray());
            System.out.println(fs.readFromFile("test"));
            //fs.appendToFile("test",new String("lololo").toCharArray());
            fs.writeToFile("test",new String("lololo").toCharArray());
            System.out.println(fs.readFromFile("test"));
            fs.createFile("test2");
            fs.writeToFile("test2",new String("ccccccccccccc").toCharArray());
            System.out.println(fs.readFromFile("test2"));
            fs.writeToFile("test2",new String("cccccccccccccaa").toCharArray());
            System.out.println(fs.readFromFile("test2"));
            //fs.deleteFile("test");
            fs.appendToFile("test2","88888".toCharArray());
            fs.appendToFile("test2","88888".toCharArray());
            fs.appendToFile("test2","88888".toCharArray());
            fs.appendToFile("test2","88888".toCharArray()); //kuj, tylko append, nie write
            fs.mDebugHelper.printDiscDataState();
            fs.mDebugHelper.listFiles();
            System.out.println(fs.mDebugHelper.generateRandomFileContent(10));
            */
        fs.mDebugHelper.listFiles();
        fs.mDebugHelper.printDiscDataState();
        // }
        //catch(FileSystem.FileSystemException e){
        //    System.out.println(e);
        //}
    }

    /***** INNER AUXILIARY CLASSES *****/

    private class Sector {

        private char[] mData = new char[30];
        private char[] mNextSector = new char[2];

        public Sector(){
            clearSector();
        }

        public void setData(char[] newData){
            char[] tmpData = new char[SECTOR_SIZE];
            for(int i = 0; i < newData.length; ++i) tmpData[i] = newData[i];
            for(int i = newData.length; i < SECTOR_SIZE; ++i) tmpData[i] = '#';
            mData = tmpData;
        }

        public char[] getData(){ //knw
            return new String(mData).toCharArray();
        }

        public void setNextSector(int newNextSector){
            char[] tmpArr = Integer.toString(newNextSector).toCharArray();
            if(tmpArr.length < 2) {
                char tmpChar = tmpArr[0];
                tmpArr = new char[tmpArr.length + 1];
                tmpArr[0] = '0';
                tmpArr[1] = tmpChar;
            }
            mNextSector = tmpArr;
        }

        public int getNextSector(){ //ok bo jako koniec pliku i w wolnych esktorac jest -1
            return Integer.parseInt(new String(mNextSector));
        }

        public void clearSector(){
            mNextSector[0] = '-';
            mNextSector[1] = '1';
            for(int i =0; i < mData.length; ++i) mData[i] = '#';
        }

    }

    private class File{

        private String mFilename;
        private int mFirstSectorIndex;
        private int mLastSectorIndex;
        private int mSize;

        public File(String filename, int firstSectorIndex, int lastSectorIndex, int size) {
            mFilename = filename;
            mFirstSectorIndex = firstSectorIndex;
            mLastSectorIndex = lastSectorIndex;
            mSize = size;
        }

        public String getFilename() {
            return mFilename;
        }

        public void setFilename(String filename) {
            mFilename = filename;
        }

        public int getFirstSectorIndex() {
            return mFirstSectorIndex;
        }

        public void setFirstSectorIndex(int firstAUIndex) {
            mFirstSectorIndex = firstAUIndex;
        }

        public int getLastSectorIndex() {
            return mLastSectorIndex;
        }

        public void setLastSectorIndex(int lastAUIndex) {
            mLastSectorIndex = lastAUIndex;
        }

        public int getSize() {
            return mSize;
        }

        public void setSize(int size) {
            mSize = size;
        }

    }

    /**
     * Standardowy wyjątek, wprowadzony tylko w celu możliwości szczegółowego łapania po typie
     * */
    public class FileSystemException extends Exception{

        public FileSystemException(String message) {
            super(message);
        }

        public FileSystemException(String message, Throwable throwable) {
            super(message, throwable);
        }

        public String getMessage(){
            return super.getMessage();
        }

    }

    /**
     * Klasa na potrzeby wyświetlania zawartości dysku, wyswietlania obecnego stanu plików etc. etc.
     * */

    public class FileSystemDebugHelper {

        public void listFiles(){
            for(File f : mRootDirectory.values()){
                System.out.println("File: " + f.getFilename() + " Size: " + f.getSize());
                int currentIndex = f.getFirstSectorIndex();
                System.out.print("Sectors: " + currentIndex + " -> ");
                while(currentIndex != f.getLastSectorIndex()){
                    currentIndex = mHardDrive[currentIndex].getNextSector();
                    System.out.print(currentIndex + " ->");
                }
                System.out.println("END");
            }
        }

        public void printDiscDataState(){
            for(int i = 0; i < SECTORS_QUANTITY; ++i){
                System.out.println("Sector no. " + i);
                for(int j = 0; j < SECTOR_SIZE; ++j){
                    System.out.print(mHardDrive[i].getData()[j]);
                    if((j+1)%6 == 0) System.out.println("");
                }
                System.out.println("");
            }
        }

        public char[] generateRandomFileContent(int size){
            char[] result = new char[size];
            for(int i = 0; i < size; ++i)
                result[i] = (char) (Math.random() * 5 + 97);
            return result;
        }
    }
}



/**
 * Otwarte tematy:
 * - czytanie fragmentu pliku (Jurek kazał olać, podobnie jak synchronizacje, katalogi, atrybuty i wiele innych ; p)
 * - tworzenie pliku poprzez podanie rozmiaru, przyda się do testowania B., chłopaki też chciele
 * Z drugiej strony nie jest to logiczne, wiec tymczasowo dostaną nakładkę która będzie generwoać plik o podanym
 * rozmiarze
 * - czy przy usuwaniu pliku usuwać też jego "doczesne szczątki"
 * - dopisanie unit testów
 * */