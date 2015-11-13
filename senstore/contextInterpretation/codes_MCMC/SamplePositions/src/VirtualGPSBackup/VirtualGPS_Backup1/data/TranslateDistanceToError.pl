#!/usr/bin/perl
use warnings;
use Cwd;

my $place = $ARGV[0];
 
open FExpectedDist, "<", cwd()."/VincentyDistances/$place"."RandomGPS_5And100Miles";
open FActualDist, "<", cwd()."/$place"."ErrorOutput";

my %ActualPointDistances = ();
my $ActualPointCount = 0;
while(<FActualDist>)
{
	if($_ =~ m/6 is the line length/)
	{
		$line = <FActualDist>;
		chomp($line);
		$ActualPointDistances{$ActualPointCount} = $line;	
		$ActualPointCount++;
	}
	elsif($_ =~ m/5 is the line length/)
	{
		$ActualPointDistances{$ActualPointCount} = "null";
		$ActualPointCount++;
	}
}

my $ExpectedDistPoints = 0;
my %ExpectedPointDistances = ();
while(<FExpectedDist>)
{
	if($_ =~ m/Distance/)
	{
		my @splitLine = split("\\s+", $_);
		$ExpectedPointDistances{$ExpectedDistPoints} = $splitLine[1];
		$ExpectedDistPoints++;
	}
}

#$expectedCount = keys %PointDistances;
#$actualCount = keys %ExpectedPointDistances;
#while(my($key,$value) = each %PointDistances)
foreach my $key (sort {$ExpectedPointDistances{$a} <=> $ExpectedPointDistances{$b}} keys %ExpectedPointDistances)
{
	if($ActualPointDistances{$key} ne "null")
	{
		my $error = $ActualPointDistances{$key} - $ExpectedPointDistances{$key};
		print "$ExpectedPointDistances{$key},$error\n";
	}
}

close FActualDist;
close FExpectedDist;
