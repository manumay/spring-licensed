package info.manuelmayer.licensed.test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TestClock extends Clock {
	
	private final Clock delegate;
	
	public TestClock() {
		this(Instant.ofEpochSecond(0), ZoneId.systemDefault());
	}
	
	public TestClock(LocalDateTime datetime) {
		this(datetime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
	}
	
	public TestClock(Instant instant) {
		this(Clock.fixed(instant, ZoneId.systemDefault()));
	}
	
	public TestClock(Instant instant, ZoneId zoneId) {
		this(Clock.fixed(instant, zoneId));
	}
	
	public TestClock(Clock delegate) {
		this.delegate = delegate;
	}

	@Override
	public ZoneId getZone() {
		return delegate.getZone();
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new TestClock(delegate.withZone(zone));
	}

	@Override
	public Instant instant() {
		return delegate.instant();
	}
}
