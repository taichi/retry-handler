import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Test;

/**
 * @author yoshiori
 * @author taichi
 */
public class ProcTest {

	@Test
	public void 処理が普通に行える() throws Throwable {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3, new Command<Exception>() {

			@Override
			public void execute() {
				list.add("hoge");
			}
		});
		assertThat(true, is(task.get()));
		assertThat(list.size(), is(1));
		assertThat(list.get(0), is("hoge"));
	}

	@Test
	public void エラーが発生しても処理が普通に行える_非チェック例外() throws Throwable {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3,
				new Command<RuntimeException>() {
					int i = 0;

					@Override
					public void execute() {
						if (this.i < 2) {
							this.i++;
							throw new RuntimeException();
						}
						list.add("hoge");
					}
				});
		assertThat(true, is(task.get()));
		assertThat(list.size(), is(1));
		assertThat(list.get(0), is("hoge"));
	}

	@Test
	public void エラーが発生しても処理が普通に行える_チェック例外() throws Throwable {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3, new Command<IOException>() {
			int i = 0;

			@Override
			public void execute() throws IOException {
				if (this.i < 2) {
					this.i++;
					throw new IOException();
				}
				list.add("hoge");
			}
		});
		assertThat(true, is(task.get()));
		assertThat(list.size(), is(1));
		assertThat(list.get(0), is("hoge"));
	}

	@Test
	public void エラーを指定してそれが発生しても処理が普通に行える_チェック例外() throws Throwable {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3, new Command<IOException>() {
			int i = 0;

			@Override
			public void execute() throws IOException {
				if (this.i < 2) {
					this.i++;
					throw new IOException();
				}
				list.add("hoge");
			}
		});
		assertThat(true, is(task.get()));
		assertThat(list.size(), is(1));
		assertThat(list.get(0), is("hoge"));
	}

	@Test
	public void 指定したエラー以外の時は処理が止まる() throws Throwable {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3, new Command<IOException>() {
			int i = 0;

			@Override
			public void execute() throws IOException {
				if (this.i < 2) {
					this.i++;
					throw new RuntimeException();
				}
				list.add("hoge");
			}
		});
		try {
			assertThat(true, is(task.get()));
		} catch (ExecutionException e) {
			assertEquals(RuntimeException.class, e.getCause().getClass());
		}

	}

	@Test
	public void 指定した時間waitを置いて処理を実行する() throws Throwable {
		final List<String> list = new ArrayList<String>();
		long startTime = System.currentTimeMillis();
		FutureTask<Boolean> task = Proc.retry(3, new Command<IOException>() {
			int i = 0;

			@Override
			public void execute() throws IOException {
				if (this.i < 2) {
					this.i++;
					throw new IOException();
				}
				list.add("hoge");
			}
		}, 5 * 1000);
		assertThat(true, is(task.get()));
		long endTime = System.currentTimeMillis();
		assertThat(endTime - startTime, greaterThanOrEqualTo(10 * 1000L));
	}

	@Test
	public void 指定した時間waitを置いて処理を実行する_キャッチする例外を指定() throws Throwable {
		final List<String> list = new ArrayList<String>();
		long startTime = System.currentTimeMillis();
		FutureTask<Boolean> task = Proc.retry(3, new Command<IOException>() {
			int i = 0;

			@Override
			public void execute() throws IOException {
				if (this.i < 2) {
					this.i++;
					throw new IOException();
				}
				list.add("hoge");
			}
		}, 5 * 1000);
		assertThat(true, is(task.get()));
		long endTime = System.currentTimeMillis();
		assertThat(endTime - startTime, greaterThanOrEqualTo(10 * 1000L));
	}

	@Test
	public void 指定した回数以内で正常終了出来なかったらRetryException() throws Exception {
		FutureTask<Boolean> task = Proc.retry(2, new Command<IOException>() {
			@Override
			public void execute() throws IOException {
				throw new IOException();
			}
		}, 20);
		try {
			assertThat(true, is(task.get()));
			fail();
		} catch (ExecutionException e) {
			assertEquals(RetryException.class, e.getCause().getClass());
		}
	}

	@Test
	public void 強制終了() throws Exception {
		final List<String> list = new ArrayList<String>();
		FutureTask<Boolean> task = Proc.retry(3, new Command<Exception>() {
			@Override
			public void execute() throws Exception {
				Thread.sleep(20);
				list.add("hoge");
			}
		});
		task.cancel(true);
		assertThat(0, is(list.size()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void timesに1以下を指定していたらIllegalArgumentExceptionを発生させる()
			throws Throwable {
		Proc.retry(1, new Command<IOException>() {
			@Override
			public void execute() throws IOException {
			}
		}, 1000);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void sleeptimeにマイナスを指定していたらIllegalArgumentExceptionを発生させる()
			throws Throwable {
		Proc.retry(3, new Command<IOException>() {
			@Override
			public void execute() throws IOException {
			}
		}, -1L);
		fail();
	}
}