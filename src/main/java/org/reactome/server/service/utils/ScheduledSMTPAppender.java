package org.reactome.server.service.utils;

import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class ScheduledSMTPAppender extends SMTPAppender {

    private final ThreadFactory tf = r -> {
        Thread t = new Thread(r, "ScheduledSMTPAppender Thread");
        t.setDaemon(true);
        return t;
    };

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, tf);
    private final List<ILoggingEvent> events = new ArrayList<>();

    private int maxMessages = 50;
    private int timeInterval = 1;

    public ScheduledSMTPAppender() {
        super();
    }

    public ScheduledSMTPAppender(EventEvaluator<ILoggingEvent> eventEvaluator) {
        super(eventEvaluator);
    }

    @Override
    public void start() {
        super.start();
        scheduler.scheduleAtFixedRate(this::sendEmail, timeInterval, timeInterval, TimeUnit.DAYS);
    }

    @Override
    protected void sendBuffer(CyclicBuffer<ILoggingEvent> cb, ILoggingEvent lastEventObject) {
        events.addAll(cb.asList());
        if (events.size() > maxMessages) sendEmail();
    }

    private synchronized void sendEmail() {
        try {
            if (events.isEmpty()) return;
            ILoggingEvent lastEvent = events.get(events.size() - 1);
            CyclicBuffer<ILoggingEvent> cb = new CyclicBuffer<>(events.size());
            for (ILoggingEvent e : events) cb.add(e);
            super.sendBuffer(cb, lastEvent);
            events.clear();
        } catch (Exception e) {
            addError("Error occurred while sending e-mail notification.", e);
        }
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    @Override
    public synchronized void stop() {
        scheduler.shutdown();
        super.stop();
    }
}