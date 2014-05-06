#!/usr/bin/perl

open FI, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/AmsterdamRandomGPS_100miles";
open FO, ">/mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/AmsterdamLatLogOnly";

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