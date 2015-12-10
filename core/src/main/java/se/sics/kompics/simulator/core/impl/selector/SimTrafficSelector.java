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
package se.sics.kompics.simulator.core.impl.selector;

import se.sics.kompics.ChannelSelector;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Msg;
import se.sics.kompics.simulator.SimulationSetup;
import se.sics.kompics.simutil.identifiable.Identifier;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SimTrafficSelector extends ChannelSelector<Msg, Boolean> {
    public static final Boolean SIMULATION_TRAFFIC = true;
    public static final Boolean APP_TRAFFIC = false;
    
    public SimTrafficSelector(boolean trafficType, boolean positive) {
        super(Msg.class, trafficType, positive);
    }
    
    @Override
    public Boolean getValue(Msg msg) {
        Address dst = msg.getHeader().getDestination();
        if(SimulationSetup.globalViewAddress.sameHostAs(dst)) {
            return SIMULATION_TRAFFIC;
        } else {
            return APP_TRAFFIC;
        }
    }
    
    public static SimTrafficSelector passAppTraffic(boolean positive) {
        return new SimTrafficSelector(APP_TRAFFIC, true);
    }
    
     public static SimTrafficSelector passSimTraffic(boolean positive) {
        return new SimTrafficSelector(SIMULATION_TRAFFIC, true);
    }
}
