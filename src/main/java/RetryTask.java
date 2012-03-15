import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @see <a
 *      href="https://github.com/twitter/commons/blob/master/src/java/com/twitter/common/util/concurrent/RetryingFutureTask.java">RetryingFutureTask.java</a>
 */
public class RetryTask extends FutureTask<Boolean> {

	protected final ScheduledExecutorService executor;
	protected final long delay;
	protected final int maxRetries;
	protected final Class<?> type;
	protected int numRetries = 0;
	protected final Callable<Boolean> callable;
	protected final List<Throwable> throwables = new ArrayList<>();

	public RetryTask(ScheduledExecutorService executor,
			Callable<Boolean> callable, long delay, int maxRetries,
			Class<?> type) {
		super(callable);
		this.callable = callable;
		this.executor = executor;
		this.delay = delay;
		this.maxRetries = maxRetries;
		this.type = type;
	}

	/**
	 * Invokes a retry of this task.
	 */
	protected void retry() {
		this.executor.schedule(this, this.delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		boolean success = false;
		try {
			success = this.callable.call();
		} catch (Exception e) {
			if (this.type.isAssignableFrom(e.getClass()) == false) {
				this.setException(e);
				return;
			}
			this.throwables.add(e);
		}

		if (!success) {
			this.numRetries++;
			if (this.numRetries > this.maxRetries) {
				this.setException(new RetryException(this.throwables));
			} else {
				this.retry();
			}
		} else {
			this.set(true);
		}
	}
}
