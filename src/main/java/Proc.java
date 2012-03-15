import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author taichi
 */
public class Proc {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1); // more configurable ?

	public static void retry(int times, Command<Exception> command)
			throws Exception {
		retry(times, command, 0L);
	}

	@SafeVarargs
	public static <E extends Exception> void retry(int times,
			Command<E> command, E... e) throws E {
		retry(times, command, 0L, e);
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <E extends Exception> void retry(final int times,
			final Command<E> command, final long wait, E... e) throws E {
		if (times < 2) {
			throw new IllegalArgumentException();
		}
		if (wait < 0) {
			throw new IllegalArgumentException();
		}
		final Class<?> type = e.getClass().getComponentType();
		final Callable<Void> c = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				command.execute();
				return null;
			}
		};

		List<Throwable> throwables = new ArrayList<Throwable>();
		for (int i = 0; i < times; i++) {
			try {
				Future<?> f = scheduler.schedule(c, i == 0 ? 0L : wait,
						TimeUnit.MILLISECONDS);
				f.get();
				return;
			} catch (InterruptedException ex) {
				throw new InterruptedRuntimeException(ex);
			} catch (ExecutionException ex) {
				Throwable t = ex.getCause();
				throwables.add(t);
				if (type.isAssignableFrom(t.getClass()) == false) {
					throw (E) t;
				}
			}
		}
		throw new RetryException(throwables);
	}

	public static List<Runnable> shutdown() {
		return scheduler.shutdownNow();
	}
}
