/**
 * This file is part of the Kompics P2P Framework.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package se.sics.kompics.simulator;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimeZone;
import javassist.ClassPool;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.Translator;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.simulator.adaptor.ConcreteOperation;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.Operation2;
import se.sics.kompics.simulator.adaptor.Operation3;
import se.sics.kompics.simulator.adaptor.Operation4;
import se.sics.kompics.simulator.adaptor.Operation5;
import se.sics.kompics.simulator.adaptor.OperationGenerator;
import se.sics.kompics.simulator.adaptor.distributions.BigIntegerExponentialDistribution;
import se.sics.kompics.simulator.adaptor.distributions.BigIntegerNormalDistribution;
import se.sics.kompics.simulator.adaptor.distributions.BigIntegerUniformDistribution;
import se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution;
import se.sics.kompics.simulator.adaptor.distributions.Distribution;
import se.sics.kompics.simulator.adaptor.distributions.DoubleExponentialDistribution;
import se.sics.kompics.simulator.adaptor.distributions.DoubleNormalDistribution;
import se.sics.kompics.simulator.adaptor.distributions.DoubleUniformDistribution;
import se.sics.kompics.simulator.adaptor.distributions.LongExponentialDistribution;
import se.sics.kompics.simulator.adaptor.distributions.LongNormalDistribution;
import se.sics.kompics.simulator.adaptor.distributions.LongUniformDistribution;
import se.sics.kompics.simulator.events.TakeSnapshot;
import se.sics.kompics.simulator.instrumentation.CodeInterceptor;
import se.sics.kompics.simulator.instrumentation.InstrumentationHelper;
import se.sics.kompics.simulator.instrumentation.JarURLFixClassLoader;
import se.sics.kompics.simulator.stochastic.events.StochasticProcessEvent;
import se.sics.kompics.simulator.stochastic.events.StochasticProcessStartEvent;
import se.sics.kompics.simulator.stochastic.events.StochasticProcessTerminatedEvent;
import se.sics.kompics.simulator.stochastic.events.StochasticSimulationTerminatedEvent;
import se.sics.kompics.simulator.stochastic.events.StochasticSimulatorEvent;
import se.sics.kompics.simulator.stochastic.events.StochasticTakeSnapshotEvent;

/**
 * The <code>SimulationScenario</code> class.
 *
 * @author Cosmin Arad {@literal <cosmin@sics.se>}
 * @version $Id$
 */
public abstract class SimulationScenario implements Serializable {

    private static final long serialVersionUID = 5278102582431240537L;

    private static long seed = 0l;

    private static Random random = new Random(seed);

    public static void setSeed(long seed) {
        SimulationScenario.seed = seed;
        SimulationScenario.random.setSeed(SimulationScenario.seed);
    }

    public static Random getRandom() {
        return random;
    }

    private final LinkedList<StochasticProcess> processes = new LinkedList<>();
    private int processCount;
    private StochasticSimulationTerminatedEvent terminatedEvent;

    public SimulationScenario() {
        processCount = 0;
    }

    protected abstract class StochasticProcess implements Serializable {

        private static final long serialVersionUID = -6303689523381305745L;

        private boolean relativeStartTime;
        private long startTime;
        private StochasticProcessStartEvent startEvent;
        private StochasticProcessTerminatedEvent terminateEvent;
        private StochasticProcessEvent stochasticEvent;
        private Distribution<Long> interarrivalTime = null;
        protected final LinkedList<OperationGenerator> generators = new LinkedList<>();
        private final String name;
        private boolean started = false;

        protected StochasticProcess(String name) {
            this.name = name;
            processCount++;
        }

        protected StochasticProcess() {
            this("Process" + processCount);
        }

        @Deprecated
        protected final void eventInterArrivalTime(Distribution<Long> interArrivalTime) {
            this.eventInterarrivalTime(interArrivalTime);
        }

        protected final void eventInterarrivalTime(Distribution<Long> interarrivalTime) {
            this.interarrivalTime = interarrivalTime;
        }

        protected final <E extends KompicsEvent> void raise(int count, Operation<E> op) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op), count);
            generators.add(generator);
        }

        protected final <E extends KompicsEvent, P1 extends Number> void raise(int count, Operation1<E, P1> op1,
                Distribution<P1> d1) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op1, d1), count);
            generators.add(generator);
        }

        protected final <E extends KompicsEvent, P1 extends Number, P2 extends Number> void raise(int count,
                Operation2<E, P1, P2> op2, Distribution<P1> d1, Distribution<P2> d2) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op2, d1, d2), count);
            generators.add(generator);
        }

        protected final <E extends KompicsEvent, P1 extends Number, P2 extends Number, P3 extends Number> void raise(
                int count, Operation3<E, P1, P2, P3> op3, Distribution<P1> d1, Distribution<P2> d2,
                Distribution<P3> d3) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op3, d1, d2, d3), count);
            generators.add(generator);
        }

        protected final <E extends KompicsEvent, P1 extends Number, P2 extends Number, P3 extends Number, P4 extends Number, P5 extends Number> void raise(
                int count, Operation4<E, P1, P2, P3, P4> op4, Distribution<P1> d1, Distribution<P2> d2,
                Distribution<P3> d3, Distribution<P4> d4) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op4, d1, d2, d3, d4), count);
            generators.add(generator);
        }

        protected final <E extends KompicsEvent, P1 extends Number, P2 extends Number, P3 extends Number, P4 extends Number, P5 extends Number> void raise(
                int count, Operation5<E, P1, P2, P3, P4, P5> op5, Distribution<P1> d1, Distribution<P2> d2,
                Distribution<P3> d3, Distribution<P4> d4, Distribution<P5> d5) {
            if (count <= 0) {
                throw new RuntimeException("Number of raised events must be strictly positive");
            }
            OperationGenerator generator = new OperationGenerator(new ConcreteOperation<>(op5, d1, d2, d3, d4, d5),
                    count);
            generators.add(generator);
        }

        public final void start() {
            relativeStartTime = false;
            startTime = 0;
            started = true;
            terminateEvent = new StochasticProcessTerminatedEvent(0, new LinkedList<StochasticProcessStartEvent>(),
                    name);
            stochasticEvent = new StochasticProcessEvent(0, interarrivalTime, terminateEvent, generators, name);
            startEvent = new StochasticProcessStartEvent(startTime, new LinkedList<StochasticProcessStartEvent>(),
                    stochasticEvent, 0, name);

            processes.remove(this);
            processes.add(this);
        }

        public final void startAt(long time) {
            relativeStartTime = false;
            startTime = time;
            started = true;
            terminateEvent = new StochasticProcessTerminatedEvent(0, new LinkedList<StochasticProcessStartEvent>(),
                    name);
            stochasticEvent = new StochasticProcessEvent(0, interarrivalTime, terminateEvent, generators, name);
            startEvent = new StochasticProcessStartEvent(startTime, new LinkedList<StochasticProcessStartEvent>(),
                    stochasticEvent, 0, name);

            processes.remove(this);
            processes.add(this);
        }

        public final void startAtSameTimeWith(StochasticProcess process) {
            relativeStartTime = true;
            started = true;
            startTime = 0;
            terminateEvent = new StochasticProcessTerminatedEvent(0, new LinkedList<StochasticProcessStartEvent>(),
                    name);
            stochasticEvent = new StochasticProcessEvent(0, interarrivalTime, terminateEvent, generators, name);
            startEvent = new StochasticProcessStartEvent(startTime, new LinkedList<StochasticProcessStartEvent>(),
                    stochasticEvent, 0, name);
            // we hook this process' start event to the referenced process'
            // list of start events
            if (!process.started) {
                throw new RuntimeException(process.name + " not started");
            }
            process.startEvent.getStartEvents().add(startEvent);

            processes.remove(this);
            processes.add(this);
        }

        public final void startAfterStartOf(long delay, StochasticProcess process) {
            relativeStartTime = true;
            started = true;
            startTime = delay;
            terminateEvent = new StochasticProcessTerminatedEvent(0, new LinkedList<StochasticProcessStartEvent>(),
                    name);
            stochasticEvent = new StochasticProcessEvent(0, interarrivalTime, terminateEvent, generators, name);
            startEvent = new StochasticProcessStartEvent(startTime, new LinkedList<StochasticProcessStartEvent>(),
                    stochasticEvent, 0, name);
            // we hook this process' start event to the referenced process'
            // list of start events
            if (!process.started) {
                throw new RuntimeException(process.name + " not started");
            }
            process.startEvent.getStartEvents().add(startEvent);

            processes.remove(this);
            processes.add(this);
        }

        public final void startAfterTerminationOf(long delay, StochasticProcess... process) {
            relativeStartTime = true;
            started = true;
            startTime = delay;
            terminateEvent = new StochasticProcessTerminatedEvent(0, new LinkedList<StochasticProcessStartEvent>(),
                    name);
            stochasticEvent = new StochasticProcessEvent(0, interarrivalTime, terminateEvent, generators, name);
            startEvent = new StochasticProcessStartEvent(startTime, new LinkedList<StochasticProcessStartEvent>(),
                    stochasticEvent, process.length, name);
            // we hook this process' start event to the referenced process'
            // list of start events
            HashSet<StochasticProcess> procs = new HashSet<>(Arrays.asList(process));
            for (StochasticProcess stochasticProcess : procs) {
                if (!stochasticProcess.started) {
                    throw new RuntimeException(stochasticProcess.name + " not started");
                }
                stochasticProcess.terminateEvent.getStartEvents().add(startEvent);
            }

            processes.remove(this);
            processes.add(this);
        }
    }

    protected final void terminateAt(long time) {
        StochasticSimulationTerminatedEvent terminationEvent = new StochasticSimulationTerminatedEvent(time, 0, false);
        terminatedEvent = terminationEvent;
    }

    protected final void terminateAfterTerminationOf(long delay, StochasticProcess... process) {
        HashSet<StochasticProcess> procs = new HashSet<>(Arrays.asList(process));
        StochasticSimulationTerminatedEvent terminationEvent = new StochasticSimulationTerminatedEvent(delay,
                procs.size(), true);
        terminatedEvent = terminationEvent;
        for (StochasticProcess stochasticProcess : procs) {
            if (!stochasticProcess.started) {
                throw new RuntimeException(stochasticProcess.name + " not started");
            }
            stochasticProcess.terminateEvent.setTerminationEvent(terminationEvent);
        }
    }

    protected final static class Snapshot {

        private final TakeSnapshot takeSnapshotEvent;

        public Snapshot(TakeSnapshot takeSnapshotEvent) {
            this.takeSnapshotEvent = takeSnapshotEvent;
        }

        public void takeAfterTerminationOf(long delay, StochasticProcess... process) {
            HashSet<StochasticProcess> procs = new HashSet<>(Arrays.asList(process));
            StochasticTakeSnapshotEvent snapshotEvent = new StochasticTakeSnapshotEvent(delay, takeSnapshotEvent,
                    procs.size());
            for (StochasticProcess stochasticProcess : procs) {
                stochasticProcess.terminateEvent.setSnapshotEvent(snapshotEvent);
            }
        }
    }

    protected final Snapshot snapshot(TakeSnapshot takeSnapshotEvent) {
        return new Snapshot(takeSnapshotEvent);
    }

    // **************************************************************************
    public final void simulate(Class<? extends ComponentDefinition> main) {
        simulate(main, new CodeInterceptor(null, false));
    }

    public final void simulate(Class<? extends ComponentDefinition> main, boolean allowThreads) {
        simulate(main, new CodeInterceptor(null, allowThreads));
    }

    public final void simulate(Class<? extends ComponentDefinition> main, Translator t) {
        InstrumentationHelper.store(this);
        final ClassLoader tcxtl = Thread.currentThread().getContextClassLoader();
        final ClassLoader fixedCL = new JarURLFixClassLoader(tcxtl);
        final LoaderClassPath lcp = new LoaderClassPath(fixedCL);
        final ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(lcp);

        try {
            Loader cl = AccessController.doPrivileged(new PrivilegedAction<Loader>() {
                @Override
                public Loader run() {
                    return new Loader(tcxtl, cp);
                }
            });
            cl.delegateLoadingOf("jdk.internal.misc.Unsafe");
            cl.delegateLoadingOf("jdk.internal.reflect.MethodAccessorImpl"); // needed for Mockito#mock
            cl.delegateLoadingOf("jdk.internal.reflect.ConstructorAccessorImpl");
            cl.delegateLoadingOf("jdk.internal.reflect.SerializationConstructorAccessorImpl");
            cl.addTranslator(cp, t);
            Thread.currentThread().setContextClassLoader(cl);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            cl.run(main.getCanonicalName(), null);
        } catch (Throwable e) {
            throw new RuntimeException("Exception caught during simulation", e);
        } finally {
            Thread.currentThread().setContextClassLoader(tcxtl); // reset loader after simulation
        }
    }

    public static SimulationScenario load(String scenarioFile) {
        return InstrumentationHelper.load(scenarioFile);
    }

    // **************************************************************************
    public final LinkedList<StochasticSimulatorEvent> generateEventList() {
        LinkedList<StochasticSimulatorEvent> eventList = new LinkedList<>();
        int started = 0;
        for (StochasticProcess process : processes) {
            if (!process.relativeStartTime) {
                eventList.add(process.startEvent);
                started++;
            }
        }
        if (started == 0) {
            throw new RuntimeException("Processes have circular relative start times");
        }
        if (terminatedEvent != null && !terminatedEvent.isRelativeTime()) {
            eventList.add(terminatedEvent);
        }
        return eventList;
    }

    protected final Distribution<Double> constant(double value) {
        return new ConstantDistribution<>(Double.class, value);
    }

    protected final Distribution<Long> constant(long value) {
        return new ConstantDistribution<>(Long.class, value);
    }

    protected final Distribution<BigInteger> constant(BigInteger value) {
        return new ConstantDistribution<>(BigInteger.class, value);
    }

    protected final Distribution<Double> uniform(double min, double max) {
        return new DoubleUniformDistribution(min, max, random);
    }

    protected final Distribution<Long> uniform(long min, long max) {
        return new LongUniformDistribution(min, max, random);
    }

    protected final Distribution<BigInteger> uniform(BigInteger min, BigInteger max) {
        return new BigIntegerUniformDistribution(min, max, random);
    }

    protected final Distribution<BigInteger> uniform(int numBits) {
        return new BigIntegerUniformDistribution(numBits, random);
    }

    protected final Distribution<Double> exponential(double mean) {
        return new DoubleExponentialDistribution(mean, random);
    }

    protected final Distribution<Long> exponential(long mean) {
        return new LongExponentialDistribution(mean, random);
    }

    protected final Distribution<BigInteger> exponential(BigInteger mean) {
        return new BigIntegerExponentialDistribution(mean, random);
    }

    protected final Distribution<Double> normal(double mean, double variance) {
        return new DoubleNormalDistribution(mean, variance, random);
    }

    protected final Distribution<Long> normal(long mean, long variance) {
        return new LongNormalDistribution(mean, variance, random);
    }

    protected final Distribution<BigInteger> normal(BigInteger mean, BigInteger variance) {
        return new BigIntegerNormalDistribution(mean, variance, random);
    }
}
