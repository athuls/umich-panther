#!/usr/bin/perl

open FILE1, ">inspector_trail_debug";

my $count=1;

for(my $i=0;$i>=-360;$i--)
{
	print FILE1 "$count\t230\t-14\t27\t0\t-10\t$i\n";	
	$count++;
}

close FILE;
close FILE1;
