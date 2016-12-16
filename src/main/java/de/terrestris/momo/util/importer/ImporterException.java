package de.terrestris.momo.util.importer;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 */
public class ImporterException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public ImporterException() {}

	/**
	 * @param message
	 */
	public ImporterException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ImporterException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ImporterException(String message, Throwable cause) {
		super(message, cause);
	}

}
