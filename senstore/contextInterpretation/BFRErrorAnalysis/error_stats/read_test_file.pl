#!/usr/bin/perl

open FILE, "</opt/senstore/contextInterpretation/BFRErrorAnalysis/error_files/winter_2012/run44/position100/frequencies/0.1";
$observedCounter = 0;
while($line = <FILE>)
{
	if($line =~ m/^observedPos/)
	{
		$counter=0;
		$observedCounter++;
		next;
	}
	if($line eq "\n")
	{
		print "done with counter value = $counter\n";
		next;
	}
	$counter++;	
	print $line;
}
print "$observedCounter\n";
