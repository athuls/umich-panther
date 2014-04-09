#FOR FALSE NEGATIVES
#For every file output with errors (i.e. 100 for each case), look at the base case for all elements identified. 
#Now for each error file, add the number of times the element was identified in the error files, and keep incrementing the counts for the elements in base files, 
#also identified in error file. Also keep track of elements identified in error file but not present in base file.

my $runNumError = 48;
my $runNumTruth = 22;
my $maxCount=100;
my $maxPosition=508;

my @frequencies=();
my $pathToFrequencies = "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run$runNumError/position0/frequencies";
opendir FREQDIR, $pathToFrequencies or die "Could not open position dir\n";
my @files = readdir(FREQDIR);
closedir FREQDIR;

foreach $freq(@files)
{
	next if($freq =~ m/^\./);
	print "$freq\n";
	push(@frequencies, $freq); 
}

@frequencies = sort(@frequencies);

open FileWithOutput, ">", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/MonteCarlo/frequencies/Metrics_62_vw30_0.0375/result_output_FN";

foreach $freq(@frequencies)
{
	my $averageErrorPerElement=0, $averageError=0;
	#This is to take care of cases where a position has 0 elements in visual cone, in the ground truth
	my $effectiveMaxPosition=$maxPosition; 
	
	for($position=0;$position<$maxPosition;$position++){
	
		open FileBaseCase, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run$runNumTruth/position$position/0";
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
		


		#Iterate through all the observed positions data in frequency file	
		open FileWithError, "<", "/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run$runNumError/position$position/frequencies/$freq"; #the file with results from java
		my $correct;
		while($line = <FileWithError>)
		{
			if($line =~ m/^observedPos/){
				$correct = 0;
				next;
			}
			if($line eq "\n"){
				#An increment occurs corresponding to an error position if all the elements in original file do not occur in error file i.e. if there is a false negative
		                if($correct!=$originalSize){
                		        $trueError=$trueError+1;
		                }       
				next;
			}

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
		close FileWithError;



		my %falseNegatives=();
		my $countOriginalElement=0;
		my $errorProb1=0;
		my $max=0;
		while(($key, $value)=each(%originalEleIdent)){
			$countOriginalElement=$countOriginalElement+1;
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
		
		
		my $errorProb=$errorProb1/($countOriginalElement*$maxCount);
		
		$averageErrorPerElement=$averageErrorPerElement+($errorProb/$effectiveMaxPosition);
		$averageError=$averageError+($trueError/($maxCount*$effectiveMaxPosition));
				
		close FileBaseCase;	
	}
	
		
	print FileWithOutput "Frequency: $freq :\n";
	#Percentage of positions where list of identified elements is wrong
	print FileWithOutput "Avg. error = $averageError\n";
	
	#Percentage of error positions where you would miss an element of interest (on average)
	$averageErrorPerElement=1-$averageErrorPerElement;
	print FileWithOutput "Average error per element = $averageErrorPerElement\n\n";
}

close FileWithOutput;
