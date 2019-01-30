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
package se.sics.kompics.simulator.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author lkroll
 */
public class JarURLFixClassLoader extends ClassLoader {

    private final ClassLoader parent;
    
    public JarURLFixClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }
    
    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        if (url == null) {
            return null;
        }
        try {
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                //System.out.println("Taking apart url to build a proper JarFile instance...");
                String urlS = url.toExternalForm();
                String[] urlParts = urlS.substring(9).split("!/");
                if (urlParts.length != 2) {
                    //System.err.println("Couldn't split input properly: " + urlS);
                    return null;
                }
                String jarPart = urlParts[0];
                //System.out.println("jarPart: "+ jarPart);
                String entryPart = urlParts[1];
                //System.out.println("entryPart: "+ entryPart);
                JarFile jf = new JarFile(jarPart);
                //System.out.println("jf: " + jf);
                JarEntry je = jf.getJarEntry(entryPart);
                //System.out.println("je: " + je);
                InputStream is = jf.getInputStream(je);
                if (is != null) {
                    //System.out.println("is: " + is);
                    return is;
                } else {
                    //System.err.println("Couldn't get a proper input stream...dunno why -.- ");
                    return null;
                }
            } else {                 
                return url.openStream();
            }
        } catch (IOException e) {
            System.err.println("Caught an exception: " + e);
            return null;
        }
    }
}
