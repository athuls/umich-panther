#!/usr/bin/perl
use warnings;

my $place = $ARGV[0];
open FI, "</mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/MadridRandomGPS_5And100Miles";
open FComplete, "</mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/MadridCompleteCoordinates";
open FIO, ">/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/MadridLocLinearInput";

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
				chop $altitude;
				break;
			}	
		}

		print FIO $longitude."\t".$latitude."\t".$altitude."\t0\t0\t".$bearingAngle."\n";
	}
}

close FI;
close FIO;
close FComplete;
