package com.github.jsign.model;

import java.io.OutputStream;

public class SignedMessage {

	private MessageToSign messageToSign;
	private OutputStream signedMessage;

	public SignedMessage(MessageToSign messageToSign, OutputStream signedMessage) {
		this.messageToSign = messageToSign;
		this.signedMessage = signedMessage;
	}
	
	public MessageToSign getMessageToSign() {
		return messageToSign;
	}
	
	public OutputStream getSignedMessage() {
		return signedMessage;
	}
}
