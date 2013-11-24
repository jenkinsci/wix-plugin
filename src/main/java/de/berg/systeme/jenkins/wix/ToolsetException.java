package de.berg.systeme.jenkins.wix;

/***
 * Thrown in case of an exception while executing the {@link Toolset}.
 * 
 * @author Bjoern Berg, bjoern.berg@gmx.de
 * @version 1.0
 *
 */
public class ToolsetException extends Throwable {
	private static final long serialVersionUID = 7128147634196518698L;

	public ToolsetException(String message) {
		super(message);
	}

	public ToolsetException(Throwable t) {
		super(t);
	}
}
