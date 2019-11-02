/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sliders;

import java.awt.BorderLayout;
import javax.swing.JFrame;

/**
 *
 * @author laurillau
 */
public class Sliders {
    
    private ParetoSliders pSliders = new ParetoSliders(3);
    
    public Sliders() {
        JFrame fenetre;

        fenetre = new JFrame("Sliders");        
        fenetre.setLayout(new BorderLayout());
        fenetre.add(pSliders, BorderLayout.CENTER);
        fenetre.setSize(800,800);
        fenetre.setVisible(true);                        
    }
}
