// 	Transcription of Travel timeline: 

//	Timeline mostly from Stefan Zweig: Magellan, der Mann und seine Tat, 1937; 
//	SOME details from Antonio Pigafetta: Mit Magellan um die Erde, Edition Erdmann, 2001
//	(Dank an Peter Jakober!)

q.timeline = [
	[10,  8, 1519, 36.7, -6, nil, 'leave_sevilla'], 	
			//datum 10.8.1519; Breitengrad, Längengrad,(wikipedia) 
			// Richtungen: (0 westen, 1, norden, 2 osten 3 süden), information
												
										//anzahl der matrosen, 
			// Schiffe: 	Trinidad, 		130 Tonnen, 66 Mann( + Magellan)
			//			San Antonio,		130 Tonnen, 60 Mann
			//			Concepcion,		90 Tonnen,  45 Mann
			//			Victoria,			90 Tonnen,  42 Mann
			//			Santiago,			60 Tonnen,  32 Mann
			// MÄNNER: 234; 170 Spanier, 40 Portugiesen, 20 Italiener, afrikanische und asiatische Dolmetscher												
	
	[20,  8, 1519, 36.4, -6.4,  nil, 'reach_sanlucar'], // erfunden
	[20,  9, 1519, 36.4, -6.4,  nil, 'leave_sanlucar'], // open sea
			//Info: spanische Autoritäten versuchen die Reise zu verhindern
	
	[26,  9, 1519,  28.5, -16,  nil, 'reach_teneriffa'],
			// 3 Tage in Teneriffa dann Monte Rosse
	
	[ 3, 10, 1519,  28.5, -16,  nil, 'leave_teneriffa'],
			// zu mitternacht wird in see gestochen
			// keine verluste gemeldet...


			// 14. oktober heftige stürme, 14° nördlicher breite
			//stimmung der mannscht verschlechtert sich...wollen heimkehren
	[14, 10, 1519,  14, -23.5, 'storm'],
//	[15, 10, 1519,  14.4, -23.5, 'normal'],

	[16, 10, 1519,  14.9, -23.5,  \normal, 'reach_cape_verde'],
	[26, 10, 1519,  14.9, -23.5,  nil, 'leave_cape_verde'],
	
	[ 28, 11, 1519,  0, -30,  nil, 'cross equator'],		// long is estimate
			//Pigafetta schreibt dass der Equator erst im Dezember erreicht wurde...
			//erste meuterei der spanischen kapitäne, welche er durch die gefangennahme seines ersten offiziers beendet 
			//(keine Berichte über tote gefunden...)
	
	[ 6, 12, 1519,  -8.4, -35,  nil, 'cape_san_augustin'], // near recife/olinda

	[13, 12, 1519, -23, -43.4, nil, 'reach_rio_de_janeiro'],
			//leiden unter hitze.
			//beschreibt die menschenfresser
			//nehmen anscheinend einen oder meherere Brasilianer an Board auf! (Kommen später im Text vor) (+ca 2Leute)
	
	
	[26, 12, 1519, -23, -43.4,  nil, 'leave_rio_de_janeiro'],
			//Pigafetta schreibt, dass sie erst am 27 dezember abreisen
			//wird noch nichts von Verlusten erwähnt 

	
	[10,  1, 1520, -35, -55,  nil, 'reach_rio_de_la_plata'],
	[11,  1, 1520, -35, -55,  \storm, ''],
	[13,  1, 1520, -35, -55,  \normal, ''],
			//versuchten die meerenge zu finden, um, so Pigafetta, ins Südmeer zu kommen, ohne Erfolg
	
	[ 2,  2, 1520, -35, -55,  nil, 'leave_rio_de_la_plata'],

	
	[31,  3, 1520, -49.3, -67.6, nil, 'reach_port_san_julian'],  
			// stay for winter
			// die Essensrationen werden gekürzt
			//nehemen 2 Riesen auf (+2 Leute)
	
	[ 1,  4, 1520, -49.3, -67.6, nil, 'mutiny'],
			//kapitän Cartagena, armada , mendoza , coca gevierteilt, mendoza will flüchten und erschlagen dabei,  
			//MINUS 5 Leute
			//der portugiesische kapitän wird begnadigt was wiederrum Hass hervorruft
			//der portugiesische Kapitän und der Bischof versuchen magellan zu töten, scheitert, sie werden ausgesetzt
			//MINUS 2 Leute
	
	[ 7,  4, 1520, -49.3, -67.6, nil, 'verdict'],

	[10,  4, 1520, -49.3, -67.6,  \storm],		// these are invented, but typical
	[14,  4, 1520, -49.3, -67.6, \normal],		// invented

	[24,  4, 1520, -49.3, -67.6,  \storm],		// invented
	[26,  4, 1520, -49.3, -67.6,  \normal],		// invented


	[14,  5, 1520, -49.3, -67.6,  \storm],		// invented
	[21,  5, 1520, -49.3, -67.6,  \normal],		// invented
	
	[22,  5, 1520, -49.3, -67.6, nil, 'loss_of_ship_santiago'],
			//die Mannschaft überlebt aber durch ein Wunder und wird fernversorgt

	[30,  5, 1520, -49.3, -67.6,  \storm],		// these are invented, but typical
	[5,  6, 1520, -49.3, -67.6, \normal],		// invented

	[8,  6, 1520, -49.3, -67.6,  \storm],		// invented
	[20,  6, 1520, -49.3, -67.6,  \normal],		// invented

	[20,  7, 1520, -49.3, -67.6,  \storm],		// invented
	[27,  7, 1520, -49.3, -67.6,  \normal],		// invented

	[10,  8, 1520, -49.3, -67.6,  \storm],		// invented
	[15,  8, 1520, -49.3, -67.6,  \normal],		// invented
	
	[24,  8, 1520, -49.3, -67.6, nil, 'leave_port_san_julian'],
	
	[14, 9, 1520, -50, -68.5, nil, 'reach_rio_santa_cruz'],
			// laut Pigafetta erreichen sie Santa Cruz schon am 14. September!
			// (bleiben dort 2 monate wegen Gefahr von Schiffbruch)
	
	[14, 10, 1520, -50, -68.5, nil, 'leave_rio_santa_cruz'],
			// laut Pigafetta erreichen sie Santa Cruz schon am 14. September!
			// (bleiben dort 2 monate wegen Gefahr von Schiffbruch)

	[21, 10, 1520, -52.7, -68.35, nil, 'reach_cape_of_virgins'], 
			//San Antonio und Concepcion werden vorausgeschickt
			// entry_of_passage
	[22, 10, 1520, -52.7, -68.35, 'storm'], 
			//San Antonio und Concepcion werden vorausgeschickt
			// entry_of_passage
	[24, 10, 1520, -52.7, -68.35, 'normal'], 
			//San Antonio und Concepcion werden vorausgeschickt
			// entry_of_passage
	
	[25, 10, 1520, -52.7, -68.35, nil, 'enter_passage'],   // 1. 11.!
			//laut Pigafetta fahren sie erst am 1.November in die Meeresenge rein.
			//Magellan nennt die Enge ja auch "Allerheiligenstraße"
	
	[1, 11, 1520, -52.7, -69.5, nil, 'turn'],
	
	[3, 11, 1520, -53.15, -70.9, nil, 'turn'],			// punta arenas
	[5, 11, 1520, -54, -71.2, nil, 'turn'],			// monte victoria
	
	[ 8, 11, 1520, -52.5, -69.5, nil, 'desertion_ship_san_antonio'],
			//schreibt nichts ob die volle Besatzung des Schiffes nach hause fährt, ich nehme die volle besatzung an..
			//MINUS 59 Leute; der ursprüngliche Kapitän wurde ja schon vorher umgebracht 
	
	[28, 11, 1520, -52.8, -74.6, nil, 'enter_pacific_ocean'], // cabo deseado
			//Essensnot, man ißt verfaultes 
			//MINUS 4 Leute: Hungertod
			//Krankheit: Zahnfleischerkrankung, können nichts essen, daher:
			//MINUS 19 Leute (auch einer der Riesen ist dabei)
			//essen Ratten - gibt einen Streit: MINUS 2 Männer 
				// (bei diesem Streit erschlägt der eine den anderen, der Täter wird erdrosselt)
	
	[24,  1, 1521, -18.2, -140.8, nil, 'pass_san_pablo'],	// now hao island
			//Magellan meint die Magnetnadel besitzt auf der südlichen halbkugel keine Kraft den Nordpol zu finden
	
	[ 4,  2, 1521, 0, -160, nil, 'pass_tiburon_islands'], // unauffindbar, interpoliert
			//erreichen Cipangus (Japan)
			//hoffen die Gewürzinseln zu erreichen

	[ 16,  2, 1521, 13, -179.74, nil, 'cross_dateline'], // interpolated: cross dateline!
	[ 17,  2, 1521, 13, 178.56, nil, 'passed_dateline'], // interpolated: cross dateline!
	
	[ 6,  3, 1521, 13.4, 144.7, nil, 'reach_ladrones'],	   // now mariana islands, taking guam
			//es kommt zu Feindseligkeiten zwischen den Einheimischen und der Schiffsbesatzung
			//bei den Feindseligkeiten wird ein Matrose verletzt, Beinamputation, MINUS 1 Mann,  da er an den Folgen stirbt
	
	[16,  3, 1521, 10.8, 125.8, nil, 'reach_samar'],   // wikip says homonhon philippines ...
			//gutes Verhältnis zwischen der Crew und den Insulanern
			//Insulaner werden durch Kanonfeuer stark erschreckt!
			//Laut Wikipedia sind zu dieser Zeit noch 150 Mann am Leben (das stimmt eigentlich mit meiner Berechnung überein!!!!)
	
	[28,  3, 1521, 10, 125, nil, 'reach_massawa'], 	//  stimmt!
			//Königsbesuch - man prahlt mit Waffen
			//Gutes Verhältnis - Tauschhandel
			//Ein bestrafter Matrose wird gegen einen Einheimischen Piloten ausgetauscht
			//Auch der König von Massawa begleitet sie
	
	[ 7,  4, 1521, 10.3, 123.9, nil, 'reach_cebu'],
			//dicke Freundschaft mit dem  König
			//christliche Missionierung - es werden 800 Einheimische getauft
			//MINUS 2 Mann, Tod wegen zuviel Essens, nach der Hungerzeit
			//die Freundschaft ist der Grund für den Angriff auf Mactan
	  
	[27,  4, 1521, 10.2, 124, nil, 'death_magellan_on_mactan'],
			//60 Mann der Besatzung greifen gemeinsam mit Magellan an und stehen einem Heer von 1500 gegenüber
			//Bei dem Kampf sterben 8 Mann und Magellan (MINUS 9 Mann) 
	 
	[ 1,  5, 1521, 10.2, 124, nil, 'death_serrao_and_barbosa'],
     //Verrat durch den Dolmetscher und durch den mittlerweile getauften christlichen König (König von Cebu),
     //Bei diesem Verrat werden 24 Mann (darunter barobosa) erschlagen der Dollmetscher läuft über, und Serranos wird ausgesetzt:
     //MINUS 26 Mann
     
          
	[ 4,  5, 1521, 10.2, 124, nil, 'burn_ship_concepcion'],  
			//schlechte Stimmung wegen des Tods Magellans und wegen der reduzierten Besatzung

//	[ 6,  5, 1521, lat, long, nil, 'deserted_san_antonio_reaches_sevilla'],  

			// erreichen zuerst auch im Juni Panilongon, wo Pigafetta den könig besucht, der ist wohlgesonnen
			// danach gehts nach Cagaian (lage laut pigafetta 7° 30') (gibt keine genauen Daten an...), 
			// und erst dann wird palaoan erreicht
	
	[10,  6, 1521, 9.8, 118.7, nil, 'reach palawan'],  // when? - auch pigafetta macht da keinen genaueren angaben
			//erzählt vom Hahnenkampf

	[21,  6, 1521, 9.8, 118.7, nil, 'leave_palawan'],  // 
	
	[7,  7, 1521, 4.9, 114.9, 'storm'],  // when? stay for 35 days
	[8,  7, 1521, 4.9, 114.9, 'normal', 'reach_brunei'],  // when? stay for 35 days
			//laut pigafetta wird borneo am 8 Juli erreicht, kommen in den schwersten Sturm
			//wieder Austausch von Geschenken
			//MINUS 2 Matrosen laufen zu den Mauren über
			//Verrat durch den König, wollen die Besatzung angreifen, misslingt,
			//2 Matrosen und der Sohn des Steuermanns werden nicht mehr auf das Schiff zurückgelassen (MINUS 3 Mann)
			//Kontakt auch mit den Heiden
	
	[12,  8, 1521, 4.9, 114.9, nil, 'leave_brunei'],  // 
	
			// kommen auf der Insel Cimbombon an, auf der sie die Schiffe reparieren wollen
			//bleiben auf dieser Insel 42 tage, Der aufenhaltszeitraum dürfte zwischen August und September sein.
			//Im September verlassen sie die Insel und bestimmen vorher noch neue Kapitäne(Gonzalo Gomez und del Cano)
			//gleich danach "bringen sie eine Dschunke auf"
			//segeln zurück und suchen die Molukken auf
			//im Oktober segeln sie zur Stadt Maingdanao (Mindanao)
			//26 Oktober überfällt sie ein furchtbarer Sturm
			//erreichen Ende Oktober die Inselgruppe zwischen Celebes und den Philipinen

	[ 8, 11, 1521, 0.64, 127.3, nil, 'reach_tidore_moluccan_island'],  // victoria, trinidad, crew 115
			//der König ist der Crew wohlgesonnen
			//bekommen Gewürznelken (im Tausch)
	
	[18, 12, 1521, 0.64, 127.3, nil, 'trinidad_leaks'],
			//König von Thadore versucht zu helfen, das Leck ist nicht auffindbar
			//die Mannschaft der Trinidad bleibt zurück: also ca: (MINUS ca. 54 Mann)
			//es gibt jetzt noch eine Mannschaft bestehend aus 47 Europäern und 13 Indern!!!
	 
	[21, 12, 1521, 0.64, 127.3, nil, 'victoria_leaves_tidore'],
			//bei 2° und 55 längen kommen sie auf die Insel Sulach (irgendwann im Dezember)
			//auf Sulach wohnen menschenfresser
			//von dort gehts weiter nach Buru (ca. einen Tag von Sulach entfernt)
	
	[8, 1, 1522, -8.2, 125, nil, 'victoria_reaches_Mallue'],
			//freundlich gesonnen
			//genügend Essen
			
	[25,  1, 1522, -8.2, 125, nil, 'victoria_leaves_ombay'],
	
	
	[26, 1, 1522, -9, 125.2, nil, 'victoria_reaches_timor'],
			//zuerst keine Einigung mit dem Befehlshaber über den Tausch
			//nahmen den Häuptling eines Ortes gefangen und erpressen Nahrungsmittel
	
	[13,  2, 1522, -9, 125.2, nil, 'victoria_leaves_timor'],
			//9. März überfällt sie ein Orkan
			//MINUS 4 Mann beim Orkan ertrunken
			//am 18.März fahren sie bei Neu Masterdam vorbei (38° südlicher Breite)
			// schon wieder lebensmittelknappheit, wegen des Orkans,
			//laut Pigafetta erreichen sie das kap der guten Hoffnung bereits am 7.Mai
	 
	[16,  5, 1522, -34.7, 18.9, 'storm', ],
	[18,  5, 1522, -34.7, 18.9, 'normal' ],
	[19,  5, 1522, -34.7, 18.9, \normal, 'round_cape_of_good_hope' ],
			// am 16 Mai überfällt sie wieder ein Sturm
			//kommen deswegen langsamer voran
			//laut pigafetta umrunden sie das Cap erst später 
				// (am 19. Mai sind sie gerade dabei es zu umrunden...)
			//MINUS 21 Mann sterben wegen verhungerns
		
	[ 9,  7, 1522, 14.9, -23.5, nil, 'reach_cabo_verde'],
			//erkennen dass sie sich um einen tag verechnet haben (wegen der Weltumsegelung)
			//MINUS 13 Mann wurden zurückgehalten (noch 22 Mann übrig, davon die Hälfte krank)
	
	[ 12, 7, 1522, 14.9, -23.5, nil, 'leave_cabo_verde'],  // guess date
			//MINUS 4 Mann: verhungert
	
	[ 6,  9, 1522, 36.4, -6.4, nil, 'reach_san_lucar'],
			//18 Mann erreichen San Lucar
	
	[ 8,  9, 1522, 36.7, -6, nil, 'reach_sevilla']
];
