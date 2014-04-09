#FOR FALSE NEGATIVES
#For every file output with errors (i.e. 100 for each case), look at the base case for all elements identified. 
#Now for each error file, add the number of times the element was identified in the error files, and keep incrementing the counts for the elements in base files, 
#also identified in error file. Also keep track of elements identified in error file but not present in base file.

my $averageErrorPerElement=0, $averageError=0;

my $maxCount=1;
my $maxPosition=288;

#This is to take care of cases where a position has 0 elements in visual cone, in the ground truth
my $effectiveMaxPosition=$maxPosition; 

for($position=0;$position<$maxPosition;$position++){
	#open FileBaseCase, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/runNoErrorSurfaceCloud62/position$position/0";
	open FileBaseCase, "<", "/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run25/position$position/0";
	my %originalEleIdent=();
	my %errorEleIdent=();
	my $flag=0;
	
	while(my $line=<FileBaseCase>){
		my @tempArr=split(/\s+/,$line);	
		$originalEleIdent{$tempArr[0]}=0;
	}	

	my $trueError=0;
	my $originalSize=keys(%originalEleIdent);
	
	#Continue loop if there are no elements in the ground truth file
	if($originalSize==0)
	{
		$effectiveMaxPosition--;
		next;
	}

	for(my $count=0;$count<$maxCount;$count++)
	{
		open FileWithError, "<", "/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/field_data/ground_truth/run8/position$position/$count"; #the file with results from java
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
		}	
		close FileWithError;
	}

	my %falseNegatives=();

#	open FileWithOutput, ">", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/view_angle/positionWiseElementIdent_13_vw50_0.0375/$position";
	
	my $countOriginalElement=0;
	my $errorProb1=0;
	#$errorProb=0;	
	my $max=0;
	while(($key, $value)=each(%originalEleIdent)){
		$countOriginalElement=$countOriginalElement+1;
		print "key is $key and value is $value\n";
		if($value!=0){
#			print FileWithOutput "$key->$value\n";	
			$errorProb1=$errorProb1+($value);
			if($value!=$maxCount){
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
	
	print "Count = $countOriginalElement $errorProb1\n";
	
	my $errorProb=$errorProb1/($countOriginalElement*$maxCount);

	print "error prob = $errorProb\n";
#	print FileWithOutput "False Positives: \n";
=pod		
	while(($key, $value)=each(%errorEleIdent)){
		print FileWithOutput "$key->$value\n";	
		$flag=1;
	}
	
	print FileWithOutput "False Negatives: \n";
		
	while(($key, $value)=each(%falseNegatives)){
		print FileWithOutput "false negative : $key->$value\n";	
		$flag=1;
	}
=cut
	if($flag==1){	
		print "$position\n";
	}
	
	$averageErrorPerElement=$averageErrorPerElement+($errorProb/$effectiveMaxPosition);
	$averageError=$averageError+($trueError/($maxCount*$effectiveMaxPosition));
	print "avg error per element= $averageErrorPerElement\n";
			
#	close FileWithOutput;	
	close FileBaseCase;	
}

open FILEWRITE, ">/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/field_data_results/prob_RTKGPS_MDOT";
#Percentage of positions where list of identified elements is wrong
print FILEWRITE "Average error is = $averageError\n";

#Percentage of error positions where you would miss an element of interest (on average)
$averageErrorPerElement=1-$averageErrorPerElement;
print FILEWRITE "Average error per element = $averageErrorPerElement\n";

