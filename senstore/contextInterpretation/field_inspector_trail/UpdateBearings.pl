#!/usr/bin/perl

open FI, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/AmsterdamRandomGPS_100miles";
open FComplete, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/AmsterdamCompleteCoordinates";
open FIO, ">/mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/AmsterdamLocLinearInput";

my $latitude = 0;
my $longitude = 0;
my $altitude = 0;
my $bearingAngle = 0;
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
	}
	if($_ =~ m/Distance.*/)
	{
		my @splitLine = split(/\s+/, $_);
		$bearingAngle = $splitLine[4];
		
		#Remove the degree symbol from end of bearing angle
		#$bearingAngle = substr($bearingAngle,0,-1); 
		$bearingAngle =~ s/$bearingAngle[$#bearingAngle]/"\n"/g;
		seek FComplete, 0, 0;
		while(my $line = <FComplete>)
		{
			my @splitLine = split(/,/, $line);					
			if($splitLine[0] eq $latitude && $splitLine[1] eq $longitude)
			{
				$altitude = $splitLine[2];	
				chomp($altitude);
				break;
			}	
		}

		print FIO $latitude."\t".$longitude."\t".$altitude."\t0\t0\t".$bearingAngle."\r\n";
	}
}

close FI;
close FO;
