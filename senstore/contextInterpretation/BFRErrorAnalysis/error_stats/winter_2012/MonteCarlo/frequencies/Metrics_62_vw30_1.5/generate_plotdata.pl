#!/usr/bin/perl

open FILE, "</opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/MonteCarlo/frequencies/Metrics_62_vw30_1.5/result_output_FN";
open LISTFILE, ">/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/MonteCarlo/frequencies/Metrics_62_vw30_1.5/list_error_FN";
open ELEMENTFILE, ">/opt/senstore/contextInterpretation/BFRErrorAnalysis/error_stats/winter_2012/MonteCarlo/frequencies/Metrics_62_vw30_1.5/element_error_FN";


while($line = <FILE>)
{
	my @splitLine = split(/\s+/,$line);	
	if($splitLine[0] =~ m/^Freq/){
		print LISTFILE "$splitLine[1]\t";
		print ELEMENTFILE "$splitLine[1]\t";
		next;
	}	
	
	if($splitLine[2] eq "=")
	{
		print LISTFILE "$splitLine[3]\n";
		next;
	}
	if($splitLine[4] eq "=")
	{
		print ELEMENTFILE "$splitLine[5]\n";
		next;
	}
}
close ELEMENTFILE;
close LISTFILE;
close FILE;
