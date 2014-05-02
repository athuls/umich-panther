#!/usr/bin/perl
use Math::Random;
use strict;
use warnings;

my $temp = 2 ** 3;
print $temp;
my $distInput = "Normal.csv";
open(DISTFILE, $distInput);

my @allData = (0);
my $lineCount = 0;
while(<DISTFILE>)
{
	chomp;
	$lineCount++;
	if($lineCount == 1)
	{
		next;
	}
	my @lineEntries = split(', ', $_);
	push @allData, $lineEntries[1];
	#$allData[$#allData] = $lineEntries[1];		
}

my $oldMean = 0;
my $countOldMean = 0;
for(my $i = 0; $i <= $#allData; $i++)
{
	$oldMean += $allData[$i];
	$countOldMean++;	
}
$oldMean = $oldMean / $countOldMean;


# Find distance from mean for each point in cluster and decide to drop
my $count = 0;
my @clusterMeans = ();
open FINALFILE, ">FinalFile.csv";
open OUTPUTFILE, ">SampleDistances.csv";
my $averageClusterDistance=0;
while($count <= 10000)
{
	my @randomIndexes=(0);
	my @clusterPoints = ();
	while($#randomIndexes != 4)
	{
		my $randomNumber = int(rand($#allData));
		push @randomIndexes, $randomNumber;
	}

	my $clusterMean = 0;
	for(my $i=0;$i<=$#randomIndexes;$i++)
	{
		$clusterPoints[$i] = $allData[$randomIndexes[$i]];
		$clusterMean = $clusterMean + $clusterPoints[$i]; 
	}
	$clusterMean = $clusterMean/($#randomIndexes+1);

	if($count % 4 == 0)
	{
		$averageClusterDistance = $averageClusterDistance/(($#randomIndexes + 1) * 10);	
		#print "$count and $averageClusterDistance\n";
		print OUTPUTFILE "$averageClusterDistance\n";
		$averageClusterDistance = 0;
	}

	for(my $i=0;$i<=$#randomIndexes;$i++)
	{
		my $distance = &GetDistance($clusterMean, $clusterPoints[$i]);	
		my $randomNum = rand();
		my $pointProbability = &GetProbability($distance);
		if($randomNum <= $pointProbability)
		{
			$allData[$randomIndexes[$i]] = -267;
			#splice(@allData, $randomIndexes[$i], 1);
		}
		else
		{	
			$averageClusterDistance += $distance;	
		}
	}
	
	my $countWhile = 0;
	my $limit = $#allData;
	while($countWhile <= $limit)
	{
		if($allData[$countWhile] == -267)
		{
			splice(@allData, $countWhile, 1);
			$limit--;
		}
		else
		{
			$countWhile++;
		}		
	}
	
	push @allData, $clusterMean;
	$count++;
}

my $countActual = 0;
my $newMean = 0;
foreach(@allData)
{
	$newMean += $allData[$countActual];
	print FINALFILE "$countActual,$allData[$countActual]\n";
	$countActual++;
}
$newMean = $newMean/$countActual;
print "New mean is $newMean\n";
print "Old mean is $oldMean\n";
close FINALFILE;
print "Size of final array  is $#allData\n";

sub GetProbability
{
	my $probability = 1/(2.71**(0.5*$_[0]));		
	return $probability;
}

sub GetDistance
{
	my $distance = ($_[0] - $_[1]) ** 2;	
	return $distance;
}
