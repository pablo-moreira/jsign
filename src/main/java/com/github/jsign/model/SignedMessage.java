package com.github.jsign.model;


public class SignedMessage {

	private MessageToSign messageToSign;
	private byte[] signedMessage;

	public SignedMessage(MessageToSign messageToSign, byte[] signedMessage) {
		this.messageToSign = messageToSign;
		this.signedMessage = signedMessage;
	}
	
	public MessageToSign getMessageToSign() {
		return messageToSign;
	}
	
	public byte[] getSignedMessage() {
		return signedMessage;
	}
}
