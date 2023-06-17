
import java.util.*;
import java.io.*;

public class main {

    public static void main(String[] args) {
	
	
	// OBJECTS USED //
	File folder = new File("pu3/education");
	System.out.println(folder.getAbsolutePath());
	ArrayList<String> attSet;						//Best attributes based on an IG minimum limit
	ArrayList<Double> Weights;						//The result of the education part
	fileAttributeAnalyzer c = new fileAttributeAnalyzer();			//Object that contains the attribute extraction methods
	LR b;									//Object that contains logistic regression and linear classification methods
										//	such as Weight Vector calculation and Validation/Test set evaluation
	NaiveBayes r;
	int size;
	int currentSize;
	
	
	// ATTRIBUTE EXTRACTION //
	System.out.println("GETTING ATTRIBUTES");
	attSet = c.getBestAttributes(folder);
	size = c.getEdSetSize();
	System.out.println("DONE");
	//----------------------//
	
	System.out.println();
	System.out.println("--------------------------------------------");
	System.out.println();
		
	// LOGISTIC REGRESSION LOOP //
	/*
	b = new LR(0.01,attSet);						//lambda = 0.01 based on evaluations of the validation set
	for(int i=1 ; i<=10 ; i++){
	    if(i<10){
		currentSize = (size/10)*i;
	    }else{
		currentSize = (size/10)*10 + (size%10);
	    }
	    
	    b.resetStats();
	    System.out.println("EDUCATION SET SIZE: " + currentSize);
	    folder = new File("pu3/education");
	    Weights = b.educate(folder,currentSize);
	    
	    b.evaluate(folder,Weights,currentSize);
	    b.printStats("E");
	    
	    System.out.println();
	    
	    b.resetStats();
	    folder = new File("pu3/validation");
	    b.evaluate(folder,Weights,(size/8));
	    b.printStats("V");
	    
	    System.out.println();
	    
	    b.resetStats();
	    folder = new File("pu3/test");
	    b.evaluate(folder,Weights,(size/8));
	    b.printStats("T");
	    
	    System.out.println();
	    System.out.println("--------------------------------------------");
	}*/
	
	
	// NAIVE BAYES LOOP //
	r = new NaiveBayes(attSet);
	for(int i=1 ; i<=10 ; i++){
	    if(i<10){
		currentSize = (size/10)*i;
	    }else{
		currentSize = (size/10)*10 + (size%10);
	    }
	    
	    r.resetClass();
	    r.resetStats();
	    System.out.println("EDUCATION SET SIZE: " + currentSize);
	    folder = new File("pu3/education");
	    r.educate(folder,currentSize);
	    
	    r.evaluate(folder,currentSize);
	    r.printStats("E");
	    
	    System.out.println();
	    
	    r.resetStats();
	    folder = new File("pu3/validation");
	    r.evaluate(folder,(size/8));
	    r.printStats("V");
	    
	    System.out.println();
	    
	    r.resetStats();
	    folder = new File("pu3/test");
	    r.evaluate(folder,(size/8));
	    r.printStats("T");
	    
	    System.out.println();
	    System.out.println("--------------------------------------------");
	}
	
	
    }
    
}
