import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author taichi
 */
public class Proc {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1); // more configurable ?

	public static FutureTask<Boolean> retry(int times,
			Command<Exception> command) throws Exception {
		return retry(times, command, 0L);
	}

	@SafeVarargs
	public static <E extends Exception> FutureTask<Boolean> retry(int times,
			Command<E> command, E... e) throws Exception {
		return retry(times, command, 0L, e);
	}

	@SafeVarargs
	public static <E extends Exception> FutureTask<Boolean> retry(
			final int times, final Command<E> command, final long wait, E... e)
			throws Exception {
		if (times < 2) {
			throw new IllegalArgumentException();
		}
		if (wait < 0) {
			throw new IllegalArgumentException();
		}
		final Class<?> type = e.getClass().getComponentType();
		final Callable<Boolean> c = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				command.execute();
				return true;
			}
		};
		final FutureTask<Boolean> task = new RetryTask(scheduler, c, wait,
				times, type);
		scheduler.execute(task);

		return task;
	}

	public static List<Runnable> shutdown() {
		return scheduler.shutdownNow();
	}
}
