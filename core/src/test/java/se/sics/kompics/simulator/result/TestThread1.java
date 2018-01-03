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
import se.sics.kompics.simulator.instrumentation.JarURLFixClassLoader;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TestThread1 implements Runnable {

  public TestThread1() {
    ClassLoader tcxtl = Thread.currentThread().getContextClassLoader();
    ClassLoader fixedCL = new JarURLFixClassLoader(tcxtl);
    Thread.currentThread().setContextClassLoader(fixedCL);
  }

  @Override
  public void run() {
    Assert.assertNull(SimulationResultSingleton.instance);
    SimulationResultSingleton.getInstance().put("a", 1);
  }
}
