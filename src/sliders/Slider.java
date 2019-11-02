/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sliders;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author laurillau
 */
public class Slider {
    public double width, height, xpos = 0, ypos = 0, rxpos, rypos;
    public Intervals intervals;
    public double pos = 0.5;
    public boolean selected = false;
    
    private String label;
    private String [] ticks;
    public Slider(double width, double height, String label, String [] ticks) {
        this.width = width;
        this.height = height;
        
        this.label = label;
        this.ticks = ticks; 
    }
    
    public void setIntervals(Intervals intervals) {
        this.intervals = intervals;
    }

    public void setPos(double pos) {
        this.pos = pos;
        
        if (pos < 0) { pos = 0; }
        if (pos > 1) { pos = 1; }
    }

    public void drawRadio(Graphics2D g2d, double x, double y) {
        int xi = (int) (x - 3 * height);
        int yi = (int) (y - height / 4);
        int diameter = (int) (height + height / 2 - 1);
        
        rxpos = x - 2.25 * height;
        rypos = y + height / 2;
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(xi, yi, diameter, diameter);
        
        g2d.setColor(Color.black);
        
        if (selected) {
            g2d.fillOval((int) (xi + height / 4), (int) y, (int) height, (int) height);
        }
        
        g2d.drawOval(xi, yi, diameter, diameter);
        
    }
    
    public void drawRange(Graphics2D g2d, double x, double y) {
        int xi = (int) x;
        int yi = (int) y;
        int wi = (int) width;
        int hi = (int) height;
        double min, max = 1.0;
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, (int) x, (int) (y - 15));
        g2d.drawString(ticks[0], (int) (x - 20), (int) (y + hi + 30));
        g2d.drawString(ticks[1], (int) (x + width / 2 - 30), (int) (y + hi + 35));
        g2d.drawString(ticks[2], (int) (x + width - 30), (int) (y + hi + 35));
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xi, yi, wi, hi);
        g2d.fillOval((int) (x - 4), (int) (y + hi + 6), 9, 9);
        g2d.fillOval((int) (x + width - 5), (int) (y + hi + 12), 9, 9);
        g2d.fillOval((int) (x + width/2 - 4), (int) (y + hi + 12), 9, 9);
        
        
        if ((intervals != null) && (intervals.size() > 0)) {
            Interval interval = intervals.get(0);
        
            if (interval.bmin > 0) {
                g2d.setColor(Color.red);
                g2d.fillRect(xi, yi, (int) (width * interval.bmin), hi);
            }

            min = interval.bmax;
            for(int i = 1; i < intervals.size(); i++) {
                interval = intervals.get(i);            
                max = interval.bmin;
                g2d.setColor(Color.red);
                g2d.fillRect((int) (x + min * width), (int) y, (int) (width * (max - min)), hi);
            
                min = interval.bmax;
            }
        
            if (min < 1.0) {
                g2d.setColor(Color.gray);
                g2d.fillRect((int) (x + min * width), (int) y, (int) (width * (1 - min)), hi);
            }            
        }

        g2d.setColor(Color.black);
        g2d.drawRect(xi, yi, wi - 1, hi);
    }
    
    public void drawCursor(Graphics2D g2d, double x, double y) {
        xpos = x + pos * width;        
        ypos = y + height / 2.0;
        
        int xi = (int) (xpos - height);
        int yi = (int) (ypos - height);
        int diameter = (int) (height + height);
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(xi, yi, diameter, diameter);
        g2d.setColor(Color.black);
        g2d.drawOval(xi, yi, diameter, diameter);
    }

    private boolean dragged = false;
    private double prevX;
    
    public boolean isInside(double x, double y) {
        double r = Math.hypot(xpos - x, ypos - y);
        if (r < height) {
            dragged = true;
            prevX = x;
            return true;
        }
        
        return false;
        
    }

    public boolean isInRadio(double x, double y) {
        double r = Math.hypot(rxpos - x, rypos - y);
        if (r < 1.5 * height) {
            return true;
        }
        
        return false;
    }
    
    public void press(double x, double y) {
        dragged = true;
        prevX = x;
    }
    
    public void move(double x, double y) {
        if (dragged) {
            pos += (x - prevX) / width;
            if (pos < 0) { pos = 0; }
            if (pos > 1) { pos = 1; }
            prevX = x;
        }
    }

    public void release(double x, double y) {
        dragged = false;
    } 
}
