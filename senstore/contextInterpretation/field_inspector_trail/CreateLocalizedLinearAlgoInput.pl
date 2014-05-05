#!/usr/bin/perl

open FH, "</mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/GPSSampledInput";
open FInput, ">/mnt/sde/oldsystem/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/LocalizedLinearInput";

while(<FH>)
{
	my @attribute = split("\s+", $_);
	if($attributes[0] == "Longitude:")
	{
		$attributes[2]=-$attributes[2];
		print FInput $attributes[2]."\t";	
	}					
	if($attributes[0] == "Latitude:")
	{
		print FInput $attributes[2]."\t";		
	}
	if($attributes[0] == "Altitude:")
	{
		print FInput $attributes[1]."\t";		
	}
	print FInput "0\t0\t";
	if($attributes[0] == "Distance:")
	{
		print FInput $attributes[4]."\t";		
	}
	print FInput "\n";
}
