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

import junit.framework.Assert;
import org.junit.Test;
import se.sics.kompics.simulator.instrumentation.JarURLFixClassLoader;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SimulationResultSingletonTest {

  @Test
  public void test() throws InterruptedException {
    (new Thread(new TestThread())).start();
    Thread.sleep(1000);
    int val = SimulationResultSingleton.getInstance().get("a", Integer.class);
    Assert.assertEquals(1, val);
  }

  public static class TestThread implements Runnable {

    public TestThread() {
      ClassLoader tcxtl = Thread.currentThread().getContextClassLoader();
      ClassLoader fixedCL = new JarURLFixClassLoader(tcxtl);
      Thread.currentThread().setContextClassLoader(fixedCL);
    }

    @Override
    public void run() {
      SimulationResultSingleton.getInstance().put("a", 1);
    }
  }
}
