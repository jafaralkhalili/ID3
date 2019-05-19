import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class ID3 {
		static String res ="";
		public static void main(String[] args) throws IOException
		{
			  Scanner input = new Scanner(System.in);
			  String fileName;
			  int target;
			  //Prompt the user to input file name and index of the target column
			  System.out.print("Please enter file name: ");
			  fileName = input.nextLine();
			  System.out.print("Please enter the index of targer column(0 to n-1): ");
			  target = input.nextInt();
			  //Create file that will store the final output
		      File file = new File(fileName);
		      File output = new File("Rules.txt");
			  Scanner inputFile = new Scanner(file);
			  String line;
			  line = inputFile.nextLine();
			  
			  ArrayList<String> attributes = new ArrayList<String>();//store names of columns of the table
			  StringTokenizer token = new StringTokenizer(line, " ");
			  while(token.hasMoreTokens())
				  attributes.add(token.nextToken());
			  
			  //count the number of rows in the table to use to declare two-dimensional array
			  int rowCount =0;
			  while(inputFile.hasNext())
			  {
				  inputFile.nextLine();
				  rowCount++;
			  }
			  //Store the content of the file in a two-dimensional array
			  String[][] table = new String[rowCount][attributes.size()];
			  rowCount =0;
			  inputFile = new Scanner(file);
			  inputFile.nextLine();
			  while(inputFile.hasNext())
			  {
				  line = inputFile.nextLine();
				  token = new StringTokenizer(line, " ");
				  for(int i=0; i<attributes.size(); i++)
					  table[rowCount][i] = token.nextToken();
				  rowCount++;
			  }

			  ArrayList<Integer> bestAttributes = new ArrayList<Integer>();
			  ArrayList<String[][]> listTables = new ArrayList<String[][]>();
			  table = replaceEmptyValue(table, attributes, rowCount);
			  
			  mainSplit(table, attributes, rowCount, target, bestAttributes, listTables);
			  System.out.println("Rules has been written to \"Rules.txt\".\nThank you.");
			  
			  FileWriter w = new FileWriter(output);
			  BufferedWriter writer = new BufferedWriter(w);
		      writer.write(res);
		      writer.close();
			  
		}
		//calculate the entropy of each column in the table and return the index of the column with highest entropy
		public static int entropy(ArrayList<HashMap<String,Integer>> distinctList, int index, String[][] table)
		{
			ArrayList<Double> entropyValues = new ArrayList<Double>();//hold entropy of each column
			double entropyTarget=0;
			double entropy =0;
			
			//Calculate the entropy of the target column
			HashMap<String, Integer> currentMap = distinctList.get(index);
			Iterator<HashMap.Entry<String, Integer>> iterate = currentMap.entrySet().iterator();
			while(iterate.hasNext())
			{
				double currentCount = (double)iterate.next().getValue();
				entropyTarget = entropyTarget - ((currentCount/(double)table.length)*Math.log(currentCount/(double)table.length)/Math.log(2));
			}
			
			//Calculate entropy of all columns
			for(int i=0; i<distinctList.size(); i++)
			{
				entropy =0;
				//if this column is not the target column then:
				if(i != index)
				{
					currentMap = distinctList.get(i);
					int currentCount =0;
					iterate = currentMap.entrySet().iterator();
					//traverse all distinct elements in this column and use their number of occurrences to calculate entropy
					while(iterate.hasNext())
					{
						Map.Entry currentEntryValue = (Map.Entry)iterate.next();
						ArrayList<Integer> targetValuesCount = new ArrayList<Integer>();
						ArrayList<String> targetValues = new ArrayList<String>();
						//Compare each element with the corresponding value in the target column
						for(int j= 0; j<table.length; j++)
						{
							if(targetValues.contains(table[j][index]) && table[j][i].equals(currentEntryValue.getKey()))
								targetValuesCount.set(targetValues.indexOf(table[j][index]), targetValuesCount.get(targetValues.indexOf(table[j][index]))+1);
							else if((!targetValues.contains(table[j][index])) && table[j][i].equals(currentEntryValue.getKey()))
							{
								targetValues.add(table[j][index]);
								targetValuesCount.add(1);
							}
						}
						//update the entropy of the current column
						for(int k=0; k<targetValuesCount.size(); k++)
						{
							entropy -= (Double.parseDouble(currentEntryValue.getValue().toString())/table.length)*((targetValuesCount.get(k)/Double.parseDouble(currentEntryValue.getValue().toString()))*Math.log(targetValuesCount.get(k)/Double.parseDouble(currentEntryValue.getValue().toString()))/Math.log(2));
						}
					}
					entropy = entropyTarget - entropy;//find entropy of the current column
					entropyValues.add(entropy);
				}
			}
			entropyValues.add(index,entropyTarget);//add the target entropy in its position
			
			//find the column with the highest entropy
			double maxEntropy = entropyValues.get(0);
			for(int i=0; i<entropyValues.size(); i++)
			{
				if(entropyValues.get(i) > maxEntropy && i != index)
					maxEntropy = entropyValues.get(i);
			}
			
			return entropyValues.indexOf(maxEntropy);
		}
		//Split the table into smaller tables, each table contains a distinct value in the column that has the highest entropy
		public static ArrayList split(ArrayList<HashMap<String,Integer>> distinctList, int index, String[][] table, ArrayList<String> attributes,ArrayList<Integer> bestAttributes, String[][] mainTable) throws IOException
		{

			int bestAttribute = entropy(distinctList, index, table);
			HashMap<String, Integer> currentMap = distinctList.get(bestAttribute);
			ArrayList<String[][]> newTables = new ArrayList<String[][]>();
			boolean returnNull = true;
			
			//Check if all columns in the table has only one distinct value
			//loop through HashMaps of distinct elements of each column except the target column
			for(int i=0; i<distinctList.size(); i++)
			{
				if(i!= index)
				{
					currentMap = distinctList.get(i);
					Iterator<HashMap.Entry<String, Integer>> iterateFirst = currentMap.entrySet().iterator();
					int count =0;
					while(iterateFirst.hasNext())
					{
						Map.Entry currentEntryValue = (Map.Entry)iterateFirst.next();
						count++;
					}
					//if at least of column has more than one distinct element then do not return null
					if(count>1)
						returnNull = false;
				}
			}
			//Return null if all columns in the table has only one distinct value
			if(returnNull == true)
				return null;
			
			//Create new tables by dividing the column with the highest entropy into smaller tables of distinct elemnents
			currentMap = distinctList.get(bestAttribute);
			int currentCount =0;
			Iterator<HashMap.Entry<String, Integer>> iterate = currentMap.entrySet().iterator();
			while(iterate.hasNext())
			{
				int count =0;
				Map.Entry currentEntryValue = (Map.Entry)iterate.next();
				int rowsCount = (int) (currentEntryValue.getValue());
				String[][] splitTable = new String[rowsCount][distinctList.size()];
				for(int j= 0; j<table.length; j++)
				{
					if(table[j][bestAttribute].equals(currentEntryValue.getKey()))
					{
						splitTable[count] = table[j];
						count++;
					}
				}
				newTables.add(splitTable);//add the generated table an ArrayList of new tables
			}
			
			for(int i=0; i<newTables.size(); i++)
			{
				//add distinct value of the target column to list
				ArrayList<String> targetValue = new ArrayList<String>();
				String[][] currentTable = newTables.get(i);
				for(int j=0; j<currentTable.length; j++)
				{
					for(int k=0; k<currentTable[0].length; k++)
					{
						if(!targetValue.contains(currentTable[j][index]))
							targetValue.add(currentTable[j][index]);
					}
				}
				//if the target column has only one distinct value then we can generate the rule
				if(targetValue.size() == 1)
				{
					//loop through list of previous columns that has been chosen and write rules
					for(int k=0; k<bestAttributes.size(); k++)
					{
						if(k == bestAttributes.size()-1)
						{
							res+= "if " + attributes.get(bestAttributes.get(k)) + " is " +currentTable[0][bestAttributes.get(k)] + " then " + attributes.get(index) + " is " + currentTable[0][index]+ System.getProperty("line.separator");
						}
						else
						{
							res+= "if " + attributes.get(bestAttributes.get(k)) + " is " +currentTable[0][bestAttributes.get(k)] + " AND "+ System.getProperty("line.separator")+"\t";
						}
					}
					
				}

			}
			return newTables;
		}
		//Take table, attributes and product an arraylist of HashMaps where each hashMap correspond to a column
		//The key of the hashMap is distinct elements and the value of HashMap is the occurrences of this element
		public static ArrayList getDistinctElementsCount(String[][] table, ArrayList<String> attributes, int rowCount)
		{
			  ArrayList<HashMap<String,Integer>> distinctList = new ArrayList<HashMap<String,Integer>>();
			  HashMap<String, Integer> distinctEntries = new HashMap<String,Integer>();
			  //loop through each column in the table
			  for(int i=0; i<attributes.size(); i++)
			  {
				  //loop through each row in the table
				  distinctEntries = new HashMap<String,Integer>();
				  for(int j=0; j<rowCount; j++)
				  {
					  //add distinct items to the hashMap and update their count
					  if(distinctEntries.containsKey(table[j][i]))
						  distinctEntries.put(table[j][i], distinctEntries.get(table[j][i]) +1);
					  else
						  distinctEntries.put(table[j][i],1);
				  }
				  distinctList.add(distinctEntries);
			  }
			  return distinctList;
		}
		
		public static void mainSplit(String[][] table, ArrayList<String> attributes, int rowCount, int index, ArrayList<Integer> bestAttributes,ArrayList<String[][]> listTables) throws IOException
		{
			listTables.add(table);
			//Use getDistinctElementsCount method to generate a list of distinct elements in each column in this table
			ArrayList<HashMap<String, Integer>> distinctList = getDistinctElementsCount(table,attributes,table.length);
			//Use entropy method to get the index of the column with the highest entropy
			int bestAttribute = entropy(distinctList,  index, table);
			//add the columns used to a list, this list is used to back trace later (In case we run out attributes)
			if(!bestAttributes.contains(bestAttribute))
				bestAttributes.add(bestAttribute);
			
			//Use split method to generate new tables that resulted from splitting the table 
		    ArrayList<String[][]> newTables = split(distinctList,index,table,attributes, bestAttributes, table);
		    
		    //If the getMajority method returned null this means that there is no more attributes that can be used for splitting
		    if(newTables == null)
		    {    	
		    	String result="";
		    	//Get the parent table 
		    	String[][] currentTable = listTables.get(listTables.size()-1);
	    		listTables.remove(listTables.size()-1);
	    		result =  getMajority(currentTable, attributes, index);
	    		//if split still return null then repeat the process with the parent table
		    	while(result == null && listTables.size()>0)
		    	{
		    		currentTable = listTables.get(listTables.size()-1);
		    		listTables.remove(listTables.size()-1);
		    		result =  getMajority(currentTable, attributes, index);
		    	}
		    	//Write the Rule to the output file
				for(int k=0; k<bestAttributes.size()-1 && result!=null; k++)
				{
					for(int o=0; o<=k && k!=0; o++)
						res += "\t";
					if(k == bestAttributes.size()-2)
						res += "if " + attributes.get(k) + " is " +currentTable[0][k];
					else
						res += "if " + attributes.get(k) + " is " +currentTable[0][k] + " AND " +System.getProperty("line.separator");
				}
				if(result != null)
				{
					res += " then " + attributes.get(index) + " is " + result + System.getProperty("line.separator");
				}
		    	return;
		    }
		    
		    //loop through the tables that were generated by split method
			for(int i=0; i<newTables.size(); i++)
			{
				ArrayList<Integer> newBestAttributes = new ArrayList<Integer>();
				for(int y=0; y<bestAttributes.size(); y++)
				{
					if(!newBestAttributes.contains(bestAttributes.get(y)))
						newBestAttributes.add(bestAttributes.get(y));
				}
				//find distinct elements in the target column
				ArrayList<String> targetElements = new ArrayList<String>();
				String[][] currentTable = newTables.get(i);
				for(int j=0; j<currentTable.length; j++)
				{
					if(!targetElements.contains(currentTable[j][index]))
							targetElements.add(currentTable[j][index]);
				}
				//if there is more than one distinct value in the target column then we repeat the process
				if(targetElements.size()>1)
				{
					distinctList = getDistinctElementsCount(currentTable,attributes,currentTable.length);
					mainSplit(currentTable,attributes,rowCount,index,newBestAttributes, listTables);
					
				}
			}
		}
		//getMajority is a helper method that is used to backtrace when we run out of attributes
		public static String getMajority(String[][] table, ArrayList<String> attributes, int index) throws IOException
		{

			ArrayList<HashMap<String, Integer>> distinctList = getDistinctElementsCount(table,attributes,table.length);
			HashMap<String, Integer> currentMap = distinctList.get(index);
			Iterator<HashMap.Entry<String, Integer>> iterateFirst = currentMap.entrySet().iterator();
			Map.Entry currentEntryValue = (Map.Entry)iterateFirst.next();
			
			//Store the count and value of the target element with the highest occurrences
			int maxTargetCount = Integer.parseInt(currentEntryValue.getValue().toString());
			String maxTargetValue = String.valueOf(currentEntryValue.getKey().toString());
			
			boolean found = false;
			//traverse the hashMap to find the element with majority occurrences
			while(iterateFirst.hasNext())
			{
				currentEntryValue = (Map.Entry)iterateFirst.next();
				//if the highest number of occurrences exist in two elements then return false (answer not found)
				if(maxTargetCount == Integer.parseInt(currentEntryValue.getValue().toString()))
					found = false;
				//Update the variables that store the element of the highest occurrences
				else if(maxTargetCount < Integer.parseInt(currentEntryValue.getValue().toString()))
				{
					found = true;	
					maxTargetCount = Integer.parseInt(currentEntryValue.getValue().toString());
					maxTargetValue = String.valueOf(currentEntryValue.getKey().toString());
				}
			}
			//return the value found or return null
			if(found == true)
				return maxTargetValue;
			else
				return null;
		}
		//This method is to replace the null value by the value of the element that has the highest number of occurrences
		public static String[][] replaceEmptyValue(String[][] table, ArrayList<String> attributes, int rowCount)
		{
			ArrayList<HashMap<String, Integer>> distinctList = getDistinctElementsCount(table,attributes,table.length);
			for(int i=0; i< table.length; i++)
			{
				for(int j=0; j<attributes.size(); j++)
				{
					//if a null value is encountered then:
					if(table[i][j].equals("null") || table[i][j].equals(""))
					{
						HashMap<String, Integer> currentMap = distinctList.get(j);
						Iterator<HashMap.Entry<String, Integer>> iterateFirst = currentMap.entrySet().iterator();
						Map.Entry currentEntryValue = (Map.Entry)iterateFirst.next();
						
						//Store the count and value of the element with the highest occurrences
						int maxCount = Integer.parseInt(currentEntryValue.getValue().toString());
						String maxValue = String.valueOf(currentEntryValue.getKey().toString());
						
						//traverse the hashMap to find the element with majority occurrences
						while(iterateFirst.hasNext())
						{
							currentEntryValue = (Map.Entry)iterateFirst.next();
							//update the value of the highest element
							if(maxCount < Integer.parseInt(currentEntryValue.getValue().toString()))
							{
								maxCount = Integer.parseInt(currentEntryValue.getValue().toString());
								maxValue = String.valueOf(currentEntryValue.getKey().toString());
							}
						}
						//Assign the value that has highest occurrences to the location that was empty
						table[i][j] = maxValue;
					}
				}
			}
			return table;
		}
}
