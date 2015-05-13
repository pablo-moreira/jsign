package com.github.jsign.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class JFrameUtils {

	static class EventPump implements InvocationHandler {

		Frame frame;

		public EventPump(Frame frame) {
			this.frame = frame;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return frame.isShowing() ? Boolean.TRUE : Boolean.FALSE;
		}

		// when the reflection calls in this method has to be
		// replaced once Sun provides a public API to pump events.
		@SuppressWarnings("rawtypes")
		public void start() throws Exception {
			Class clazz = Class.forName("java.awt.Conditional");
			Object conditional = Proxy.newProxyInstance(clazz.getClassLoader(),	new Class[] { clazz }, this);
			Method pumpMethod = Class.forName("java.awt.EventDispatchThread").getDeclaredMethod("pumpEvents", new Class[] { clazz });
			pumpMethod.setAccessible(true);
			pumpMethod.invoke(Thread.currentThread(), new Object[] { conditional });
		}
	}

	public static void setCenterLocation(Component component) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setLocation(dim.width / 2 - component.getWidth() / 2, dim.height / 2 - component.getHeight() / 2);
	}

	// show the given frame as modal to the specified owner.
	// NOTE: this method returns only after the modal frame is closed.
	public static void showAsModal(final Frame frame, final Frame owner) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				owner.setEnabled(true);
				frame.removeWindowListener(this);
			}

			public void windowOpened(WindowEvent e) {
				owner.setEnabled(false);
			}
		});

		owner.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				if (frame.isShowing()) {
					frame.setExtendedState(JFrame.NORMAL);
					frame.toFront();
				} else
					owner.removeWindowListener(this);
			}
		});

		frame.setVisible(true);
		try {
			new EventPump(frame).start();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}
	
	public static void showAlerta(String titulo, String msg, Component parent) {
		showMsg(titulo, msg, parent, JOptionPane.WARNING_MESSAGE);
	}

	public static void showErro(String titulo, String msg, Component parent) {
		showMsg(titulo, msg, parent, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showInfo(String titulo, String msg, Component parent) {
		showMsg(titulo, msg, parent, JOptionPane.INFORMATION_MESSAGE);
	}
	
	private static void showMsg(String titulo, String msg, Component parent, int tipo) {
		JOptionPane.showMessageDialog(
				parent,
				msg,
				titulo,
				tipo);
	}

	@SuppressWarnings("deprecation")
	public static String showInputPasswordDialog(String titulo, String msg) {

		JPasswordField txtPassword = new JPasswordField(10);
		txtPassword.setEchoChar('*');

		JLabel lblMsg = new JLabel(msg);
		
		JPanel panel = new JPanel(new GridLayout(2,1));
		
		panel.add(lblMsg);
		panel.add(txtPassword);
		
		JOptionPane.showMessageDialog(null, panel, titulo, JOptionPane.PLAIN_MESSAGE);

		return txtPassword.getText();
	}

	public static void showErro(String titulo, String msg) {
		showErro(titulo, msg, null);		
	}
}