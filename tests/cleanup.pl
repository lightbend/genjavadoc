#!/usr/bin/perl -w

use strict;

system { '/bin/rm' } 'rm', '-r', 'target/java/scala';

open my $inputs, "find target/java -type f|" or die "cannot run find: $!\n";

while (<$inputs>) {
  chomp;
  print "cleaning up $_\n";
  open my $f, "<", $_ or die "cannot read $_: $!\n";
  local $/;
  my $text = <$f>;
  close $f;
  $text =~ s,^\s*//.*\n,,gm;
  open $f, ">", $_ or die "cannot write $_: $!\n";
  print $f $text;
  close $f;
}
