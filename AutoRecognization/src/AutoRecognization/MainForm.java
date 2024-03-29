/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainForm.java
 *
 * Created on Oct 8, 2009, 9:25:16 AM
 */
package AutoRecognization;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author ddda
 */
public class MainForm extends javax.swing.JFrame {

    private JFileChooser chooser = null;
    private String imgpathglobal = "";
    private DefaultListModel dlm = null;
    private TiffImageProcessing tiff = null;
    private StringBuilder contentOCR = null;
    private StringBuilder failcontentOCR = null;
    private String[] arrcn = null;
    private static MainForm instance = null;

    public static MainForm Instance(){
        if(instance==null){
            instance = new MainForm();
        }
        return instance;
    }

    public JFileChooser getChooser() {
        if (chooser == null) {
            chooser = new JFileChooser();
        }
        return chooser;
    }

    public TiffImageProcessing getTiff() {
        if (tiff == null) {
            tiff = new TiffImageProcessing();
        }
        return tiff;
    }

    public StringBuilder getContentOCR() {
        if (contentOCR == null) {
            contentOCR = new StringBuilder();
        }
        return contentOCR;
    }

    public StringBuilder getFailcontentOCR() {
        if (failcontentOCR == null) {
            failcontentOCR = new StringBuilder();
        }
        return failcontentOCR;
    }

    public DefaultListModel getDlm() {
        if (dlm == null) {
            dlm = new DefaultListModel();
        }
        return dlm;
    }
    ActionListener actionListener = new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
            jspImageRecognition.getVerticalScrollBar().setValue(jspImageRecognition.getVerticalScrollBar().getMaximum());
            jspImageRecognition.getHorizontalScrollBar().setValue(jspImageRecognition.getHorizontalScrollBar().getMaximum());
            matchForm();
            writeResult();
            if (chkAuto.isSelected() && lstImages.getSelectedIndex() >= getDlm().getSize() - 1) {
                timer.stop();
            }
            if (chkAuto.isSelected()) {
                lstImages.setSelectedIndex(lstImages.getSelectedIndex() + 1);
                lstImages.ensureIndexIsVisible(lstImages.getSelectedIndex());
            } else {
                timer.stop();
            }
        }
    };
    javax.swing.Timer timer = new javax.swing.Timer(10, actionListener);

    public MainForm() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        initComponents();
        getChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        lstImages.setModel(getDlm());
        this.setLocationRelativeTo(null);
        String[] type = Configuration.Instance().readTemplete().split("\n");
        for (String str : type) {
            cbxType.addItem(str);
        }
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        arrcn = Configuration.Instance().readConnection().split(";");
    }

    private void ChooseFileImage() {
        String defaultpath = Configuration.Instance().readConfig();
        getChooser().setCurrentDirectory(new File(defaultpath));
        getDlm().clear();
        int returnVal = getChooser().showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            imgpathglobal = getChooser().getSelectedFile().getAbsolutePath();
            File folder = new File(imgpathglobal);
            for (File f : folder.listFiles()) {
                if (f.getAbsolutePath().endsWith(".tif")) {
                    getDlm().addElement(f.getAbsolutePath());
                }
            }
            if (getDlm().capacity() > 0) {
                lstImages.setSelectedIndex(0);
            }
            Configuration.Instance().writeConfig(chooser.getSelectedFile().getParent());
        }
    }

    private void matchForm() {
        File focr = new File(lstImages.getSelectedValue().toString());
        String strpath = focr.getParentFile().getParentFile().getName() + "/" + focr.getParentFile().getName() + "/" + focr.getName();
        String ret = "";
        for (int i = 0; i < cbxType.getItemCount(); i++) {
            jaiRecognitionctr.calculate(cbxType.getItemAt(i).toString());
            ret = getOCRforMatching();
            if (ret.length() == 17 && ret.startsWith("000")) {
                txtRecognization.setForeground(Color.BLACK);
                getContentOCR().append(strpath);
                getContentOCR().append(";");
                getContentOCR().append(ret);
                getContentOCR().append("\n");
                txtRecognization.setText(ret);
                cbxType.setSelectedIndex(i);
                if (arrcn[3].equals("savedb") && chkAuto.isSelected()) {
                    DataHandler.Instance(arrcn[0], arrcn[1], arrcn[2]).updOrinstData(focr.getAbsolutePath().substring(2), ret);
                }
                return;
            }
        }
        txtRecognization.setForeground(Color.RED);
        txtRecognization.setText(ret);
        getFailcontentOCR().append(strpath);
        getFailcontentOCR().append(";");
        getFailcontentOCR().append(ret);
        getFailcontentOCR().append("\n");
    }

    private void writeResult() {
        if (lstImages.getSelectedIndex() == (getDlm().getSize() - 1) && chkAuto.isSelected()) {
            File focr = new File(lstImages.getSelectedValue().toString());
            Configuration.Instance().writeFailResult(focr.getParentFile().getParentFile().getName(), getFailcontentOCR().toString());
            failcontentOCR = null;
            Configuration.Instance().writeCorrectResult(focr.getParentFile().getParentFile().getName(), getContentOCR().toString());
            contentOCR = null;
            unlockControl(true);
            JOptionPane.showMessageDialog(this, "Recognition is completed!");
        }
    }

    private String getOCRforMatching() {
        String str = jaiRecognitionctr.RecognizeICRImage().get(0).replace("\n", "A");
        char[] carr = str.toCharArray();
        for (char c : carr) {
            if (!Character.isLetterOrDigit(c) && c != 'A') {
                str = str.replace(String.valueOf(c), "");
            }
        }
        String arr[] = str.split("A");
        for (String temp : arr) {
            if (temp.length() >= 17) {
                str = temp.replace(" ", "");
                break;
            }
        }
        str = str.replace("A", "");
        return str;
        /*String str = jaiRecognitionctr.RecognizeICRImage().get(0);
        System.out.println(str);
        char[] carr = str.toLowerCase().toCharArray();
        StringBuffer strBuff = new StringBuffer();
        for (char c : carr) {
        if (Character.isDigit(c)) {
        strBuff.append(c);
        }
        }
        String arr[] = strBuff.toString().split("\n");
        for (String temp : arr) {
        if (temp.length() >= 17) {
        str = temp;
        break;
        }
        }
        return str;*/
    }

    private void setImage(String filename) {
        PlanarImage image = null;
        this.repaint();
        try {
            try {
                image = getTiff().readImage(filename, 0, 100, 0);
                RandomIterFactory.create(image, null);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            jaiRecognitionctr.img = new ImageIcon(image.getAsBufferedImage()).getImage();
            jaiRecognitionctr.set(image);
            jaiRecognitionctr.repaint();
        } catch (Exception ex) {
        }
    }

    private void unlockControl(Boolean lock) {
        cmdLoad.setEnabled(lock);
        cmdRecognize.setEnabled(lock);
        cmdShow.setEnabled(lock);
        lstImages.setEnabled(lock);
        chkAuto.setSelected(!lock);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cmdLoad = new javax.swing.JButton();
        chkAuto = new javax.swing.JCheckBox();
        cmdRecognize = new javax.swing.JButton();
        jspText = new javax.swing.JScrollPane();
        txtRecognization = new javax.swing.JTextArea();
        jspImageRecognition = new javax.swing.JScrollPane();
        jaiRecognitionctr = new AutoRecognization.jaiRecognition();
        jspImages = new javax.swing.JScrollPane();
        lstImages = new javax.swing.JList();
        cbxType = new javax.swing.JComboBox();
        cmdShow = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Auto Recognition For Douglas Card");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cmdLoad.setText("Load");
        cmdLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLoadActionPerformed(evt);
            }
        });

        chkAuto.setText("Auto");

        cmdRecognize.setText("Recognize");
        cmdRecognize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRecognizeActionPerformed(evt);
            }
        });

        txtRecognization.setColumns(20);
        txtRecognization.setFont(new java.awt.Font("Arial", 1, 18));
        txtRecognization.setLineWrap(true);
        txtRecognization.setRows(5);
        jspText.setViewportView(txtRecognization);

        javax.swing.GroupLayout jaiRecognitionctrLayout = new javax.swing.GroupLayout(jaiRecognitionctr);
        jaiRecognitionctr.setLayout(jaiRecognitionctrLayout);
        jaiRecognitionctrLayout.setHorizontalGroup(
            jaiRecognitionctrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 917, Short.MAX_VALUE)
        );
        jaiRecognitionctrLayout.setVerticalGroup(
            jaiRecognitionctrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 518, Short.MAX_VALUE)
        );

        jspImageRecognition.setViewportView(jaiRecognitionctr);

        lstImages.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstImagesValueChanged(evt);
            }
        });
        jspImages.setViewportView(lstImages);

        cbxType.setEnabled(false);

        cmdShow.setText("Show result");
        cmdShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdShowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jspImageRecognition, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 936, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmdShow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmdLoad, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                            .addComponent(cmdRecognize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jspText, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jspImages, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxType, 0, 224, Short.MAX_VALUE)
                            .addComponent(chkAuto, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jspImages, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jspText, 0, 92, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cmdLoad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdRecognize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdShow))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkAuto)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jspImageRecognition, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLoadActionPerformed
        ChooseFileImage();
        cmdRecognize.setEnabled(true);
}//GEN-LAST:event_cmdLoadActionPerformed

    private void cmdRecognizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRecognizeActionPerformed
        timer.start();
        unlockControl(false);
}//GEN-LAST:event_cmdRecognizeActionPerformed

    private void lstImagesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstImagesValueChanged
        if (lstImages.getSelectedValue() != null) {
            setImage(lstImages.getSelectedValue().toString());
            timer.start();
        }
}//GEN-LAST:event_lstImagesValueChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        DataHandler.Instance(arrcn[0], arrcn[1], arrcn[2]).closeConnection();
    }//GEN-LAST:event_formWindowClosing

    private void cmdShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdShowActionPerformed
        if (dlm.getSize() > 0) {
            String path = dlm.get(0).toString().substring(2);
            File f = new File(path);
            path = f.getParent().replace("\\", "/") + "/";
            new ShowDataOCR(this, true, arrcn, path).setVisible(true);
        }
    }//GEN-LAST:event_cmdShowActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbxType;
    private javax.swing.JCheckBox chkAuto;
    private javax.swing.JButton cmdLoad;
    private javax.swing.JButton cmdRecognize;
    private javax.swing.JButton cmdShow;
    private javax.swing.JPanel jPanel1;
    private AutoRecognization.jaiRecognition jaiRecognitionctr;
    private javax.swing.JScrollPane jspImageRecognition;
    private javax.swing.JScrollPane jspImages;
    private javax.swing.JScrollPane jspText;
    private javax.swing.JList lstImages;
    private javax.swing.JTextArea txtRecognization;
    // End of variables declaration//GEN-END:variables
}
