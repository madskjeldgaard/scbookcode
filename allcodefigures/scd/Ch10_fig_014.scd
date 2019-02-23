Figure 10.14

(
~mySlider.adjustCurve([ 0 @ 0, 0.25 @ 1, 0.5 @ 0, 1.0 @ 0.5 ]);
~mySlider.view.drawFunc = { arg view;
    var b, scale, lnP1, lnP2, linePos, proj, inner, sum = 0;
    b     = view.bounds;
    // scaling factor for coordinates normalized to 0...1
    scale = b.width @ b.height;
    // set the initial curve coordinate
    JPen.moveTo( ~mySlider.curve[ 0 ] * scale );
    (1..(~mySlider.curve.size-1)).do({ arg i;
        // create the curve by adding successive line segments
        JPen.lineTo( ~mySlider.curve[ i ] * scale );
    });
    // stroke the curve with a 2-pixel wide black pen
    JPen.width = 2;
    JPen.color = Color.black;
    JPen.stroke;
    // draw the knob: iterate over the line segments until
    // the one is found inside which the knob (read from ~mySlider.pos)
    // is located.
    block { arg break;
        ~mySlider.lineLens.do({ arg len, i;
            // if we have found the line segment...
            if( sum + len >= ~mySlider.pos, {
                // calucate the point inside this segment
                // (proj) and draw a filled circle around it
                #lnP1, lnP2    = ~mySlider.lines[ i ];
                linePos        = (~mySlider.pos - sum) / len;
                proj           = lnP1 + ((lnP2 - lnP1) * linePos);
                JPen.fillOval( Rect.aboutPoint( proj * scale, 8, 8 ));
                // if this view is focused, use blue color, otherwise grey
                JPen.fillColor = Color.hsv( 0.7, view.hasFocus.if( 0.7, 0.0 ), 1.0 );
                // if the mouse is pressed, the outline should be thicker
                inner          = if( ~mySlider.pressed, 5, 7 );
                JPen.fillOval( Rect.aboutPoint( proj * scale, inner, inner ));
                break.value;
            });
            sum = sum + len;
        });
    };
    
    // a debugging utility to visualize the point projections
    // of the mouse dragging
    if( ~mySlider.project, {
        ~mySlider.projections.do({ arg pt;
            pt = pt * scale;
            JPen.line( pt + (-5 @ -5), pt + (5 @  5) );
            JPen.line( pt + (-5 @  5), pt + (5 @ -5) );
        });
        JPen.width = 1; JPen.stroke;
    });
};
)

