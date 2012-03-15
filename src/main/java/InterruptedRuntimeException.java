/**
 * @author taichi
 */
public class InterruptedRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3305329334892683857L;

	public InterruptedRuntimeException(InterruptedException cause) {
		super(cause);
	}

}
