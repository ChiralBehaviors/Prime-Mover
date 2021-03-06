/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This file is part of the Prime Mover Event Driven Simulation Framework.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.primeMover.runtime;

import java.io.PrintStream;
import java.io.Serializable;

import com.hellblazer.primeMover.Event;

/**
 * Represents the simulated event
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class EventImpl implements Cloneable, Serializable,
        Comparable<EventImpl>, Event {
    private static final long         serialVersionUID = -628833433139964756L;
    /**
     * The arguments for the event
     */
    private Object[]                  arguments;
    /**
     * The continuation state of the event
     */
    private Continuation              continuation;
    /**
     * The event
     */
    private final int                 event;
    /**
     * The entity which is the target of the event
     */
    private transient EntityReference reference;
    /**
     * The event that was the source of this event
     */
    private final Event               source;

    /**
     * The instant in time when this event was raised
     */
    private long                      time;

    private final String              debugInfo;

    EventImpl(long time, Event sourceEvent, EntityReference reference,
              int ordinal, Object... arguments) {
        this(null, time, sourceEvent, reference, ordinal, arguments);
    }

    EventImpl(String debugInfo, long time, Event sourceEvent,
              EntityReference reference, int ordinal, Object... arguments) {
        assert reference != null;
        this.debugInfo = debugInfo;
        this.time = time;
        this.reference = reference;
        event = ordinal;
        this.arguments = arguments;
        source = sourceEvent;
    }

    public EventImpl clone(long time) {
        EventImpl clone;
        try {
            clone = (EventImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone not supported for Event!", e);
        }

        clone.continuation = null;
        clone.time = time;

        return clone;
    }

    @Override
    public int compareTo(EventImpl event) {
        // cannot do (thisMillis - otherMillis) as can overflow
        if (time == event.time) {
            return 0;
        }
        if (time < event.time) {
            return -1;
        } else {
            return 1;
        }
    }

    Continuation getContinuation() {
        return continuation;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.runtime.Event#getSignature()
     */
    @Override
    public String getSignature() {
        return reference.__signatureFor(event);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.runtime.Event#getSource()
     */
    @Override
    public Event getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.runtime.Event#getTime()
     */
    @Override
    public long getTime() {
        return time;
    }

    Object invoke() throws Throwable {
        return reference.__invoke(event, arguments);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.runtime.Event#printTrace()
     */
    @Override
    public void printTrace() {
        printTrace(System.err);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.primeMover.runtime.Event#printTrace(java.io.PrintStream)
     */
    @Override
    public void printTrace(PrintStream s) {
        synchronized (s) {
            s.println(this);
            Event eventSource = source;
            while (eventSource != null) {
                s.println("\tat " + eventSource);
                eventSource = eventSource.getSource();
            }
        }
    }

    EventImpl resume(long currentTime, Object result, Throwable exception) {
        time = currentTime;
        continuation.setReturnState(result, exception);
        return this;
    }

    void setContinuation(Continuation continuation) {
        this.continuation = continuation;
    }

    void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        if (debugInfo == null) {
            return String.format("%s : %s", time, getSignature());
        } else {
            return String.format("%s : %s @ %s", time, getSignature(),
                                 debugInfo);
        }
    }
}
