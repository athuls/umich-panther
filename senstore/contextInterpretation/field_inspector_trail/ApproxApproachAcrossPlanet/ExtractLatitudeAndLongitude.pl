#!/usr/bin/perl

open FI, "</mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/RioRandomGPS_5And100Miles";
open FO, ">/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/RioLatLogOnly";

my $latitude = 0;
my $longitude = 0;
while(<FI>)
{
	if($_ =~ m/Latitude.*/)
	{
		my @splitLine = split(/\s+/, $_);
		$latitude = $splitLine[2]; 
	}			
	if($_ =~ m/Longitude.*/)
	{
		my @splitLine = split(/\s+/, $_);
		$longitude = $splitLine[2];			
		print FO $latitude.",".$longitude."\n";
	}
}

close FI;
close FO;
