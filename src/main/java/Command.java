/**
 * @author taichi
 */
public interface Command<E extends Exception> {

	void execute() throws E;
}
