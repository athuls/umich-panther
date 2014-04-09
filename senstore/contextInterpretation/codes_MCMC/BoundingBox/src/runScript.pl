#!/usr/bin/perl

#NOTE : before this compile by  - javac -cp .:../../Jama-1.0.2.jar ./directBFRErrors/BFRErrorAnalysis.java 


open(STDOUT, ">>output.txt");
open(STDERR, ">>error.txt");

system("java -cp .:../../Jama-1.0.2.jar directBFRErrors.BFRErrorAnalysis");
