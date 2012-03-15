import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
		final ConcurrentLinkedQueue<Future<Void>> list = new ConcurrentLinkedQueue<>();
		final AtomicInteger counter = new AtomicInteger();
		final Callable<Void>[] r = new Callable[1];
		r[0] = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				if (counter.incrementAndGet() <= times) {
					try {
						command.execute();
					} catch (Throwable t) {
						// more error handling ?
						if (type.isAssignableFrom(t.getClass())) {
							list.add(scheduler.schedule(r[0], wait,
									TimeUnit.MILLISECONDS));
						} else {
							throw (E) t;
						}
					}
				}
				return null;
			}
		};
		list.add(scheduler.schedule(r[0], 0, TimeUnit.MILLISECONDS));
		try {
			while (0 < list.size()) {
				Future<Void> f = list.poll();
				f.get();
			}
		} catch (InterruptedException ex) {
		} catch (ExecutionException ex) {
			throw (E) ex.getCause();
		}
	}
}
