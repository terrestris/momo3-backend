package de.terrestris.momo.exception;

/**
 *
 * @author Daniel Koch
 * @author Andrè Henn
 * @author terrestris GmbH & Co. KG
 *
 */
public class MethodDeprecatedException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public MethodDeprecatedException() {
		super();
	}

	/**
	 *
	 * @param msg
	 */
	public MethodDeprecatedException(String msg) {
		super(msg);
	}

	/**
	 *
	 * @param msg
	 * @param cause
	 */
	public MethodDeprecatedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
