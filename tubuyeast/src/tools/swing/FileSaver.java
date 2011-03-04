package tools.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
public class FileSaver extends JPanel
                             implements ActionListener {
    static private final String newline = "\n";
    private JButton saveButton;
    private JTextArea log;
    private JFileChooser fc;
    private ActionListener saveListener = null;
    private File currentFile;
    private JTextPane ntext = new JTextPane();
//    private JTextArea ntext = new JTextArea();
    private JLabel lblText = new JLabel();

    public FileSaver() {
    	this("");
    }
    
    public FileSaver(String directory) {
        this(directory, "");
    }    
    
    public FileSaver(String directory, String text) {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);

        //Create a file chooser
        fc = new JFileChooser(directory);

        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.
        URL urlOpen  = getClass().getResource("resources/Open16.gif");
        URL urlSave = getClass().getResource("resources/Save16.gif");
        ImageIcon openIcon = new ImageIcon(urlOpen);
        ImageIcon saveIcon = new ImageIcon(urlSave);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Save a File...", saveIcon);
        saveButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout

        ntext.setText("");
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        int size = 200;
        ntext.setPreferredSize(new Dimension(size, 20));
        ntext.setParagraphAttributes(attribs, true);  
        ntext.setBorder(BorderFactory.createEtchedBorder());
//        ntext.setPreferredSize(new Dimension(200, 15));
//        ntext.setLineWrap(true);
//        ntext.setWrapStyleWord(true); 
        
        lblText.setText(text);
        
        // Add controls
        buttonPanel.add(lblText);
        buttonPanel.add(ntext);
        buttonPanel.add(saveButton);
        
        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
    }

    public void addSaveActionListener(ActionListener l) {
        saveListener = l;
    }

    public File getCurrentFile() {
        return currentFile;
    }
    
    public void actionPerformed(ActionEvent e) {

    	if (e.getSource() == saveButton) {
            int returnVal = fc.showSaveDialog(FileSaver.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Saving: " + currentFile.getName() + "." + newline);

                if (currentFile != null) {
                    setPath("../" + currentFile.getParentFile().getName() + "/" + currentFile.getName());
                }

                if (saveListener != null) {
                    saveListener.actionPerformed(e);
                }

            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    /**
     * @param text
     */
    public void setPath(String text) {
        ntext.setText(text);
    }

    /**
     * @param text
     */
    public void setLabel(String label) {
        lblText.setText(label);
    }

    @Override
    public String toString() {
        return log.getText();
    }
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new FileSaver("", "Some text"));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }

    /**
     * @return the current path
     */
	public String getPath() {
		return currentFile.getAbsolutePath();
	}
    
}
