package de.berg.systeme.jenkins.wix;

public class ToolsetException extends Throwable {
	private static final long serialVersionUID = 7128147634196518698L;

	public ToolsetException(String message) {
		super(message);
	}

	public ToolsetException(Throwable t) {
		super(t);
	}
}
