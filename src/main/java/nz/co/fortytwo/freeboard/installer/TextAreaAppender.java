/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.fortytwo.freeboard.installer;

import javax.swing.JTextArea;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public /*static*/ class TextAreaAppender extends AppenderSkeleton {
    static JTextArea textArea;

	 public TextAreaAppender(JTextArea ta){
		 super();
		 textArea = ta;
	 }

    @Override
    protected void append(LoggingEvent event) {
        textArea.append(layout.format(event));
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return true;
    }

}
