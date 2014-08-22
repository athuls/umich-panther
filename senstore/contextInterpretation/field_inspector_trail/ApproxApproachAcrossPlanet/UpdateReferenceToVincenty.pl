#!/usr/bin/perl

my $city = $ARGV[0];
open FI, "<", "$city"."RandomGPS_5And100Miles";
open FV, "<VincentyDistances//output_".$city."VincentyDistances";
open FO, ">VincentyDistances//"."$city"."RandomGPS_5And100Miles";

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
		}
		print FO $_;	
	}
}
close FI;
close FV;
close FO;
