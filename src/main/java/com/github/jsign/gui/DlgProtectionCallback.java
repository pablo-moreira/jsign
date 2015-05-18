/**
 * 
 */
package com.github.jsign.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import com.github.jsign.exceptions.LoginCancelledException;
import javax.security.auth.callback.CallbackHandler;

public class DlgProtectionCallback implements CallbackHandler {
	
	private String title;
		
	public DlgProtectionCallback(String title){
		this.title = title;
	}
		
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		
		for(Callback cb: callbacks) {
			
			if(cb instanceof PasswordCallback) {
				
				final PasswordCallback pc = (PasswordCallback) cb;
				JLabel label = new JLabel(title);
				final JPasswordField passField = new JPasswordField();
				JOptionPane jop = new JOptionPane(new Object[]{label, passField}, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				JDialog dialog = jop.createDialog(title);
				dialog.addComponentListener(new ComponentAdapter() {
					  @Override
					  public void componentShown(ComponentEvent e){
					    SwingUtilities.invokeLater(new Runnable(){
					      @Override
					      public void run(){
					        passField.requestFocusInWindow();
					      }
					    });
					  }
					});
				dialog.setVisible(true);
				int result = (Integer)jop.getValue();
				dialog.dispose();
				if(result == JOptionPane.OK_OPTION) {
					pc.setPassword(passField.getPassword());
				}
				else {
					pc.clearPassword();					
					throw new IOException(new LoginCancelledException());
				}
			}
		}
	}	
}
