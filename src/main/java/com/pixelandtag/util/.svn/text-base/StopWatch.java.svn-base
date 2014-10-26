package com.inmobia.util;

import java.util.concurrent.TimeUnit;

public final class StopWatch {
	private final Ticker ticker;
	private boolean isRunning;
	private long elapsedNanos;
	private long startTick;

	public StopWatch() {
		this(new Ticker());
	}

	public StopWatch(Ticker ticker) {
		this.ticker = ticker;
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public StopWatch start() {

		this.isRunning = true;
		this.startTick = this.ticker.read();
		return this;
	}

	public StopWatch stop() {
		long tick = this.ticker.read();
		this.isRunning = false;
		this.elapsedNanos += tick - this.startTick;
		return this;
	}

	public StopWatch reset() {
		this.elapsedNanos = 0L;
		this.isRunning = false;
		return this;
	}

	private long elapsedNanos() {
		return this.isRunning ? this.ticker.read() - this.startTick
				+ this.elapsedNanos : this.elapsedNanos;
	}

	public long elapsedTime(TimeUnit desiredUnit) {
		return desiredUnit.convert(elapsedNanos(), TimeUnit.NANOSECONDS);
	}

	public long elapsedMillis() {
		return elapsedTime(TimeUnit.MILLISECONDS);
	}

	public String toString() {
		return toString(4);
	}

	public String toString(int significantDigits) {
		long nanos = elapsedNanos();

		TimeUnit unit = chooseUnit(nanos);
		double value = nanos / TimeUnit.NANOSECONDS.convert(1L, unit);

		return String.format("%." + significantDigits + "g %s", new Object[] {
				Double.valueOf(value), abbreviate(unit) });
	}

	private static TimeUnit chooseUnit(long nanos) {
		if (TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
			return TimeUnit.SECONDS;
		}
		if (TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
			return TimeUnit.MILLISECONDS;
		}
		if (TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
			return TimeUnit.MICROSECONDS;
		}
		return TimeUnit.NANOSECONDS;
	}

	private static String abbreviate(TimeUnit unit) {
		switch (unit.ordinal()) {
		case 1:
			return "ns";
		case 2:
			return "micro s";
		case 3:
			return "ms";
		case 4:
			return "s";
		}
		throw new AssertionError();
	}
}

class Ticker {
	public long read() {
		return System.nanoTime();
	}

}