/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * GVoD is free software; you can redistribute it and/or
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
package se.sics.kompics.simulator.network.impl;

import se.sics.kompics.network.Msg;
import se.sics.kompics.simulator.network.NetworkModel;
import se.sics.kompics.simulator.network.identifier.Identifier;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;

import java.util.Set;

/**
 * @author Paris Carbone {@literal <parisc@kth.se>}
 */
public class BinaryNetworkModel implements NetworkModel {

    private final IdentifierExtractor idE;
    private final NetworkModel firstNM;
    private final NetworkModel secondNM;
    private final Set<Identifier> selectedNodes;

    public BinaryNetworkModel(IdentifierExtractor idE, NetworkModel firstNM, NetworkModel secondNM,
            Set<Identifier> selectedNodes) {
        this.idE = idE;
        this.firstNM = firstNM;
        this.secondNM = secondNM;
        this.selectedNodes = selectedNodes;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public long getLatencyMs(Msg message) {
        Identifier srcId = idE.extract(message.getHeader().getSource());
        Identifier dstId = idE.extract(message.getHeader().getDestination());
        if (selectedNodes.contains(srcId) || selectedNodes.contains(dstId)) {
            return secondNM.getLatencyMs(message);
        }
        return firstNM.getLatencyMs(message);
    }

    @Override
    public String toString() {
        return "Binary Network Model";
    }
}
