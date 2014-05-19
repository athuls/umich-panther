#!/usr/bin/perl

open FI, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/AmsterdamRandomGPS_1mile";
open FComplete, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/AmsterdamCompleteCoordinates";
open FIO, ">/mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/AmsterdamLocLinearInput";

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
		$bearingAngle = substr($bearingAngle, 0, $#bearingAngle);
		chop $bearingAngle;
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

		print FIO $longitude."\t".$latitude."\t".$altitude."\t0\t0\t".$bearingAngle."\n";
	}
}

close FI;
close FO;
