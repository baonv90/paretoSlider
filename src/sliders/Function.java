package sliders;

import java.util.ArrayList;

public class Function {
    
    private static double func(double x, double y, double z) {
        double u = x - 2.0d;
        double v = y - 2.0d;
        double w =   ((u * u + 0.8d) * v * v) / 4.0d
                    - (0.6d / (1.5d * 1.5d)) * (x * x)
                    + (0.3d * x) / 1.5d 
                    + 0.3d 
                    - z;                
        return w;
    }
    
    private static double fz(double x, double y) {
        double u = x - 2.0d;
        double v = y - 2.0d;
        double w =   ((u * u + 0.8d) * v * v) / 4.0d
                    - (0.6d / (1.5d * 1.5d)) * (x * x)
                    + (0.3d * x) / 1.5d 
                    + 0.3d;                
        return w;
    }

    private static double fy(double x, double z) {
        double u = x - 2.0d;
               
        return 2.0d - 2.0d * Math.sqrt(  (z - 0.3d - (0.3d * x) / 1.5d 
                                          + (0.6d / (1.5d * 1.5d)) * (x * x)
                                         ) / (u * u + 0.8d));
    }

    public double lg(double u, double vmin, double vmax, int nb) {
        switch(n) {
            case 0:  return lgX(u, vmin, vmax, nb);                        
            case 1:  return lgY(u, vmin, vmax, nb);                
            default: return lgZ(u, vmin, vmax, nb);
                
        }        
    }
    // lg X, z = f(y)
    public static double lgX(double x, double ymin, double ymax, int n) {
        double lg = 0;
        double z1, z2, dz, dy = (ymax - ymin) / n;
        
        z1 = fz(x, ymin);
        for(int i = 1; i <= n; i++) {
            z2 = fz(x, ymin + i * dy);
            dz = z2 - z1;            
            lg += Math.sqrt(dz * dz + dy * dy);
            
            z1 = z2;
        }
        
        return lg;
    }
    
    // lg Y, z = f(y)
    public static double lgY(double y, double xmin, double xmax, int n) {
        double lg = 0;
        double z1, z2, dz, dx = (xmax - xmin) / n;
        
        z1 = fz(xmin, y);
        for(int i = 1; i <= n; i++) {
            z2 = fz(xmin + i * dx, y);
            dz = z2 - z1;            
            lg += Math.sqrt(dz * dz + dx * dx);
            
            z1 = z2;
        }
        
        return lg;
    }
    
    // lg Z, y = g(x)
    public static double lgZ(double z, double xmin, double xmax, int n) {
        double lg = 0;
        double y1, y2, dy, dx = (xmax - xmin) / n;
        
        y1 = fy(xmin, z);
        for(int i = 1; i <= n; i++) {
            y2 = fy(xmin + i * dx, z);
            dy = y2 - y1;            
            lg += Math.sqrt(dy * dy + dx * dx);
            
            y1 = y2;
        }
        
        return lg;
    }
    
    
    private int n;
    public Function(int n) {
        this.n = n;
    }
    
    public double f(double u, double v, double w) {
        switch(n) {
            case 0:  return func(u, v, w);                        
            case 1:  return func(v, u, w);                
            default: return func(v, w, u);
                
        }
    }
    
    public static  Double [] getValues(double [] uvals, int [] ubool, int steps) {
        int prec = 0;
        ArrayList<Double> res = new ArrayList<Double>();
        
        for(int i = 0; i <= steps; i++) {
            int u = ubool[i];
            if (prec != 0) {
                if (u != prec) {
                    res.add((uvals[i] + uvals[i - 1]) / 2.0d);
                }
            }
            prec = u;
        }
        
        return res.toArray(new Double[0]);
    }
    
    public static  Intervals getIntervals(double [] uvals, int [] ubool, int steps) {
        double umin = 0, umax = 0;
        boolean newInterval = false;
        Intervals intervals = new Intervals();
        
        for(int i = 0; i <= steps; i++) {
            if (Math.abs(ubool[i]) <= steps) {
                umax = uvals[i];
                
                if (! newInterval) {
                    newInterval = true;
                    
                    if (i == 0) {
                        umin = umax;
                    } else {
                        umin = (umax + uvals[i - 1]) / 2.0;
                    }
                } else if (i == steps) {
                    Interval interval = new Interval();
                    interval.bmin = umin;
                    interval.bmax = umax;
                    intervals.add(interval);
                }
            } else if (newInterval) {
                Interval interval = new Interval();
                interval.bmin = umin;
                interval.bmax = (umax + uvals[i]) / 2.0;
                intervals.add(interval);
                newInterval = false;
            }
        }
        
        return intervals;
    }
        
    public  Double [] findValues(double v, double w, double umin, double umax, int steps) {
        int [] ubool = new int[steps + 1];        
        double [] uvals = new double[steps + 1];        
        double u, du = (umax - umin) / steps;

        for(int i = 0; i <= steps; i++) {
            u = umin + i * du;
            uvals[i] = u;
            ubool[i] = ((f(u, v, w) < 0) ? -1 : 1);
        }
        uvals[steps] = umax;
        
        return getValues(uvals, ubool, steps);
    }

    public Intervals [] findIntervals(double u, double [][] domains, int steps) {
        Intervals [] res;
        double vmin = domains[0][0];
        double vmax = domains[0][1];
        double wmin = domains[1][0];
        double wmax = domains[1][1];        
        int [] wbool = new int[steps + 1]; 
        int [] vbool = new int[steps + 1];
        double [] wvals = new double[steps + 1];        
        double [] vvals = new double[steps + 1];
        double w, dw = (wmax - wmin) / steps;
        double v, dv = (vmax - vmin) / steps;

        for(int i = 0; i <= steps; i++) {
            w = wmin + i * dw; wvals[i] = w; wbool[i] = 0; 
            v = vmin + i * dv; vvals[i] = v; vbool[i] = 0;
        }
        vvals[steps] = vmax;
        wvals[steps] = wmax;
        
        for(int i = 0; i <= steps; i++) {
            int cw = 0;
            v = vvals[i];
            
            for(int j = 0; j <= steps; j++) {
                w = wvals[j];
                if (f(u, v, w) < 0) {
                    cw += -1;
                    wbool[j] += -1;
                } else {
                    cw += 1;
                    wbool[j] += 1;
                }
            }
            vbool[i] = cw;
        }
        
        res = new Intervals[2];
        res[0] = getIntervals(vvals, vbool, steps);                
        res[1] = getIntervals(wvals, wbool, steps);
        
        return res;
    }
}
