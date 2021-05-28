 
/** 
 * Program Instructions 
 *      1. Press run it should compare on its own, both 1000 and 1000 endpoints.
 *      2. The output for 1000 starts with "Endpoint: 1000", the output for 10000 
 *         starts with Endpoint: 10 000".
 * 
 */
/* Start of AllPrimeNumbersV2 */
package  allprimenumbers.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class AllPrimeNumbersV2
 *      Purpose: This software program makes multiple threads to calculate the
 *               first 1000 prime numbers. The threads run at the same time and
 *               the first 1000 prime numbers do not have be in order. Threads 
 *               store the prime numbers and are check at the end for being 
 *               correct. After the 1000 prime numebrs have been printed the 
 *               program starts over with 10 000 prime numbers.
 *              
 * @author Norman
 */
public class AllPrimeNumbersV2  extends Thread
{    
    /* countOfDoneThreads: a variable to count threads that complete.   Gets it's own lock.  */
    private static int countOfDoneThreads = 0;      private static final Object COUNT_LOCK = new Object();
    /* EndPoint: The number of prime numbers to be created.    Gets it's own lock.*/
    private static int EndPoint = 1000;     private static final Object ENDPOINT_LOCK = new Object();
    /* howmanyworkers: The number of threads that will share the work load of producing the prime numbers. Gets it's own lock.*/
    private static final int howmanyWorkers = 10;    private static final Object HOWMANYWORKERS_LOCK = new Object();
    /* id: The id of a thread. Gets it's own lock.*/
    private final int id;   private static final Object ID_LOCK = new Object();
    /* SplitSizes: A variable to hold the size of the work load splits. Gets it's own lock. */
    private static int SplitSizes = 0;     private static final Object SPLITSIZES_LOCK = new Object();    
    /* Thread: An array of threads the size of howmanyWorkers. */
    private static Thread[] threads= new Thread[howmanyWorkers];  
    /* General_LOCK: A universal lock. */
    private static final Object GENERAL_LOCK = new Object();
    /* primeNumbersMap: A concurrentHashMap to hold store prime numbers as keys and thread ids for which thread created them. Gets it's own lock.*/
    private final static ConcurrentHashMap<Integer, Integer> primeNumbersMap = new ConcurrentHashMap<Integer, Integer>();   
    private static final Object PRIMENUMBERSMAP_LOCK = new Object();    
    
    
    
    
    /** Constructor AllPrimeNumbersV2
     *      Purpose: Passes the loop increment value in main to the id of this 
     *               thread.
     */
    AllPrimeNumbersV2(int identification) {
        synchronized(ID_LOCK){
            id = identification;
        }
    }/*End of Constructor  AllPrimeNumbersV2*/

    
    
    
    /** Method run
     *  Purpose: Produces prime numbers for for one split of the total amount
     *  of prime numbers requested.
     */
    public void run() {    
        /* Write the thread id into a variable.*/
        int threadID = 0;   synchronized(ID_LOCK){  threadID = id;  } 
        
        /*Determine Split sizes */
        int splitStart = 0; int splitEnd = 0;
        synchronized(SPLITSIZES_LOCK){  
            splitStart = threadID*SplitSizes +1;//Use thread ID to determine start of split.
            splitEnd = splitStart + SplitSizes -1;
        }        
        boolean firstLoop = true;
        int oldPRIMECOUNT = 0; 
        int oldISDISPRIME = 0;        
            for( int placement=splitStart; placement <= splitEnd; placement++) {
                int primNumber = 0;
                int primeCount;
                int isdisPrime;   
                if (firstLoop == true) {                    
                    primeCount = 0;
                    isdisPrime = 2;   
                } else {                    
                    primeCount = oldPRIMECOUNT;
                    isdisPrime = oldISDISPRIME +1;                       
                }              
                int K;
                do
                {                                  
                    boolean primeFlag = true;                                                        
                    for(K=2;K<=isdisPrime/2;K++)  
                    {
                        if(isdisPrime%K==0) 
                        {                        
                            primeFlag = false;
                            break;
                        }                        
                    }                                        
                    if(primeFlag == true)
                    {    primeCount++;  
                        primNumber = isdisPrime;                        
                    }
                    isdisPrime++;
                }while(primeCount != placement);                
                
                /*Put prime number and threadID in Map*/
                synchronized(PRIMENUMBERSMAP_LOCK)
                {
                    primeNumbersMap.put( primNumber, threadID );
                }
                              
                firstLoop = false;
                oldPRIMECOUNT = primeCount;
                oldISDISPRIME = primNumber;                   
            }
            
        letpass(threadID); //Thread is done, run letpass to determine if all threads are done.
    }/*End of Method run*/

    
    
    
    /** Method printPrimeNumberMap()
     *      Purpose: Print all Prime Numbers in primeNumberMap. 
     *               Makes an arraylist of the prime numbers produced.
     *               Calls method Determine if correct.
     */
    public static void printPrimeNumberMap() throws IOException
    {   
        ArrayList<Integer> PrimeNumbers = new ArrayList<Integer>();
        
        synchronized(PRIMENUMBERSMAP_LOCK)
        {            
            int howmanyworkers = 0; synchronized(HOWMANYWORKERS_LOCK){howmanyworkers = howmanyWorkers;}
            for (int i = 0; i < howmanyworkers; ++i){
                System.err.println("Thread: "+i);                             
                for(int key : primeNumbersMap.keySet())
                {
                    int val = primeNumbersMap.get(key);
                    if( val == i)
                    {
                        System.out.println("    Prime: "+ key);
                        PrimeNumbers.add(key);
                    }
                }                                
            }
        }        
        determineIfCorrect(PrimeNumbers);
    }/*End of Method printPrimeNumberMap */

    
    
    
    /** Method determineIfCorrect()
     *      Purpose: Determines if the Arraylist provided matched a file with
     *               the first 1000 prime numbers or 10 000 prime numbers.
     */
    public static void determineIfCorrect(ArrayList<Integer> CheckPrimeNumbers) throws FileNotFoundException, IOException
    {   
        int endpoint = 0;
        synchronized(ENDPOINT_LOCK){
            endpoint = EndPoint;
        }
        String path = "";
        if( endpoint == 1000)
        {
            System.out.println(" Compaing prime numbers data to correct first 1000 prime numbers.");
            path = "./src//allprimenumbers//AllPrimenumbersto1000.txt" ;                  
        }
        else if(endpoint == 10000)
        {
            System.out.println(" Compaing prime numbers data to correct first 10 000 prime numbers.");
            path = "./src//allprimenumbers//AllPrimenumbersto10000.txt" ;                
        }
       
        ArrayList<Integer> CorrectPrimeNumbers = new ArrayList<Integer>();
        File file = new File(path);        
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        int Countoflines = 0;        
        String x = new String("");          
        x = reader.readLine();
        do
        {                                   
            int k = Integer.parseInt(x);
            CorrectPrimeNumbers.add(k);
            Countoflines++; 
            x = reader.readLine();
        }while ( x != null) ;        
        reader.close();         
       
        /*Sort CorrectPrimeNumbers smallest to greatest*/
        Collections.sort(CorrectPrimeNumbers);
        /*Sort CheckPrimeNumbers smallest to greatest*/
        Collections.sort(CheckPrimeNumbers);
        
        int Asize = CorrectPrimeNumbers.size();
        int Bsize = CheckPrimeNumbers.size();
        boolean DataIsCorrect = true;
        
        if( Asize == Bsize )
        {            
            System.out.println(" Data has equal size.");
            int i = 0;
            for(int item : CheckPrimeNumbers)
            {                                  
                /*If item is not in prime numbers */
                if( !CorrectPrimeNumbers.contains(item))
                {
                    System.err.println(" Item from new list: "+item+" Is not in the correct prime number list.");
                    DataIsCorrect = false;
                    break;
                }
                i++;
            }
            
            for(int item : CorrectPrimeNumbers)
            {                                  
                /*If item is not in prime numbers */
                if( !CheckPrimeNumbers.contains(item))
                {
                    System.err.println(" Item from correct prime numbers: "+item+" Is not in the new prime number list.");
                    DataIsCorrect = false;
                    break;
                }
                i++;
            }            
        }
        else
        {
            System.err.println(" Data does not have equal size.");
            DataIsCorrect = false;
        }

        /*Produce verdict*/
        if(DataIsCorrect == true)
        {
            if( endpoint == 1000)
            {
                System.out.println(" Prime numbers produced by program match the correct first 1000 prime numbers.");            
            }
            else if(endpoint == 10000)
            {
                System.out.println(" Prime numbers produced by program match the correct first 10000 prime numbers.");
            }
        }
        else
        {
            if( endpoint == 1000)
            {
                System.err.println(" Prime numbers produced by program DO NOT MATCH the correct first 1000 prime numbers.");            
            }
            else if(endpoint == 10000)
            {
                System.err.println(" Prime numbers produced by program DO NOT MATCH the correct first 10000 prime numbers.");
            }        
        }        
    }/*End of Method determineIfCorrect */

    
    
    
    /** Method let pass
     *  Purpose: Controls wait in main. When all threads are done a notify 
     *           all is called and main knows to stop time.
     */
    public static void  letpass(int threadID)
    {   
        int count = 0;
        synchronized(COUNT_LOCK)
        {
            countOfDoneThreads++;
            count = countOfDoneThreads;
        }
                
        int howmanyworkers = 0; synchronized(HOWMANYWORKERS_LOCK){howmanyworkers = howmanyWorkers;}
        
        if(count == howmanyworkers  )
        {
            synchronized(GENERAL_LOCK){
                GENERAL_LOCK.notifyAll();               
            }                
        }
    }/* End of Method letpass */

    
    
    
    /** Method determineSplit
     *      Purpose: Determines the size of a single split. This would be the 
     *               size of the EndPoint divided by howmanyworkers.
     */
    public static void determineSplit()
    {   
        int splitSize = 0;
        int endpoint = 0; synchronized(ENDPOINT_LOCK){endpoint = EndPoint;}
        int howmanyworkers = 0; synchronized(HOWMANYWORKERS_LOCK){howmanyworkers = howmanyWorkers;}        
        splitSize = endpoint/howmanyworkers;                    
        synchronized(SPLITSIZES_LOCK)
        {
            SplitSizes = splitSize;
        }                
    }/*End of Method determineSplit*/

    
    
    
    /** Method main
     *      Purpose: Creates multiple threads that work together to produce a 
     *               list of prime numbers. This method times how long the process
     *               takes to complete this task. A do while loop allows the program
     *               to run twice first time with 1000 as an endpoint second time
     *               with 10000 as an end point.
     */
    public static void main(String[] args) {
        int plzDoTwice = 2;
        do{
            synchronized(ENDPOINT_LOCK)
            {
                if(plzDoTwice == 1)
                {               
                    EndPoint = 10000;
                    countOfDoneThreads = 0;
                }
                System.out.println("\n\n\n\n\n\n\n\nEndpoint: "+EndPoint);   
            }
            long startTime = System.currentTimeMillis();
            int howmanyworkers = 0; synchronized(HOWMANYWORKERS_LOCK){howmanyworkers = howmanyWorkers;}
            determineSplit();

            /* Create and run threads. */
            for (int i = 0; i < howmanyworkers; ++i){
                (threads[i] = new AllPrimeNumbersV2(i)).start();
            }

            /* Wait for all threads to finish. */
            try{
                synchronized(GENERAL_LOCK){
                    GENERAL_LOCK.wait();            
                    long endTime = System.currentTimeMillis();
                    long runTime = endTime - startTime;                
                    System.out.printf("Execution time: %, d\n", runTime);                

                }
            }catch(Exception e)
            {
                System.err.println(" There was an error...");
            }  
            try {
                printPrimeNumberMap();
            } catch (IOException ex) {
                Logger.getLogger(AllPrimeNumbersV2.class.getName()).log(Level.SEVERE, null, ex);
            }
            plzDoTwice--;
        }while(plzDoTwice != 0);        
    }/*End of Method main*/
    
    
}/* End of Class AllPrimeNumbersV2 */
