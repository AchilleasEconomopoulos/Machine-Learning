

import java.util.ArrayList;
import java.io.*;
import java.util.StringTokenizer;

public class NaiveBayes {
    BufferedReader br;
    StringTokenizer st;
    
    final ArrayList<String> attSet;
    ArrayList<Boolean> checked;
    
    ArrayList<Integer> existsLegit;
    ArrayList<Integer> existsSpam;
    
    ArrayList<Double> pX1C1;
    ArrayList<Double> pX1C0;
    ArrayList<Double> pX0C1;
    ArrayList<Double> pX0C0;
    
    ArrayList<Integer> X;
    
    //Used for possibility calculations during education
    double legit;
    double spam;
    
    //Used for precision and recall calculations during evaluation
    double predSpam;
    double relevant;
    double falseSpam=0;
    
    //Used for accuracy calculations during evaluation
    int correct=0;								
    int incorrect=0;
    
    double pC1;
    double pC0;
    
    
    public NaiveBayes(ArrayList<String> attributes){
	existsLegit = new ArrayList<>();
	existsSpam = new ArrayList<>();
	
	pX1C1 = new ArrayList<>();
	pX1C0 = new ArrayList<>();
	pX0C1 = new ArrayList<>();
	pX0C0 = new ArrayList<>();
	
	for(int i=0; i<=attributes.size();i++){
	    existsLegit.add(1);
	    existsSpam.add(1);
	    
	    pX1C1.add(0.0);
	    pX1C0.add(0.0);
	    pX0C1.add(0.0);
	    pX0C0.add(0.0);
	}
	
	attSet = attributes;
	
	checked = new ArrayList<>();
	X = new ArrayList<Integer>();
	
	for(int i =0; i<attributes.size();i++){
	    checked.add(false);
	    X.add(0);
	}
	
	legit = 0.0;
	spam = 0.0;
	
	pC1 = 0.0;
	pC0 = 0.0;
    }
    
    public void educate(File folder, int size){
	String line;								
	String current;								
	int category;								
	int index;
	int counter = 1;
	
	for (File fileEntry : folder.listFiles()){
	    if(counter<=size){
	    if (fileEntry.isDirectory()){
		educate(fileEntry,size);
	    }
	    else{
		counter++;
		for(int i =0; i<checked.size(); i++)
		    checked.set(i,false);

		category = 1;
		if(fileEntry.getName().contains("legit")){
		    category=0;
		    legit++;
		}else
		    spam++;
	    
		try{
		    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
		    try{
			while((line=br.readLine())!=null){
			    st = new StringTokenizer(line);
			    while(st.hasMoreTokens()){
				current = st.nextToken();

				if(current.compareTo("Subject:")!=0){
				    index = attSet.indexOf(current);
				    if(index!=-1){
					if(checked.get(index)==false){
					    checked.set(index,true);
					    if(category==1)
						existsSpam.set(index,existsSpam.get(index)+1);
					    else
						existsLegit.set(index,existsLegit.get(index)+1);
					}
				    }
				}
			    }
			}
		    }catch(IOException e){
			System.err.println("Could not read file.");
		    }
		}catch(FileNotFoundException e){
		    System.err.println("File not found.");
		}
	    }
	}
	}
	calculateProbabilities();
    }
    
    private void calculateProbabilities(){
	
	for(int i=0;i<attSet.size();i++){
	    pX1C1.set(i,(existsSpam.get(i) / (spam + 2)));
	    pX1C0.set(i,(existsLegit.get(i) / (legit + 2)));
	    pX0C1.set(i,((spam + 2 - existsSpam.get(i)) / (spam + 2)));
	    pX0C0.set(i,((legit + 2 - existsLegit.get(i)) / (legit + 2)));
	    
	    
	    //DEBUGGING
	    /*
	    System.out.println(attSet.get(i));
	    System.out.println(pX1C1.get(i) + " " + pX1C0.get(i) + " " + pX0C1.get(i) + " " + pX0C0.get(i));
	    */
	}
	
	pC1 = spam / (legit + spam);
	pC0 = legit / (legit + spam);
	
	
	//DEBUGGING
	/*
	System.out.println("P(C=1) = " + pC1);
	System.out.println("P(C=0) = " + pC0);
	*/
    }
    
    
    
    public void evaluate(File folder,int size){
	String line;								
	String current;								
	int category;								
	int index;
	int t;
	int counter = 1;
	
	for (File fileEntry : folder.listFiles()){
	    if(counter<=size){
	    if (fileEntry.isDirectory()){
		evaluate(fileEntry,size);
	    }
	    else{
		counter++;
		for(int i =0; i<X.size();i++)
		    X.set(i,0);
		
		category = 1;
		if(fileEntry.getName().contains("legit")){
		    category=0;
		}else
		    relevant++;
		
		try{
		    br = new BufferedReader(new FileReader(fileEntry));
		    try{
			while((line = br.readLine()) != null){
			    st = new StringTokenizer(line);

			    while(st.hasMoreTokens()){
				current = st.nextToken();
				if(current.compareTo("Subject:")!=0){
				    index = attSet.indexOf(current);
				    if(index!=-1){
					X.set(index,1);
				    }
				}
			    }
			}
		    }
		    catch (IOException e){
			    System.err.println("Could not read file.");
		    }
		}
		catch(FileNotFoundException e){
		    System.err.println("File not found.");
		}
		
		t = NBayesClassification(X);
		
		if (t == category) correct++;
		else {
		    incorrect++;
		    if(category == 0)
			falseSpam++;
		}
	    }
	}
	}
    }
    
    private int NBayesClassification(ArrayList<Integer> X){
	double pC1X=pC1;
	double pC0X=pC0;
	
	for(int i =0; i<X.size();i++){
	   if(X.get(i) == 1){
	       pC1X *= pX1C1.get(i);
	       pC0X *= pX1C0.get(i);
	   }else{
	       pC1X *= pX0C1.get(i);
	       pC0X *= pX0C0.get(i);
	   }
	}
	
	if(pC1X > pC0X){
	    predSpam++;
	    return 1;
	}
	else{
	    return 0;
	}
    }
    
    public void resetStats(){
	incorrect=0;
	correct=0;
	predSpam = 0.0;
	relevant = 0.0;
	falseSpam = 0.0;
    }
    
    public void resetClass(){
	pC1 = 0.0;
	pC0 = 0.0;
	legit = 0.0;
	spam = 0.0;
	
	for(int i=0; i<attSet.size();i++){
	    existsLegit.set(i,0);
	    existsSpam.set(i,0);
	    pX1C1.set(i,0.0);
	    pX1C0.set(i,0.0);
	    pX0C1.set(i,0.0);
	    pX0C0.set(i,0.0);
	}
    }
    
    public void printStats(String str){
	double accuracy;
	double precision;
	double recall;
	double f1;
	accuracy = ((double)correct/(correct + incorrect));
	precision = (predSpam - falseSpam) / predSpam;
	recall = (predSpam - falseSpam) / relevant;
	f1 = 2*(precision*recall)/(precision+recall);
	
	System.out.println(str + " Accuracy: " + accuracy);
	System.out.println(str + " Precision: " + precision);
	System.out.println(str + " Recall : " + recall);
	System.out.println(str + " F1: " + f1);
	System.out.println("CORRECT: " + correct + " ... INCORRECT: " + incorrect);
	System.out.println("Detected spam: " + predSpam + " | Actual spam: " + relevant + " | Falsely classified as spam: " + falseSpam);
    }
}
