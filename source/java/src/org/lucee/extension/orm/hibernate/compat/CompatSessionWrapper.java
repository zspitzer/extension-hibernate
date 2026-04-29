package org.lucee.extension.orm.hibernate.compat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Wraps a Hibernate 7 {@link Session} in a compat proxy that re-exposes the
 * H5.6-era Session methods removed in H6/H7. See {@code h73-session-shim-spec.md}
 * for the empirical surface (73 methods removed; 32 across 9 families are
 * CFML-relevant).
 *
 * <p>Mirrors {@link CompatSessionFactoryWrapper}'s shape: a {@link Proxy}
 * implementing both {@link CompatSession} (legacy method names) and
 * {@link SessionImplementor} (modern SPI). CFML reflection walks declared
 * methods, so anything not on a declared interface is invisible.
 *
 * <p>Returns the input unchanged if it's null or already a {@link CompatSession}
 * — safe to call on any path.
 *
 * <p>Batch 1 status: pure-delegate proxy. Subsequent batches add per-family
 * routing branches in the {@link Handler} below.
 */
public final class CompatSessionWrapper {

	private CompatSessionWrapper() {}

	public static Session wrap(Session real) {
		if (real == null || real instanceof CompatSession) return real;
		return (Session) Proxy.newProxyInstance(
				CompatSession.class.getClassLoader(),
				new Class<?>[] { CompatSession.class, SessionImplementor.class },
				new Handler(real));
	}

	/**
	 * Returns the underlying H7 Session if the input was produced by {@link #wrap},
	 * otherwise returns the input unchanged. Symmetric counterpart to {@link #wrap}.
	 *
	 * <p>CFML callers should usually use the JPA-standard {@code session.unwrap(Class)}
	 * instead — that works through the proxy delegation. This helper is for Java-side
	 * code (tests, future internal consumers) that has a {@link Session} reference and
	 * wants the raw delegate without going through reflection.
	 */
	public static Session unwrap(Session sess) {
		if (sess == null || !Proxy.isProxyClass(sess.getClass())) return sess;
		InvocationHandler h = Proxy.getInvocationHandler(sess);
		if (h instanceof Handler) return ((Handler) h).delegate;
		return sess;
	}

	private static final class Handler implements InvocationHandler {

		private final Session delegate;

		Handler(Session delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Batch 2/3 routing branches insert here.
			//
			// Pure delegate for now — every call goes to the real Session.
			return method.invoke(delegate, args);
		}
	}
}
