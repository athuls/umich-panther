#!/usr/bin/perl

use warnings;
#NOTE : before this compile by  - javac -cp .:../../Jama-1.0.2.jar ./directBFRErrors/BFRErrorAnalysis.java 

my $OutputFilePath = "/mnt/sdb/old/opt/umich-panther/senstore/contextInterpretation/field_inspector_trail/ApproxApproachAcrossPlanet/";
my $OutputFileName = $ARGV[0];
my $OutputFile = $OutputFilePath.$OutputFileName;
my $ErrorFile = $OutputFilePath.$OutputFileName."Error";

open(STDOUT, ">$OutputFile");
open(STDERR, ">$ErrorFile");

system("java -cp .:../../Jama-1.0.2.jar approximateApproach.ApproximateAlgorithm");
