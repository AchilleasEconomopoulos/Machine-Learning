
import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class fileAttributeAnalyzer {
    BufferedReader br;
    StringTokenizer st;
    ArrayList<String> tokens;							//All unique encoded words encountered
    ArrayList<Integer> legitTks;						//legitTks(i) contains the number of legit messages in which the encoded word tokens(i) can be found at least once
    ArrayList<Integer> spamTks;							//spamTks(i) contains the number of spam messages in which the encoded word tokens(i) can be found at least once
    ArrayList<Boolean> checked;							//Array that makes sure every encoded word found in a message is marked only one time
    ArrayList<Double> tkIG;							//tkIG(i) contains the Information Gain of the encoded word tokens(i)
    int legit;									//Total number of legit messages
    int spam;									//Total number of spam messages
    int totalFiles;
    
    static final double LRminIG = 0.03;
    static final double NBminIG = 0.045;
    
    public fileAttributeAnalyzer(){
	legit = 0;
	spam = 0;
	tokens = new ArrayList<>();
	legitTks = new ArrayList<>();
	spamTks = new ArrayList<>();
	checked = new ArrayList<>();
	tkIG = new ArrayList<>();
    }
    
    public ArrayList<String> getBestAttributes(File folder){
	ArrayList<String> bestTokens = new ArrayList<>();
	
	findAttributes(folder);
	calculateIG();
	
	int counter=0;
	
	System.out.println("Filtering the best attributes based on Information Gain..");
	
	for(int i =0; i<tkIG.size();i++){
	    if(tkIG.get(i)>NBminIG){		
		counter++;
		
		//DEBUGGING
		/*
		System.out.print("Token: " + tokens.get(i));
		System.out.println(" | Appearences: " + (legitTks.get(i) + spamTks.get(i)));
		System.out.println(tkIG.get(i));
		*/

		bestTokens.add(tokens.get(i));
	   }
	}
	System.out.println("Number of attributes: " + counter);
	
	return bestTokens;
    }
    
    private void findAttributes(File folder){
	String line;								//BufferReader line
	String current;								//current token
	int category;								//legit or spam message
	int index;								//Used to check if a word is already added to tokens and to update the correct legitTks,spamTks index
	
	for (File fileEntry : folder.listFiles()){
	    if (fileEntry.isDirectory()){
		System.out.println("Detecting possible attributes in folder: " + fileEntry.getName() + "..");
		findAttributes(fileEntry);
	    }
	    else{
		totalFiles++;
		
		for(int i =0; i<checked.size(); i++)
		    checked.set(i,false);

		category = 1;
		if(fileEntry.getName().contains("legit")){
		    category=0;
		    legit++;
		}else
		    spam++;

		try{
		    br = new BufferedReader(new FileReader(fileEntry));
		    try{
			while((line = br.readLine()) != null){
			    st = new StringTokenizer(line);

			    while(st.hasMoreTokens()){
				current = st.nextToken();
				if(current.compareTo("Subject:")!=0){
				    index = tokens.indexOf(current);
				    if(index==-1){
					tokens.add(current);
					legitTks.add(0);
					spamTks.add(0);
					checked.add(false);
					index = tokens.size()-1;
				    }

				    if(checked.get(index) == false){
					if(category == 0)
					    legitTks.set(index, legitTks.get(index)+1);
					else
					    spamTks.set(index, spamTks.get(index)+1);

					checked.set(index,true);

				    }
				}
			    }
			}

		    }
		    catch (IOException e){
			System.err.println("Could not read file.");
		    }

		}
		catch (FileNotFoundException e){
		    System.err.println("File not found.");
		}
	    }
	}
    }
    
    private void calculateIG(){
	double pX1;								
	double pX0;								
	double totalX;								//Total messages that contain X
	double totalC;								//Total messages scanned
	double H1;								//H(C|X=1)
	double H0;								//H(C|X=0)
	
	double pC1X0 = 0;							//P(C=1|X=0) : possibility for a message to be C=1 when it doesn't contain X
	double pC0X0 = 0;							//P(C=0|X=0) : possibility for a message to be C=0 when it doesn't contain X
	double pC1X1 = 0;							//P(C=1|X=1) : possibility for a message to be C=1 when it contains X
	double pC0X1 = 0;							//P(C=0|X=1) : possibility for a message to be C=0 when it contains X
	
	
	//logarithms of the above possibilities
	double logP10 = 0;
	double logP00 = 0;
	double logP11 = 0;
	double logP01 = 0;
	
	double result;
	
	double entropy = calculateEntropy();
	
	for(int i =0; i<tokens.size();i++){
	    totalX = legitTks.get(i) + spamTks.get(i);
	    totalC = legit + spam;
	    pX0 = (totalC - totalX) / totalC;					//Messages that do not contain the attribute X / total messages
	    pX1 = totalX / totalC;						//Messages that contain the attribute X / total messages
	    
	    
	    if((totalC - totalX)!=0){						//If X was found in all messages totalC - totalX = 0 and the result becomes NaN
		pC1X0 = (spam - spamTks.get(i)) / (totalC - totalX);		//spam - spamTks.get(i) = spam messages that do not contain the current X
		pC0X0 = (legit - legitTks.get(i)) / (totalC - totalX);		//legit - legitTks.get(i) = legit messages that do not contain the current X
	    }
	    pC1X1 = (spamTks.get(i)/totalX);					
	    pC0X1 = (legitTks.get(i)/totalX);
	    
	    if(pC1X0!=0)
		logP10 = Math.log(pC1X0)/Math.log(2);
	    if(pC0X0!=0)
		logP00 = Math.log(pC0X0)/Math.log(2);
	    if(pC1X1!=0)
		logP11 = Math.log(pC1X1)/Math.log(2);
	    if(pC0X1!=0)
		logP01 = Math.log(pC0X1)/Math.log(2);
	    
	    H0 = -(pC1X0 * logP10) - (pC0X0 * logP00);
	    H1 = -(pC1X1 * logP11) - (pC0X1 * logP01);
	    
	    result = entropy - (pX0 * H0) - (pX1 * H1);
	    
	    tkIG.add(result);  
	}
    }
    
    private double calculateEntropy(){
	double H;
	double pC1;								//P(C=1)
	double pC0;								//P(C=0)
	double total = legit + spam;						//Total # of messages
	
	pC1 = spam / total;
	pC0 = legit / total;
	
	H = (-1)*pC1 *(Math.log(pC1)/Math.log(2)) + (-1)*pC0 * (Math.log(pC0)/Math.log(2));
	
	return H;
    }
    
    public int getEdSetSize(){
	return totalFiles;
    }
       
    
}


//DEBUGGING
    /*
    public void listMostFrequentAttributes(int k){
	int counter = 0;
	for(int i=0; i<tokens.size();i++){
	    if(legitTks.get(i)+spamTks.get(i) > k){
		counter++;
		System.out.print("Token: " + tokens.get(i));
		System.out.print(" | Legit: " + legitTks.get(i));
		System.out.print(" | Spam: " + spamTks.get(i));
		System.out.println();
	    }
	    
	}
	System.out.println(counter);
    }*/

//GET ALL ATTRIBUTES THAT APPEAR IN AT LEAST k FILES
/*
private ArrayList<String> getMostFrequentAttributes(int k){
	ArrayList<String> fTokens = new ArrayList<>();
	
	for(int i=0; i<tokens.size();i++){
	    if(legitTks.get(i) + spamTks.get(i) > k)
		fTokens.add(tokens.get(i));
	}
	
	return fTokens;
    }
*/
