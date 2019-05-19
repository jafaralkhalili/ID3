This program is an implementation of the Decision Tree Classification in Java
   
-Overview the program code:

	The program prompts the user to enter the name of the file and the index of the target column. Then it store the content of the file using proper data structure
	and use it to generate decision tree. The classification rules will be written to a file called "Ruled.txt".
 
 
-program structure:
  
	main()—> 
		Read file and store data using proper data structure, 
		call replaceEmptyValue to replace null values with the element that have highest occurrences,
		call mainSplit method to implement decision tree, 
		write the results into output file.

	entropy()->
		calculate the entropy for each column and store them in an arrayList,
		return the index of the column that has the highest entropy.

	getDistinctElementsCount()->
		for each column, store the values and count of distinct elements into HashMap(Key is elmenet, Valus is count of element),
		return an arrayList that contains the HashMaps that were found.
		
	mainSplit()—>
		This is a recursive method, it is responsible of splitting the table by using the column of the highest entropy, if all columns has been used then it will chose the
		element with highest occurences as the result. If there are more than one element with the same number of occurrences then backtrace and repeat the procedure on
		the parent table.
		The method calls itself for each new generated table until it gets a table with only one distinct value in the target column.
		To perform this process mailSplit uses the following helper methods:
			-getDistinctElementsCount: generate an arrayList of hashMaps, where each HashMap contains distinct elements and their count of a column in the table.
			-Split: This method splits table by using the column of the highest entropy and return an arrayList that contains the new tables.
					If all columns has been used then the method return null and do not split the table.
			-getMajority: This method is called when the target column has more than one distinct elemnent and there is no more columns that can be used to split the table. 
						  It returns the element that has the highest occurrences, in the case of having more than one distinct element with same highest count then the
						  returns null.
	Split()—> 
		This method uses entropy() method to get the index of the column with the highest entropy,
		It generates an arrayList that contains new tables by creating a new table for each distinct value in the column that has the highest entropy 
		If all columns has been used for splitting then the method do not split the table and return null.
		
	getMajority()—>
		It returns the element that has the highest occurrences in the target column, 
		in the case of having more than one distinct element with same highest count then the returns null.

	replaceEmptyValue()—>
		Search the table for null values and replace them with the element that has the highest occurrences in the corresponding column,
		return a new table with no empty fields and null values.
	
  
Run the program:
   Compile: javac ID3.java.     
   Run: java ID3
                      
