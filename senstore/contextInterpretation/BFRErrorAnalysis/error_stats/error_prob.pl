#For every file output with errors (i.e. 100 for each case), look at the base case for all elements identified. 
#Now for each error file, add the number of times the element was identified in the error files, and keep incrementing the counts for the elements in base files, 
#also identified in error file. Also keep track of elements identified in error file but not present in base file.

my $averageErrorPerElement, $averageError;

for($position=0;$position<508;$position++){
	#open FileBaseCase, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/runNoErrorSurfaceCloud62/position$position/0";
	open FileBaseCase, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run22/position$position/0";
	my %originalEleIdent=();
	my %errorEleIdent=();
	my $flag=0;
	
	while(my $line=<FileBaseCase>){
		my @tempArr=split(/\s+/,$line);	
		$originalEleIdent{$tempArr[0]}=0;
	}	

	my $trueError=0;
	my $originalSize=keys(%originalEleIdent);
	
	for(my $count=0;$count<100;$count++)
	{
		open FileWithError, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run23/position$position/$count"; #the file with results from java
		my $correct=0;

		while($line=<FileWithError>)
		{
			my @tempArr=split(/\s+/, $line);
			if(exists($originalEleIdent{$tempArr[0]})){	#if the element was in original file also
				$originalEleIdent{$tempArr[0]}=$originalEleIdent{$tempArr[0]}+1;
				$correct=$correct+1;		
			}
			elsif(exists($errorEleIdent{$tempArr[0]})){	#if the element in error file was not in original file (false positive)
				$errorEleIdent{$tempArr[0]}=$errorEleIdent{$tempArr[0]}+1;
			}
			else{
				$errorEleIdent{$tempArr[0]}=1;
			}
		}

		#An increment occurs corresponding to an error position if all the elements in original file do not occur in error file i.e. if there is a false negative
		if($correct!=$originalSize){
			$trueError=$trueError+1;
			print "There was a false negative in error number $count and position $position\n";
		}	
		close FileWithError;
	}

	my %falseNegatives=();

	open FileWithOutput, ">", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/corrected_stats_visual_cone/positionWiseElementIdent_62_0.0375/$position";
		
	print FileWithOutput "Elements identified in the base and error files: \n";
	
	my $countOriginalElement=0;
	my $errorProb1=0;
	#$errorProb=0;	
	my $max=0;
	while(($key, $value)=each(%originalEleIdent)){
		$countOriginalElement=$countOriginalElement+1;
		#print "key is $key and value is $value\n";
		if($value!=0){
			print FileWithOutput "$key->$value\n";	
			$errorProb1=$errorProb1+($value);
			if($value!=100){
				#if((100-$value)>$max){
				#	$max=100-$value;
				#}
				#$errorProb=$errorProb+((100-$value)/100);
				$flag=1;
			}
		}
		else{
			$falseNegatives{$key}=$value;
		}
	}
	
	#print "Count = $countOriginalElement $errorProb1\n";
	my $errorProb=$errorProb1/($countOriginalElement*100);
	print FileWithOutput "False Positives: \n";
		
	while(($key, $value)=each(%errorEleIdent)){
		print FileWithOutput "$key->$value\n";	
		$flag=1;
	}
	
	print FileWithOutput "False Negatives: \n";
		
	while(($key, $value)=each(%falseNegatives)){
		print FileWithOutput "false negative : $key->$value\n";	
		$flag=1;
	}

	if($flag==1){	
		#print "$position\n";
	}

	my $temp1=$errorProb;
	my $temp2=$trueError/(100);
	print "$temp2\n";
	
	$averageErrorPerElement=$averageErrorPerElement+($errorProb/508);
	$averageError=$averageError+($trueError/(100*508));
			
	close FileWithOutput;	
	close FileBaseCase;	
}

#Percentage of positions where list of identified elements is wrong
print "Average error is = $averageError\n";

#Percentage of error positions where you would miss an element of interest (on average)
$averageErrorPerElement=1-$averageErrorPerElement;
print "Average error per element = $averageErrorPerElement\n";

