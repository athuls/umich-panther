#!/usr/bin/perl

open FH, ">","elementVisibility_1";

for($i=1;$i<=62;$i++)
{
	if($i<=25){
		print FH "$i\t0\n";
	}
	else 
	{
		print FH "$i\t1\n";
	}
}
