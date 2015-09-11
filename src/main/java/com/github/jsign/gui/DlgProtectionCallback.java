/**
 * 
 */
package com.github.jsign.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import com.github.jsign.exceptions.LoginCancelledException;
import com.github.jsign.model.AvailableProvider;

public class DlgProtectionCallback implements CallbackHandler {
	
	private String title = "Insira o PIN:";
	private AvailableProvider availableProvider;
	private JPasswordField passField = new JPasswordField();
				
	public DlgProtectionCallback(AvailableProvider availableProvider) {
		this.availableProvider = availableProvider;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		
		for(Callback cb: callbacks) {
			
			if(cb instanceof PasswordCallback) {
				
				final PasswordCallback pc = (PasswordCallback) cb;
				JLabel label1 = new JLabel("Tipo: " + getAvailableProvider().getType().name());
				JLabel label2 = new JLabel("Descrição: " + getAvailableProvider().getDescription());
				JLabel label3 = new JLabel("");
				JLabel label4 = new JLabel(title);				
				JOptionPane jop = new JOptionPane(new Object[]{ label1, label2, label3, label4, passField }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
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
				
				passField.setText("");
				
				dialog.setAlwaysOnTop(true);
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
		
	public AvailableProvider getAvailableProvider() {
		return availableProvider;
	}

	public char[] getPassword() {
		return passField.getPassword();
	}
}
