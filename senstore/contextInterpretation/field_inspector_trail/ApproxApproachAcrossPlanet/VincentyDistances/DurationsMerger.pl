#!/usr/bin/perl
use Cwd;

my $place=$ARGV[0];
open FVincenti, "<", cwd()."//PerfAnalysis_10_19_2014//$place"."VincentiDurationsNew";
open FApprox, "<", cwd()."//PerfAnalysis_10_19_2014//$place"."ApproximateDurationsNew";

open FAll, ">", cwd()."//PerfAnalysis_10_19_2014//$place"."AllDurationsNew";

print FAll "Algorithm,Latency\n";
while(<FVincenti>)
{
	if($_!~m/^Actual/)
	{
		print FAll "Vincenti,$_";
	}
}
while(<FApprox>)
{
	if($_!~m/^Duration/)
	{
		print FAll "Approximate,$_";
	}
}

close FVincenti;
close FApprox;
close FAll;
