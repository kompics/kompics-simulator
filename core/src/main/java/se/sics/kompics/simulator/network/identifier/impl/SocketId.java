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
package se.sics.kompics.simulator.network.identifier.impl;

import com.google.common.primitives.Ints;
import java.net.InetSocketAddress;
import java.util.Objects;
import se.sics.kompics.simulator.network.identifier.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SocketId implements Identifier {
    private final InetSocketAddress isa;
    
    public SocketId(InetSocketAddress isa) {
        this.isa = isa;
    }
    
    @Override
    public int partition(int nrPartitions) {
        int ip = Ints.fromByteArray(isa.getAddress().getAddress());
        return ip % nrPartitions;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.isa);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SocketId other = (SocketId) obj;
        if (!Objects.equals(this.isa, other.isa)) {
            return false;
        }
        return true;
    }
}
