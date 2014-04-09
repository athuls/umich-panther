#FOR FALSE NEGATIVES
#For every file output with errors (i.e. 100 for each case), look at the base case for all elements identified. 
#Now for each error file, add the number of times the element was identified in the error files, and keep incrementing the counts for the elements in base files, 
#also identified in error file. Also keep track of elements identified in error file but not present in base file.

my $maxCount=1;
my $maxPosition=288;

open FilePosition, ">", "/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/field_data_results/result_MDOT_RTKGPS";
print FilePosition "Position\tCorrect\tFP\tFN\n";

for($position=0;$position<$maxPosition;$position++){
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
	
	for(my $count=0;$count<$maxCount;$count++)
	{
		open FileWithError, "<", "/mnt/old/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/field_data/ground_truth/run8/position$position/$count"; #the file with results from java
			
		my $correct=0, $FP=0, $FN=0;

		while($line=<FileWithError>)
		{
			my @tempArr=split(/\s+/, $line);
			if(exists($originalEleIdent{$tempArr[0]})){	#if the element was in original file also
				$correct=$correct+1;
			}
			elsif(exists($errorEleIdent{$tempArr[0]})){	#if the element in error file was not in original file (false positive)
				$FP = $FP+1;
				$errorEleIdent{$tempArr[0]}=$errorEleIdent{$tempArr[0]}+1;
			}
			else{
				$FP=$FP+1;
				$errorEleIdent{$tempArr[0]}=1;
			}
		}
		$FN = $originalSize-$correct;
		print FilePosition "$position\t$correct\t$FP\t$FN\n";
		
		close FileWithError;
	}
}
close FilePosition;
