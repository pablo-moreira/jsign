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

public class PKCS11CallbackHandler implements CallbackHandler {
	
	private String title;
	private boolean userCanceled;
		
	public PKCS11CallbackHandler(String title){
		this.title = title;
	}
		
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		
		for(Callback cb: callbacks) {
			
			if(cb instanceof PasswordCallback) {
				
				userCanceled = false;
				
				final PasswordCallback pc = (PasswordCallback) cb;
				JLabel label = new JLabel("Insira o PIN: ");
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
					userCanceled = true;
					throw new IOException("O usuário desistiu do procedimento de liberar o certificado!");
				}
			}
		}
	}

	public boolean isUserCanceled() {
		return userCanceled;
	}	
}
