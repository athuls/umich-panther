#!/usr/bin/perl
use Cwd;

my $place=$ARGV[0];
open FVincenti, "<", cwd()."/$place"."VincentiDurations";
open FApprox, "<", cwd()."/$place"."ApproximateDurations";

open FAll, ">", cwd()."/$place"."AllDurations";

print FAll "Algorithm,Latency\n";
while(<FVincenti>)
{
	if($_=~m/^Vincenti,/)
	{
		print FAll $_;
	}
}
while(<FApprox>)
{
	if($_=~m/^ApproximateAlgorithm,/)
	{
		print FAll $_;
	}
}

close FVincenti;
close FApprox;
close FAll;
