package me.ibhh.xpShop.send.sql;

public class XPSend {

	private String player;
	private String sender;
	private int sendedXP;
	private String message;
	private int status;
	private int id = -1;
	
	public XPSend(String player, String sender, int sendedXP, String message, int status) {
		this.setPlayer(player);
		this.setSender(sender);
		this.setSendedXP(sendedXP);
		this.setMessage(message);
		this.setStatus(status);
	}

	public String getPlayer() {
		return player;
	}

	private void setPlayer(String player) {
		this.player = player;
	}

	public String getSender() {
		return sender;
	}

	private void setSender(String sender) {
		this.sender = sender;
	}

	public int getSendedXP() {
		return sendedXP;
	}

	public void setSendedXP(int sendedXP) {
		this.sendedXP = sendedXP;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
