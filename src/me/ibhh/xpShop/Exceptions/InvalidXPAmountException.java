package me.ibhh.xpShop.Exceptions;

public class InvalidXPAmountException extends Exception {

	private static final long	serialVersionUID	= 1L;

	public InvalidXPAmountException(int amount) {
		super("invalid XP amount: " + amount);
	}

}
