

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class LR {
    BufferedReader br;
    StringTokenizer st;
    
    final ArrayList<String> attSet;						//The chosen attribute since every function uses them and no function modifies them
    ArrayList<Double> W;							//The weight vector that the logistic regression updates affect 
    ArrayList<Integer> X;							//The feature vector for each individual sample is global so that it isn't initialized every time a new sample is examined
										//	but instead has all its values (except for x0) reset to 0
    double lambda;
    double s;
    
    //Used for precision and recall calculations during evaluation
    double predSpam;
    double relevant;
    double falseSpam=0;
    
    //Used for accuracy calculations during evaluation
    int correct=0;								//Correct and incorrect classifications are global because of the recursive nature of the evaluation method
    int incorrect=0;
    
    static final double h = 0.007;
    static final int maxEpochs = 15;
    static final double k = 0.5;
    static final double convLim = 5;
    
    //int counter =0;								//DEBUGGING
    
    
    public LR(double lambda, ArrayList<String> attributes){
	attSet = attributes;
	
	X = new ArrayList<Integer>();
	for(int i = 0; i<attributes.size(); i++){
	    X.add(0);
	}
	X.add(1);
	
	W = new ArrayList<>();
	for(int i = 0; i<attributes.size() + 1;i++){				//+1 for w0
	    W.add(0.0);
	}
	this.lambda = lambda;
	s=0;
    }
    
    // EDUCATION PART //
    public ArrayList<Double> educate (File folder, int size){
	int currEpoch = 1;
	int counter = 0;
	double lastErr;
	
	for(int i=0; i<W.size();i++){
	    W.set(i,0.0);
	}
	
	while(currEpoch<=maxEpochs && counter!=convLim){
	    //System.out.println("Applying logistic regression for epoch " + currEpoch + "..");
	    
	    lastErr = s;
	    s=0;
	    epochFunction(folder,size);
	    
	    if(Math.abs(lastErr - s) < k)
		counter++;
	    else
		counter=0;
	    
	    //System.out.println(s);
	    
	    currEpoch++;
	}
	return W;
    }
    
    private void epochFunction(File folder,int size){
	String line;								
	String current;								
	int category;								
	int index;
	int counter=1;
	
	for (File fileEntry : folder.listFiles()){
	    if(counter<=size){
		if (fileEntry.isDirectory()){
		    epochFunction(fileEntry,size);
		}
		else{
		    counter++;
		    for(int i =0; i<X.size()-1;i++)
			X.set(i,0);

		    category = 1;
		    if(fileEntry.getName().contains("legit")){
			category=0;
		    }
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
		updateWeights(category);
		}
	    }
	}
    }
	

    private void updateWeights(double y){
	    double t=0;
	    double diff=0;
	    double Wnew;
	    double f;

	    for(int i=0; i<W.size();i++){
		t += W.get(i)*X.get(i);						//One way of calculating the dot product of the W(T) and X vectors: w1*x1 + w2*x2 + ... + wn*xn + w0*x0
	    }

	    f = 1/(1 + Math.exp(-t));

	    diff = f-y;
	    s += 0.5*Math.pow(diff,2);

	    for(int i=0 ; i<W.size();i++){
		Wnew = (1-2*lambda*h)*W.get(i) + h*(y - 1/(1+Math.exp(-t)))*X.get(i);
		W.set(i,Wnew);
	    }
	}
    
    
    // USE OF ALGORITHM //
    public void evaluate(File folder, ArrayList<Double> Win,int size){
	String line;								
	String current;								
	int category = 1;								
	int index;
	double t=0;
	int counter = 1;
	
	for (File fileEntry : folder.listFiles()){
	    if(counter<=size){
	    if (fileEntry.isDirectory()){
		evaluate(fileEntry,Win,size);
	    }
	    else{
		counter++;
		t=0;
		for(int i =0; i<X.size()-1;i++)
		    X.set(i,0);

		category = 1;
		if(fileEntry.getName().contains("legit")){
		    category=0;
		}else
		    relevant++;

		try{
		    br = new BufferedReader(new FileReader(fileEntry));
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

		t = sigmoidClassification(Win);

		if (t == category) correct++;
		else {
		    incorrect++;
		    if(category == 0)
			falseSpam++;
		}
		
		
		//DEBUGGING
		/*
		System.out.print(fileEntry.getName() + " ");
		System.out.print("| Classified as: ");
		if(t==1) System.out.print("SPAM");
		else System.out.print("LEGIT");
		System.out.println();
		*/
		
		
	    }
	}
	}
    }
    
    private double sigmoidClassification(ArrayList<Double> Win){
	double t=0;
	for(int i =0; i<X.size();i++)
	     t+= Win.get(i)*X.get(i);						//t = W(T) * X = SUM(w(i) * x(i))
	
	double f = 1/(1+Math.exp(-t));

	if (f>0.5) {
	    predSpam++;
	    return 1;								//W(T)*X>0  -> C=1 (SPAM)
	}							
	else return 0;								//W(T)*X<=0 -> C=0 (LEGIT)
    }
    
    public void resetStats(){
	incorrect=0;
	correct=0;
	predSpam = 0.0;
	relevant = 0.0;
	falseSpam = 0.0;
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
