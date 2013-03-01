#!/usr/bin/perl -w

use strict;

local $/ = "\n[[syntax trees at end of";

my $previous;
while (<>) {
  next unless /^\s*(\w+)]](.*?)$(.*)/sm;
  my ($phase, $status, $text) = ($1, $2, $3);
  print "*** $phase ***\n";
  if (!defined($previous)) {
    print $text;
  } elsif ($status =~ /tree is unchanged since/) {
    $text = $previous;
  } else {
    &diff($previous, $text);
  }
  $previous = $text;
}

sub diff {
  my ($old, $new) = @_;
  local $^F = 1000;
  pipe my $oldr, my $oldw;
  pipe my $newr, my $neww;
  my $diff = fork;
  unless ($diff) {
    close $oldw;
    close $neww;
    my $old = "/dev/fd/".fileno($oldr);
    my $new = "/dev/fd/".fileno($newr);
    exec { '/usr/bin/diff' } 'diff', '-wu', $old, $new or die "cannot exec with $old $new: $!\n";
  }
  close $oldr;
  close $newr;
  unless (fork) {
    close $neww;
    print $oldw $old;
    exit;
  }
  close $oldw;
  print $neww $new;
  close $neww;
  waitpid $diff, 0;
}
