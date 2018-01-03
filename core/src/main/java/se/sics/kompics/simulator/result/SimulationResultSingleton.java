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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freely adapted from <http://surguy.net/articles/communication-across-classloaders.xml>.
 * 
 * @author Lars Kroll <lkroll@kth.se>
 */
public class SimulationResultSingleton implements SimulationResultMap {

    static SimulationResultMap instance = null;

    public synchronized static SimulationResultMap getInstance() {
        ClassLoader myClassLoader = SimulationResultSingleton.class.getClassLoader();
        if (instance == null) {
            if (!myClassLoader.toString().startsWith("sun.")) {
                try {
                    ClassLoader parentClassLoader = SimulationResultSingleton.class.getClassLoader().getParent();
                    Class otherClassInstance = parentClassLoader.loadClass(SimulationResultSingleton.class.getName());
                    Method getInstanceMethod = otherClassInstance.getDeclaredMethod("getInstance", new Class[]{});
                    Object otherAbsoluteSingleton = getInstanceMethod.invoke(null, new Object[]{});
                    instance = (SimulationResultMap) Proxy.newProxyInstance(myClassLoader,
                            new Class[]{SimulationResultMap.class},
                            new PassThroughProxyHandler(otherAbsoluteSingleton));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                instance = new SimulationResultSingleton();
            }
        }

        return instance;
    }

    private SimulationResultSingleton() {
    }

    private ConcurrentHashMap<String, Object> entries = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object o) {
        entries.put(key, o);
    }

    @Override
    public <T> T get(String key, Class<T> tpe) {
        return (T) entries.get(key);
    }

}
