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
package se.sics.kompics.simulator.examples.networkModel.sim;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.examples.util.BasicAddress;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ScenarioSetup {

    public static final Map<Integer, Address> nodeAddressMap = new HashMap<>();
    public static final Map<Integer, Address> nodePing = new HashMap<>();

    static {
        try {
            nodeAddressMap.put(1, new BasicAddress(InetAddress.getByName("193.0.0.1"), 12345));
            nodeAddressMap.put(2, new BasicAddress(InetAddress.getByName("193.0.0.2"), 12346));
            nodeAddressMap.put(3, new BasicAddress(InetAddress.getByName("193.0.0.3"), 12347));
            nodeAddressMap.put(4, new BasicAddress(InetAddress.getByName("193.0.0.4"), 12348));

            nodePing.put(1, nodeAddressMap.get(2));
            nodePing.put(2, nodeAddressMap.get(3));
            nodePing.put(3, nodeAddressMap.get(4));
            nodePing.put(4, nodeAddressMap.get(1));
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }
}
