/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AnchorRecognitionForm.java
 *
 * Created on Oct 30, 2009, 2:12:45 PM
 */
package AnchorRecognition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author ddda
 */
public class AnchorRecognitionForm extends javax.swing.JFrame {

    private JFileChooser chooser = new JFileChooser();
    private String imgpathglobal = "";
    private TiffImageProcessing tiff = new TiffImageProcessing();
    private static AnchorRecognitionForm instance = null;
    private StringBuilder sb = new StringBuilder();

    public static AnchorRecognitionForm Instance() {
        if (instance == null) {
            instance = new AnchorRecognitionForm();
        }
        return instance;
    }

    public void setTextAxis(String value) {
        lblAxis.setText(value);
        lblAxis.updateUI();
    }

    private AnchorRecognitionForm() {
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

        pnlMain = new javax.swing.JPanel();
        pnlPanel = new javax.swing.JPanel();
        cmdLoadImage = new javax.swing.JButton();
        lblAxis = new javax.swing.JLabel();
        cmdSave = new javax.swing.JButton();
        cmdSaveConfig = new javax.swing.JButton();
        scrImage = new javax.swing.JScrollPane();
        imageDisplayer = new AnchorRecognition.ImageDisplayer();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Anchor Recognition");

        pnlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Panel")));

        cmdLoadImage.setText("Load Image");
        cmdLoadImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLoadImageActionPerformed(evt);
            }
        });

        lblAxis.setText("Axis: ");
        lblAxis.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cmdSave.setText("Accept");
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });

        cmdSaveConfig.setText("Save");
        cmdSaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlPanelLayout = new javax.swing.GroupLayout(pnlPanel);
        pnlPanel.setLayout(pnlPanelLayout);
        pnlPanelLayout.setHorizontalGroup(
            pnlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPanelLayout.createSequentialGroup()
                .addComponent(cmdLoadImage, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdSaveConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlPanelLayout.setVerticalGroup(
            pnlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPanelLayout.createSequentialGroup()
                .addGroup(pnlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdLoadImage)
                    .addComponent(lblAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdSave)
                    .addComponent(cmdSaveConfig))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout imageDisplayerLayout = new javax.swing.GroupLayout(imageDisplayer);
        imageDisplayer.setLayout(imageDisplayerLayout);
        imageDisplayerLayout.setHorizontalGroup(
            imageDisplayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 830, Short.MAX_VALUE)
        );
        imageDisplayerLayout.setVerticalGroup(
            imageDisplayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 524, Short.MAX_VALUE)
        );

        scrImage.setViewportView(imageDisplayer);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrImage, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(pnlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrImage, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdLoadImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLoadImageActionPerformed
        ChooseFileImage();
    }//GEN-LAST:event_cmdLoadImageActionPerformed

    private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveActionPerformed
        sb.append(imageDisplayer.getConfig());
        sb.append("\n");
    }//GEN-LAST:event_cmdSaveActionPerformed

    private void cmdSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveConfigActionPerformed
        try {
            Writer output = null;
            File file = new File("write.txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(sb.toString());
            output.close();
            JOptionPane.showMessageDialog(this, sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(AnchorRecognitionForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_cmdSaveConfigActionPerformed

    private void ChooseFileImage() {
        chooser.setCurrentDirectory(new File("D:\\EBook\\OCR\\Image\\new Project"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imgpathglobal = chooser.getSelectedFile().getAbsolutePath();
            setImage(imgpathglobal);
        }
    }

    private void setImage(String filename) {
        PlanarImage image = null;
        this.repaint();
        try {
            try {
                image = tiff.readImage(filename, 0, 100, 0);
                RandomIterFactory.create(image, null);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            imageDisplayer.set(image);
            imageDisplayer.repaint();
        } catch (Exception ex) {
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                AnchorRecognitionForm.Instance().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdLoadImage;
    private javax.swing.JButton cmdSave;
    private javax.swing.JButton cmdSaveConfig;
    private AnchorRecognition.ImageDisplayer imageDisplayer;
    private javax.swing.JLabel lblAxis;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlPanel;
    private javax.swing.JScrollPane scrImage;
    // End of variables declaration//GEN-END:variables
}
