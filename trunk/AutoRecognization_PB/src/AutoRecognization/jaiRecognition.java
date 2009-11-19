/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * jaiRecognition.java
 *
 * Created on Oct 8, 2009, 10:25:17 AM
 */
package AutoRecognization;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 *
 * @author ddda
 */
public class jaiRecognition extends DisplayJAI {

    private OCREngine.Recognition ocrobj = new OCREngine.Recognition();
    private ArrayList<Rectangle> reclist = new ArrayList<Rectangle>();
    private Rectangle newrecanchor = new Rectangle();
    private boolean calcomplete = false;
    public Image img;
    private int scaleX = 900;
    private int scaleY = 150;

    public enum TypeNumer {

        RightBottom,
        LeftBottom
    }

    /** Creates new form jaiRecognition */
    public jaiRecognition() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            scaleY = scaleY + 20;
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            scaleY = scaleY - 20;
        }
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
            scaleX = scaleX - 20;
        }
        if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
            scaleX = scaleX + 20;
        }
        //calculate(TypeNumer.RightBottom);
    }//GEN-LAST:event_formKeyPressed

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        this.requestFocus();
    }//GEN-LAST:event_formMouseEntered

    public ArrayList<String> RecognizeICRImage() {
        ocrobj.setSlideX(0);
        ocrobj.setSlideY(0);
        if (ocrobj.LoadImageICR(img, reclist)) {
            if (ocrobj.callICREngine()) {
                ArrayList<String> result = ocrobj.getAllResultRecognition();
                return result;
            }
        }
        return null;
    }

     public ArrayList<String> RecognizeOCRImage() {
        ocrobj.setSlideX(0);
        ocrobj.setSlideY(0);
        if (ocrobj.LoadImage(img, reclist)) {
            if (ocrobj.callOCREngine()) {
                ArrayList<String> result = ocrobj.getAllResultRecognition();
                return result;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (calcomplete) {
            for (Rectangle temp : reclist) {
                g.setColor(new Color(255, 0, 0, 128));
                g.fillRect(temp.x, temp.y, temp.width, temp.height);
            }
        }
    }

    public void calculate(String type) {
        reclist.clear();
        String[] coordinates = type.split(";");
        for (String strtypes : coordinates) {
            String[] strs = strtypes.split(",");
            int y = 0;
            int x = 0;
            y = this.getHeight() - Integer.valueOf(strs[3].trim());
            x = this.getWidth() - Integer.valueOf(strs[2].trim());
            Rectangle temp = new Rectangle();
            temp.x = x;
            temp.y = y;
            temp.width = Integer.valueOf(strs[0].trim());
            temp.height = Integer.valueOf(strs[1].trim());
            reclist.add(temp);
        }
        calcomplete = true;
        repaint();
    }

    public void calculateForm2(String type) {
        reclist.clear();
        String[] coordinates = type.split(";");
        for (int i = 0; i < 2; i++) {
            String[] strs = coordinates[i].split(",");
            int y = 0;
            int x = 0;
            y = this.getHeight() - Integer.valueOf(strs[3].trim());
            x = this.getWidth() - Integer.valueOf(strs[2].trim());
            Rectangle temp = new Rectangle();
            temp.x = x;
            temp.y = y;
            temp.width = Integer.valueOf(strs[0].trim());
            temp.height = Integer.valueOf(strs[1].trim());
            reclist.add(temp);
        }
        calcomplete = true;
        repaint();
    }

    public void calculateForm2Text(String type) {
        reclist.clear();
        String[] coordinates = type.split(";");
        String[] strs = coordinates[2].split(",");
        int y = 0;
        int x = 0;
        y = this.getHeight() - Integer.valueOf(strs[3].trim());
        x = this.getWidth() - Integer.valueOf(strs[2].trim());
        Rectangle temp = new Rectangle();
        temp.x = x;
        temp.y = y;
        temp.width = Integer.valueOf(strs[0].trim());
        temp.height = Integer.valueOf(strs[1].trim());
        reclist.add(temp);
        calcomplete = true;
        repaint();
    }

     public void calculateForm2Page2(String type) {
        reclist.clear();
        String[] coordinates = type.split(";");
        String[] strs = coordinates[3].split(",");
        int y = 0;
        int x = 0;
        y = this.getHeight() - Integer.valueOf(strs[3].trim());
        x = this.getWidth() - Integer.valueOf(strs[2].trim());
        Rectangle temp = new Rectangle();
        temp.x = x;
        temp.y = y;
        temp.width = Integer.valueOf(strs[0].trim());
        temp.height = Integer.valueOf(strs[1].trim());
        reclist.add(temp);
        calcomplete = true;
        repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
