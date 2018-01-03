/*
 * This file is part of the Kompics Simulator.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) 
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.kompics.simulator.result;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javassist.ClassPool;
import javassist.Loader;
import javassist.LoaderClassPath;
import junit.framework.Assert;
import org.junit.Test;
import se.sics.kompics.simulator.instrumentation.CodeInterceptor;
import se.sics.kompics.simulator.instrumentation.JarURLFixClassLoader;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SimulationResultSingletonTest {

    @Test
    public void test() throws InterruptedException {
        Assert.assertNull(SimulationResultSingleton.instance);
        SimulationResultMap m = SimulationResultSingleton.getInstance();
        Assert.assertNotNull(SimulationResultSingleton.instance);
        Thread t1 = new Thread(new TestThread1());
        t1.start();
        Thread.sleep(1000);
        int res = m.get("a", Integer.class);
        Assert.assertEquals(1, res);
    }

    public static class TestThread1 implements Runnable {

        public TestThread1() {
        }

        @Override
        public void run() {
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
                cl.addTranslator(cp, new CodeInterceptor(null, false));
                Thread.currentThread().setContextClassLoader(cl);
                cl.run(TestMain.class.getCanonicalName(), null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(tcxtl); // reset loader after simulation
            }

        }
    }

}
