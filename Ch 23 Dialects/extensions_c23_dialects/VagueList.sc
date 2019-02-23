
VagueList : List {	at { |index| 		^super.at((index + 1.rand2).clip(0, this.lastIndex))	}}