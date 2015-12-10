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
package se.sics.kompics.simulator.examples.config.sim;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.network.Address;
import se.sics.kompics.simutil.identifiable.impl.IntIdentifier;
import se.sics.kompics.simutil.msg.impl.BasicAddress;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ScenarioSetup {

    public static final Map<Integer, Address> nodeAddressMap = new HashMap<>();

    static {
        try {
            nodeAddressMap.put(1, new BasicAddress(InetAddress.getByName("193.0.0.1"), 12345, new IntIdentifier(1)));
            nodeAddressMap.put(2, new BasicAddress(InetAddress.getByName("193.0.0.2"), 12346, new IntIdentifier(2)));
            nodeAddressMap.put(3, new BasicAddress(InetAddress.getByName("193.0.0.3"), 12347, new IntIdentifier(3)));
            nodeAddressMap.put(4, new BasicAddress(InetAddress.getByName("193.0.0.4"), 12348, new IntIdentifier(4)));
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }
}
