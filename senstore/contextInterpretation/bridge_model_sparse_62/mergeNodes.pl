#!/usr/bin/perl -w

open FH3,">","./final_merge.txt";

#@files=read_dir('/home/athuls89/Desktop/gsra/bridge_model');
@files=</opt/senstore/contextInterpretation/bridge_model/TelegraphData/*>;
#while($count<=17){
foreach $file(@files){
	print "reading file $file\n";
	open FH, "<", "$file";
	while($lineread = <FH>){
		print FH3 $lineread;
	}
#	print $file."\n";
	close FH;
}

close FH3;
