#!/usr/bin/perl

my $windowSize = 5;
my @places = ("Athens", "Beijing","Cairo","Frankfurt","HongKong","Lisbon","London","Madrid","Melbourne","Moscow","Mumbai","NewDelhi","Paris","Rio","Rome","Shanghai","Tehran","Tokyo","Vienna","Zurich");
open FO, ">LatLongVariation";
foreach(@places)
{
my $averageWindowError = 0;
my $count = 0;	
my $place = $_;
open FI, "<$place"."Calibration";
open FIOutput, "<$place"."FinalError.csv";
while(<FI>)
{
	my @splitLine = split(",", $_);
	if($splitLine[0] eq "Latitude")
	{
		my $latLong = <FI>;
		chomp $latLong;
		print FO $latLong.",";
		last;
	}
}
close FI;
while(<FIOutput>)
{

	my @splitLine = split(",",$_);	
	chomp $splitLine[1];
	$averageWindowError += abs($splitLine[1]);
	$count++;
	if($count == $windowSize)
	{
		$averageWindowError = $averageWindowError / $windowSize;
		if($averageWindowError > 0.1)
		{
			print FO $splitLine[0]."\n";
			last;
		}
		else
		{
			$count = 0;
			$averageWindowError = 0;
		}
	}
}
close FIOutput;
}
close FO;
