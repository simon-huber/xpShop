package me.ibhh.xpShop.Exceptions;

import me.ibhh.xpShop.xpShop;

public class PlayerNotOnlineException extends Exception {

	private static final long	serialVersionUID	= 1L;

	public PlayerNotOnlineException(xpShop plugin, String player) {
		super(player + plugin.config.playernotonline);
	}

}
