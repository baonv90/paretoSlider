/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sliders;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**-
 *
 * @author laurillau
 */
public class ParetoSliders extends JPanel implements MouseListener, MouseMotionListener {
    private static final int PADX = 10;
    private static final int DELTA_H_0 = 50;    
    private static final int HEIGHT = 20;
    private static final int DELTA_HEIGHT = 200;
    private static final int STEPS = 100;
    private static final String [] names = { "Confort T˚", "Q air", "Coût"};
    private static final String [][] ticks = { { "faible", "correct", "bon"},
                                               { "médiocre", "correcte", "bonne"},
                                               { "cher", "raisonnable", "éco"} };
    
    private int nb_sliders, priority;
    private Slider [] sliders = null;
    private Function [] funcs;
    private Targets targets;
    
    private Intervals [] intervals;
    
    private double [][] domains = new double[][] { { 0, 2 }, { 0, 2 }, { 0, 2 }};
     
    private double [][] graphs;
            
    public ParetoSliders(int nb_sliders) {
        setBackground(new Color(192,192,192));
        
        this.nb_sliders = Math.max(0, nb_sliders);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        
        priority = 0;
        targets = new Targets();
        funcs = new Function[nb_sliders];
        intervals = new Intervals[nb_sliders];        
        for(int i = 0; i < nb_sliders; i++) {
            funcs[i] = new Function(i);
            intervals[i] = new Intervals();
        }    
        
        double min = 0, max = 0, f;
        
        
        Intervals [] ranges;
        graphs = new double[3][101];        
        for(int j = 0; j < 3; j++) {
            for(int i = 0; i <= 100; i++) {
                double x = i * 0.02, lg = 0;
                ranges = funcs[j].findIntervals(x, domains, 100);
            
                for(Interval range : ranges[0]) {
                    f = funcs[j].lg(x, range.bmin, range.bmax, 100);
                    if (! Double.isNaN(f)) {
                        lg += f;
                    }
                }
                
                if ((i == 0) && (j == 0)) {
                    min = max = lg;
                } else {
                    if (lg < min) { min = lg; }
                    if (lg > max) { max = lg; }
                }
                
                graphs[j][i] = lg;
            }
        }
        
        for(int j = 0; j < 3; j++) {
            for(int i = 0; i <= 100; i++) {
                if (max != min) {
                    graphs[j][i] = (graphs[j][i] - min) / (max - min);
                } else {
                    graphs[j][i] = 0;
                }
            }
        }
    }
    
    private void getIntervals() {
        double u;
        Interval inter;
        int offset = 0;        
        double umin, umax;
        Intervals [] ranges;
        
        umin = domains[priority][0];
        umax = domains[priority][1];
        
        u = umin + sliders[priority].pos * (umax - umin);
        
        ranges = funcs[priority].findIntervals(u, domains, STEPS);

        for(int i = 0; i < nb_sliders; i++) {
            intervals[i].clear();            
            if (i != priority) {
                double mmin, mwidth;
                
                mmin = domains[offset][0];
                mwidth = domains[offset][1] - mmin;
                
                for(Interval range : ranges[offset]) {
                    inter = new Interval();
                    inter.bmin = (range.bmin - mmin) / mwidth;
                    inter.bmax = (range.bmax - mmin) / mwidth;
                    intervals[i].add(inter);
                }
                
                offset++;
            }
        }
    }   

    private void getTargets() {
        Target t;
        Double [] values;
        double p;
        double u, umin, umax;
        double v, vmin, vmax;
        double w, wmin, wmax;
        
        int v_index = (priority == 0) ? 1 : 0;
        int w_index = 3 - priority - v_index;
        
        umin = domains[priority][0];
        umax = domains[priority][1];        
        u = umin + sliders[priority].pos * (umax - umin);
        
        vmin = domains[v_index][0];
        vmax = domains[v_index][1];        
        v = vmin + sliders[v_index].pos * (vmax - vmin);

        wmin = domains[w_index][0];
        wmax = domains[w_index][1];        
        w = wmin + sliders[w_index].pos * (wmax - wmin);

        targets.clear();
        if (priority < w_index) {
            values = funcs[v_index].findValues(u, w, vmin, vmax, STEPS);
        } else {
            values = funcs[v_index].findValues(w, u, vmin, vmax, STEPS);
        }
        
        for(double pos : values) {
            p = (pos - vmin) / (vmax - vmin);
            
            for(Interval inter : intervals[v_index]) {
                if ((p >= inter.bmin) && (p <= inter.bmax)) {
                    t = new Target();
                    t.src = w_index;
                    t.dst = v_index;
                    t.pos = p;
                    targets.add(t);
                    
                    break;
                }
            }
        }

        if (priority < v_index) {
            values = funcs[w_index].findValues(u, v, wmin, wmax, STEPS);
        } else {
            values = funcs[w_index].findValues(v, u, wmin, wmax, STEPS);
        }
        
        for(double pos : values) {
            p = (pos - wmin) / (wmax - wmin);
            
            for(Interval inter : intervals[w_index]) {
                if ((p >= inter.bmin) && (p <= inter.bmax)) {
                    t = new Target();
                    t.src = v_index;
                    t.dst = w_index;
                    t.pos = p;
                    targets.add(t);
                    
                    break;
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Dimension dim = getSize();
        Graphics2D g2d = (Graphics2D) g;        
        double width  = dim.width - 2 * PADX;
        double x, y;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (sliders == null) {
            sliders = new Slider[nb_sliders];
            for(int i = 0; i < nb_sliders; i++) {
                sliders[i] = new Slider(width - 3 * HEIGHT, HEIGHT, names[i], ticks[i]);
                sliders[i].setIntervals(intervals[i]);
            }
            sliders[priority].selected = true;
            setPriority(priority);
        }
        
        // draw range
        x = PADX + 3 * HEIGHT; y = DELTA_H_0;
        for(int i = 0; i < nb_sliders; i++) {
            sliders[i].drawRange(g2d, x, y);
            sliders[i].drawRadio(g2d, x, y);
            
            y += DELTA_HEIGHT;
        }
        
        
        double a1, b1, a2, b2, dx = sliders[0].width / 101.0;
        
        for(int j = 0; j < 3; j++) {
            a1 = PADX + 3 * HEIGHT;
            b1 = DELTA_H_0 - 20 * graphs[j][0] - 5 + j * DELTA_HEIGHT;
            g2d.setColor(Color.YELLOW);
            for(int i = 1; i <= 100; i++) {
                a2 = a1 + dx;
                b2 = DELTA_H_0 - 20 * graphs[j][i] - 5 + j * DELTA_HEIGHT;
                g2d.drawLine((int) a1, (int) b1, (int) a2, (int) b2);
                a1 = a2; b1 = b2;
            }
        }
        
        // draw lines
        if (selected_slider == priority) {
            double x2, y2;
            double x1 = sliders[selected_slider].xpos;
            double y1 = sliders[selected_slider].ypos;
            
            g2d.setColor(Color.gray);
            x = PADX + 3 * HEIGHT; y = DELTA_H_0;
            for(int i = 0; i < nb_sliders; i++) {
                if (i != priority) {
                    y2 = y;
                    if (i < priority) { y2 += sliders[i].height; }
                    
                    for(Interval range : sliders[i].intervals) {
                        x2 = x + sliders[i].width * range.bmin;
                        drawDashedLine(g2d, (int) x1, (int) y1, (int) x2, (int) y2);
                        x2 = x + sliders[i].width * range.bmax;
                        drawDashedLine(g2d, (int) x1, (int) y1, (int) x2, (int) y2);                        
                    }
                }
                y += DELTA_HEIGHT;
            }            
        }

        // draw cursors
        x = PADX+ 3 * HEIGHT; y = DELTA_H_0;
        for(int i = 0; i < nb_sliders; i++) {
            sliders[i].drawCursor(g2d, x, y);
            y += DELTA_HEIGHT;
        }
        
        if (selected_slider != -1) {
            Slider s;
            int diameter;
            int x1, y1, x2, y2;
        
            // draw targets
            g2d.setColor(Color.orange);        
            x = PADX+ 3 * HEIGHT;
            for(Target t : targets) {
                if (t.src != selected_slider) { continue; }
                
                s = sliders[t.src];
                x1 = (int) s.xpos;
                y1 = (int) s.ypos;
            
                s = sliders[t.dst];
                y = DELTA_H_0 + t.dst * DELTA_HEIGHT;
                x2 = (int) (x + t.pos * s.width - s.height / 2 + 1);
                y2 = (int) (y + 1);
                diameter = (int) s.height - 1;            
                g2d.fillOval(x2, y2, diameter, diameter);
            
                x2 = (int) (x + t.pos * s.width + 1);
                y2 = (int) (y + s.height / 2 + 1);
                drawDashedLine(g2d, (int) x1, (int) y1, (int) x2, (int) y2);
            }
        }        
    }
    
    public void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2){

        //creates a copy of the Graphics instance
        Graphics2D g2d = (Graphics2D) g.create();

        //set the stroke of the copy, not the original 
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x1, y1, x2, y2);

        //gets rid of the copy
        g2d.dispose();
}
    private int selected_slider = -1;
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        for(int i = 0; i < nb_sliders; i++) {
            if (sliders[i].isInRadio(x, y)) {
                sliders[i].selected = true;
                sliders[priority].selected = false;
                setPriority(i);
                return;
            }
        }
        
        if (selected_slider == -1) {
            for(int i = 0; i < nb_sliders; i++) {
                if (sliders[i].isInside(x, y)) {
                    selected_slider = i;
                    sliders[i].press(x, y);
                    repaint();                    
                    break;
                }
            }
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (selected_slider != -1) {
            sliders[selected_slider].move(e.getX(), e.getY());
            
            if (selected_slider == priority) {
                this.getIntervals();
            }
            this.getTargets();
            
            repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (selected_slider != -1) {
            sliders[selected_slider].release(e.getX(), e.getY());
            repaint();            
        }
        selected_slider = -1;        
    }  
    
    public void setPriority(int p) {
        this.priority = p;
        
        this.getIntervals();
        this.getTargets();
        
        repaint();
    }
}
