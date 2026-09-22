package com.example.apigateway.web;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

/**
 * Bridges the correlation ID stored in Reactor's {@link Context} (by
 * {@link CorrelationIdWebFilter}) into SLF4J's thread-local MDC around every
 * signal delivery, so log statements anywhere downstream see the right
 * correlation ID regardless of which thread Reactor happens to be running on
 * at that moment - and so the MDC value is restored to whatever it was
 * before, once the signal has been handled, leaving no leakage on shared
 * scheduler threads.
 *
 * <p>This replaces the automatic Reactor-Context-to-ThreadLocal propagation
 * Micrometer's {@code context-propagation} library provides from Spring Boot
 * 3.2 onward (via {@code Hooks.enableAutomaticContextPropagation()}) - that
 * mechanism needs reactor-core 3.5.3+, which isn't available on the
 * Boot-2.7/Cloud-2021.0.9 stack this service targets. This is the
 * long-standing manual pattern reactor-core has supported since 3.1.
 *
 * <p>The {@link Subscription} passed to {@code onSubscribe} is wrapped only
 * to intercept {@code cancel()} (so a cancellation signal also runs with the
 * right MDC entry set). The wrapper conditionally implements
 * {@link Fuseable.QueueSubscription} when the delegate does - several of
 * Reactor's built-in operators cast the subscription to that type for
 * macro/micro-fusion, and a plain wrapper breaks that cast
 * (ClassCastException) for every reactive pipeline in the process, not just
 * this one.
 */
public final class MdcContextLifter<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> delegate;

    private MdcContextLifter(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    public static void register() {
        Hooks.onEachOperator(
                MdcContextLifter.class.getName(),
                Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSubscribe(Subscription subscription) {
        Subscription wrapped = subscription instanceof Fuseable.QueueSubscription
                ? new MdcQueueSubscription((Fuseable.QueueSubscription<T>) subscription)
                : new MdcSubscription(subscription);
        withMdc(() -> delegate.onSubscribe(wrapped));
    }

    @Override
    public void onNext(T t) {
        withMdc(() -> delegate.onNext(t));
    }

    @Override
    public void onError(Throwable throwable) {
        withMdc(() -> delegate.onError(throwable));
    }

    @Override
    public void onComplete() {
        withMdc(delegate::onComplete);
    }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    private void withMdc(Runnable signal) {
        String previous = MDC.get(CorrelationIdWebFilter.MDC_KEY);
        String contextValue = currentContext().getOrDefault(CorrelationIdWebFilter.CONTEXT_KEY, null);
        setMdc(contextValue);
        try {
            signal.run();
        } finally {
            setMdc(previous);
        }
    }

    private static void setMdc(String value) {
        if (value != null) {
            MDC.put(CorrelationIdWebFilter.MDC_KEY, value);
        } else {
            MDC.remove(CorrelationIdWebFilter.MDC_KEY);
        }
    }

    private class MdcSubscription implements Subscription {
        final Subscription delegateSubscription;

        MdcSubscription(Subscription delegateSubscription) {
            this.delegateSubscription = delegateSubscription;
        }

        @Override
        public void request(long n) {
            delegateSubscription.request(n);
        }

        @Override
        public void cancel() {
            withMdc(delegateSubscription::cancel);
        }
    }

    private final class MdcQueueSubscription extends MdcSubscription implements Fuseable.QueueSubscription<T> {
        private final Fuseable.QueueSubscription<T> queueDelegate;

        MdcQueueSubscription(Fuseable.QueueSubscription<T> queueDelegate) {
            super(queueDelegate);
            this.queueDelegate = queueDelegate;
        }

        @Override
        public int requestFusion(int requestedMode) {
            return queueDelegate.requestFusion(requestedMode);
        }

        @Override
        public T poll() {
            return queueDelegate.poll();
        }

        @Override
        public int size() {
            return queueDelegate.size();
        }

        @Override
        public boolean isEmpty() {
            return queueDelegate.isEmpty();
        }

        @Override
        public void clear() {
            queueDelegate.clear();
        }
    }
}
