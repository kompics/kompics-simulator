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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TestThread1 implements Runnable {

  ClassLoader cl;

  public TestThread1(ClassLoader cl) {
    this.cl = cl;
  }

  @Override
  public void run() {
    Thread.currentThread().setContextClassLoader(cl);
    try {
      Class otherClassInstance = cl.loadClass(SimulationResultSingleton.class.getName());
      Method m1 = otherClassInstance.getMethod("getInstance");
      Object instance = m1.invoke(null);
      Method m2 = otherClassInstance.getMethod("put", String.class, Object.class);
      m2.invoke(instance, "a", 1);
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchMethodException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SecurityException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvocationTargetException ex) {
      Logger.getLogger(TestThread1.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
}
