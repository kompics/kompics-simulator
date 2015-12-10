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
package se.sics.kompics.simulator.util;

import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.simutil.msg.ContentMsg;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public abstract class GlobalViewHandler<E extends KompicsEvent> extends ClassMatchedHandler<E, ContentMsg<Address, Header<Address>, E>> {
    private SimulationContext simContext;
    
    public void setSimulationContext(SimulationContext simContext) {
        this.simContext = simContext;
    }
    
    @Override 
    public final void handle(E content, ContentMsg<Address, Header<Address>, E> container) {
        handle(content, container, simContext);
    }
    
    public abstract void handle(E content, ContentMsg<Address, Header<Address>, E> container, SimulationContext simContext);
}
