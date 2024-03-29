/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JScriptEditor.java
 *
 * Created on Nov 13, 2009, 1:27:55 PM
 */
package JScriptIDE;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author ddda
 */
public class JScriptEditor extends javax.swing.JPanel {

    private LineNr linenr = new LineNr();
    private SyntaxError syne = new SyntaxError();

    /** Creates new form JScriptEditor */
    public JScriptEditor() {
        initComponents();
        pnlSource.add(linenr, BorderLayout.WEST);
        pnlSource.add(linenr.scrollPane, BorderLayout.CENTER);
    }

    /**
     * Get source code java script
     * @return Source code
     */
    public String getSource() {
        linenr.txtSource.selectAll();
        return linenr.txtSource.getSelectedText();
    }

    /**
     * Go to line of code
     * @param line
     */
    public void gotoLine(int line) {
        linenr.gotoLine(line);
    }

    /**
     * Set tab space for source code
     * @param tab sise, examples: 4 or 8
     */
    public void setTabSize(int tab) {
        linenr.setTabs(tab);
    }

    /**
     * compile source code java script
     * @return SyntaxError Object which contains error lines and error messages
     */
    public SyntaxError compileSource() {
        syne.compileCode(getSource());
        linenr.setLine_error(syne.getLine_number_error());
        linenr.repaint();
        if (syne.getLine_number_error() > 0) {
            linenr.gotoLine(syne.getLine_number_error());
            return syne;
        }
        return null;
    }

    /**
     * Save source code into disk
     * @param path - destination of source code where you wan to store source code.
     */
    public void saveSource(String path) {
        try {
            Writer output = null;
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            output = new BufferedWriter(new FileWriter(file));
            output.write(getSource());
            output.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Search String in Source code
     * @param value - a string which you want to find
     * @param isCaseInsensitive
     */
    public void findString(String value, Boolean isCaseInsensitive) {
        linenr.txtSource.requestFocus();
        SyntaxMonitor.Instance().findString(value, linenr.txtSource, isCaseInsensitive, 0);
    }

    /**
     * Find next.
     */
    public void findNext() {
        linenr.txtSource.requestFocus();
        SyntaxMonitor.Instance().findNext(linenr.txtSource);
    }

    /**
     * replace string by another string.
     * @param findwhat
     * @param replacewith
     * @param isReplaceAll - if value is true, then the function will replace all, otherwise it just replace the first once.
     */
    public void replaceAll(String findwhat, String replacewith, Boolean isReplaceAll) {
        linenr.txtSource.requestFocus();
        SyntaxMonitor.Instance().replaceAll(findwhat, replacewith, linenr.txtSource, isReplaceAll);
    }

    public void addLineClickListener(LineClickListener listener) {
        linenr.listenersList.add(LineClickListener.class, listener);
    }

    public void removeLineClickListener(LineClickListener listener) {
        linenr.listenersList.remove(LineClickListener.class, listener);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSource = new javax.swing.JPanel();

        pnlSource.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlSource.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSource, javax.swing.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSource, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlSource;
    // End of variables declaration//GEN-END:variables
}
