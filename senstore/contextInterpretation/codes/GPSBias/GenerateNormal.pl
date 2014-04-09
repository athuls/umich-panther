#!/usr/bin/perl

#use lib '/mnt/sde/opt/senstore/contextInterpretation/codes/GPSBias/Math';
#use Random;
use Math::Random;
use warnings;
open GPSFILE, ">Normal.csv";

for($i=0;$i<200;$i++)
{
	print GPSFILE "$i, ";
	print GPSFILE Math::Random::random_normal(1,10,2);
	print GPSFILE "\n";
}
close GPSFILE;
