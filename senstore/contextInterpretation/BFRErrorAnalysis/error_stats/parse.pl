#!/usr/bin/perl

#Process the 'contextually relevant element' information for each position of the inspector i.e. stats like false negatives and false positives, and 
#further check if there are any positions with <100% correctly identified elements. 

@files=</opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/positionWiseElementIdent/*>;

for $file(@files){
	open FH, "<", "$file";
	while($line=<FH>){
		@splitLine=split(/\s+/,$line);
		@checkElement=split(/\-\>/,$splitLine[0]);
		if($#checkElement>0){
			if($checkElement[1]!=100){	
				print $file;
			}
		}
		
	}
	close FH;
	print "$file\n";
}

