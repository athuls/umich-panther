#!/usr/bin/perl

open FI, "<AthensRandomGPS_5And100Miles";
open FV, "<VincentyDistances//AthensVincenty";

while(<FI>)
{
	my @splitLine = split(/\s+/, $_);
	if($#splitLine >= 0)
	{
		if($splitLine[0] =~ m/Distance/)
		{
			my $vincenty = <FV>;
			chomp $vincenty;
			$_ =~ s/^Distance(.*)\s+mi/Distance: $vincenty mi/g;	
			print $_, "VincentyDistances/Athens Vincenty "	
		}
	}
}
