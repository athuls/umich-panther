#!/usr/bin/perl

open FI, "</home/athuls89/Desktop/Courses/CS598/VirtualGPS/umich-panther/senstore/contextInterpretation/codes_MCMC/SamplePositions/src/VirtualGPS/data/BeijingWithin20Miles";
open FO, ">/home/athuls89/Desktop/Courses/CS598/VirtualGPS/umich-panther/senstore/contextInterpretation/codes_MCMC/SamplePositions/src/VirtualGPS/data/BeijingLatLongOnly_20Miles";

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
