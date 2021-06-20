package proyectofinalacr.utils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

public class OutputTextPane extends OutputStream {
    private JTextPane text;

    public OutputTextPane(JTextPane text) {
        this.text = text;
    }

    @Override
    public void write(int b) throws IOException {
        String value=String.valueOf((char)b);
        // redirects data to the text area
        appendToPane(text,value,Out.ColorToPrint);
        
        // scrolls the text area to the end of data
        text.setCaretPosition(text.getDocument().getLength());
        // keeps the textArea up to date
        text.revalidate();
        text.repaint();
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Raleway");
        aset = sc.addAttribute(aset, StyleConstants.FontSize, 16);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }
}