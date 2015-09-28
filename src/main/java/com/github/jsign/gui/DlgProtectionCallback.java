/**
 * 
 */
package com.github.jsign.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
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

public class DlgProtectionCallback implements CallbackHandler {
	
	private String title = "Insira o PIN:";	
	private JPasswordField passField = new JPasswordField();
	private PasswordCallback passwordCallback;
	private String keyStoreType;
	private String description;
				
	public DlgProtectionCallback(String keyStoreType, String description) {
		this.keyStoreType = keyStoreType;
		this.description = description;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		
		for(Callback cb: callbacks) {
			
			if(cb instanceof PasswordCallback) {
				
				this.passwordCallback = (PasswordCallback) cb;
				JLabel label1 = new JLabel("Tipo: " + this.keyStoreType);
				JLabel label2 = new JLabel("Descrição: " + this.description);
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
				
				try {
					dialog.setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/key.png")));
				}
				catch (Exception e) {
					
				}
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				
				int result = (Integer)jop.getValue();
				dialog.dispose();
				if(result == JOptionPane.OK_OPTION) {
					this.passwordCallback.setPassword(passField.getPassword());
				}
				else {
					this.passwordCallback.clearPassword();					
					throw new IOException(new LoginCancelledException());
				}
			}
		}
	}	

	public char[] getPassword() {
		return passField.getPassword();
	}

	public PasswordCallback getPasswordCallback() {
		return passwordCallback;
	}
}
