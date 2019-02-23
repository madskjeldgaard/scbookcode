package de.sciss.swingosc.test;

// THIS IS THE ORIGINAL SOURCE CODE FROM
// http://java.sun.com/applets/other/Bubbles/src/Bubbles.java

/*
 * @(#)Bubbles.java	1.3 97/07/21
 *
 * Copyright (c) 1994-1995 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted. 
 * Please refer to the file http://java.sun.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://java.sun.com/licensing.html for further important licensing
 * information for the Java (tm) Technology.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").  SUN
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 *
 * Author: Rachel Gollub, 1995
 *
 */

import java.awt.*;

public class Bubbles extends java.applet.Applet implements Runnable {
  Thread artist=null;
  int bubble=0, thisbubble=0;	// # of bubbles on screen, and current bubble
  int MAXBUBBLES = 25;		 // Maximum bubbles on screen
  int stepper = 4;		 // Counter for which bubbles to move when
  int record[][] = new int[MAXBUBBLES][5];  // Array that holds bubbles
  Color col = null;		// Color holder
  
public void init() {
  resize(500,500);		// Set bubble window size
}

public void move_bubble(int x, int y, int r, int step, Graphics g) {
  int i;

  for (i=x-r; i<=x+r; i++) {	 // Draws the upper edge of a circle
    g.drawLine(i, y - (int)(Math.sqrt( r*r - ( (i-x)*(i-x) ))),
	       i, y + step - (int)(Math.sqrt( r*r - ( (i-x)*(i-x) ))));
  }
  g.setColor(getBackground());
  for (i=x-r; i<=x+r; i++) {	 // Draws the lower edge of the circle
    g.drawLine(i, y + (int)(Math.sqrt( r*r - ( (i-x)*(i-x) ))),
	       i, y + step + (int)(Math.sqrt( r*r - ( (i-x)*(i-x) ))));
  }
}

public void paint(Graphics g) {
  int i, j, tmp;

  if (bubble < MAXBUBBLES || thisbubble < MAXBUBBLES) {	   
    record[thisbubble][0]=(int)(Math.random() * 500);
    record[thisbubble][1]=550;
    record[thisbubble][2]=(int)(Math.random() * 500)/20;
    record[thisbubble][3]=(int)(Math.random() * 255);
    record[thisbubble][4]=(int)(Math.random() * 255);
    col = new Color(record[thisbubble][3], record[thisbubble][4], 255);
    g.setColor(col);
    g.fillOval(record[thisbubble][0]-record[thisbubble][2],
	       record[thisbubble][1]-record[thisbubble][2],
		record[thisbubble][2]*2,record[thisbubble][2]*2);
    if (bubble < MAXBUBBLES) {
      bubble++; thisbubble++;
    }
    else
      thisbubble = MAXBUBBLES;
  }
  for (i=0; i<bubble; i++) {
    if (i%5 <= stepper) { // Steps each bubble at a different speed
      record[i][1] -= 1;
      col = new Color(record[i][3], record[i][4], 255);
      g.setColor(col);
      move_bubble(record[i][0], record[i][1], record[i][2], 1, g);
      for (j=0; j<i; j++) {   // Checks for touching bubbles, pops one
	tmp = ( (record[i][1]-record[j][1])*(record[i][1]-record[j][1]) +
		(record[i][0]-record[j][0])*(record[i][0]-record[j][0]) );
	if (j != i && Math.sqrt(tmp) < record[i][2] + record[j][2]) {
	  g.setColor(getBackground());
	  for (tmp = record[i][2]; tmp >= -1; tmp = tmp - 2)
	    g.fillOval(record[i][0]-(record[i][2]-tmp), 
		       record[i][1]-(record[i][2]-tmp), 
		       (record[i][2]-tmp)*2, (record[i][2]-tmp)*2);
	  col = new Color(record[j][3], record[j][4], 255);
	  g.setColor(col);
	  g.fillOval(record[j][0]-record[j][2], record[j][1]-record[j][2], 
		     record[j][2]*2, record[j][2]*2);
	  record[i][1] = -1; record[i][2]=0;
	}
      }
    }
    if (record[i][1]+record[i][2] < 0 && bubble >= MAXBUBBLES) {
      thisbubble = i;
    }
    stepper=(int)(Math.random()*10);
    col = null;
  }
}

public void update(Graphics g) {
  paint(g);
}

public void start() {
  if (artist == null) {
    artist = new Thread(this);
    artist.start();
  }
}

public void stop() {
    artist = null;
}

public void run() {
  while (artist != null) {
    try {Thread.sleep(20);} catch (InterruptedException e){ /* ignored */ }
    repaint();
  }
  artist = null;
}
  
public String getAppletInfo() {
  return "Title: Bubbles\nAuthor: Rachel Gollub";
  }
}