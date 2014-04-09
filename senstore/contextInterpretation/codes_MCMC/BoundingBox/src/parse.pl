#!/usr/bin/perl

open FH, "<", "./output.txt";
$count=0;
while($lineread=<FH>){
	@temp=split(/\s+/, $lineread);
	if($lineread =~ m/^time taken is/){
		print $lineread;
		$count++;
	}
}
print "$count\n";
