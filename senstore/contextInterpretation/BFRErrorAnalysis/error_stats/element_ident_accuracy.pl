#For every file output with errors (i.e. 100 for each case), look at the base case for all elements identified. 
#Now for each error file, add the number of times the element was identified in the error files, and keep incrementing the counts for the elements in base files, 
#also identified in error file. Also keep track of elements identified in error file but not present in base file.

for($position=0;$position<508;$position++){
	open FileBaseCase, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/runNoErrorSurfaceCloud62/position$position/0";
	%originalEleIdent=();
	%errorEleIdent=();
	$flag=0;
	
	while($line=<FileBaseCase>){
		@tempArr=split(/\s+/,$line);	
		$originalEleIdent{$tempArr[0]}=0;
	}	
	
	for($count=0;$count<100;$count++)
	{

		open FileWithError, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/run6/position$position/$count"; #the file with results from java

		while($line=<FileWithError>)
		{
			@tempArr=split(/\s+/, $line);
			if(exists($originalEleIdent{$tempArr[0]})){
				$originalEleIdent{$tempArr[0]}=$originalEleIdent{$tempArr[0]}+1;
			}
			elsif(exists($errorEleIdent{$tempArr[0]})){
				$errorEleIdent{$tempArr[0]}=$errorEleIdent{$tempArr[0]}+1;
			}
			else{
				$errorEleIdent{$tempArr[0]}=1;
			}
		}	
		close FileWithError;
	}

	%falseNegatives=();

	open FileWithOutput, ">", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/positionWiseElementIdent_62_3/$position";
		
	print FileWithOutput "Elements identified in the base and error files: \n";
	
	while(($key, $value)=each(%originalEleIdent)){
		if($value!=0){
			print FileWithOutput "$key->$value\n";	
			if($value!=100){
				$flag=1;
			}
		}
		else{
			$falseNegatives{$key}=$value;
		}
		
	}		
	
	print FileWithOutput "False Positives: \n";
		
	while(($key, $value)=each(%errorEleIdent)){
		print FileWithOutput "$key->$value\n";	
		$flag=1;
	}
	
	print FileWithOutput "False Negatives: \n";
		
	while(($key, $value)=each(%falseNegatives)){
		print FileWithOutput "$key->$value\n";	
		$flag=1;
	}

	if($flag==1){	
		print "$position\n";
	}	
	
	close FileWithOutput;	
	close FileBaseCase;	
}
	
