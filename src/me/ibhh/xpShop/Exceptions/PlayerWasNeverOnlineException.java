package me.ibhh.xpShop.Exceptions;

import me.ibhh.xpShop.xpShop;

public class PlayerWasNeverOnlineException extends Exception {

	private static final long	serialVersionUID	= 1L;

	public PlayerWasNeverOnlineException(xpShop plugin, String player) {
		super(player + " " + plugin.config.playerwasntonline);
	}

}
