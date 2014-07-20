#!/usr/bin/perl
use warnings;
use Cwd;

my $place = $ARGV[0];
 
open FActualDist, "<", cwd()."/$place"."RandomGPS_5And100Miles";
open FExpectedDist, "<", cwd()."/$place"."ErrorOutput";

my %PointDistances = ();
my $pointCount = 0;
while(<FExpectedDist>)
{
	if($_ =~ m/6 is the line length/)
	{
		$line = <FExpectedDist>;
		chomp($line);
		$PointDistances{$pointCount} = $line;	
		$pointCount++;
	}
	elsif($_ =~ m/5 is the line length/)
	{
		$PointDistances{$pointCount} = "null";
		$pointCount++;
	}
}

my $ActualDistPoints = 0;
my %ActualPointDistances = ();
while(<FActualDist>)
{
	if($_ =~ m/Distance/)
	{
		my @splitLine = split("\\s+", $_);
		$ActualPointDistances{$ActualDistPoints} = $splitLine[1];
		$ActualDistPoints++;
	}
}

#$expectedCount = keys %PointDistances;
#$actualCount = keys %ActualPointDistances;
#while(my($key,$value) = each %PointDistances)
foreach my $key (sort {$ActualPointDistances{$a} <=> $ActualPointDistances{$b}} keys %ActualPointDistances)
{
	if($PointDistances{$key} ne "null")
	{
		my $error = $ActualPointDistances{$key} - $PointDistances{$key};
		print "$ActualPointDistances{$key},$error\n";
	}
}

close FActualDist;
close FExpectedDist;
