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
package se.sics.kompics.simulator.examples.globalview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.simutil.identifiable.Identifiable;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HostComp extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(HostComp.class);
    private String logPrefix;

    private final Positive network = requires(Network.class);
    private final Positive timer = requires(Timer.class);

    private Component pingComp;
    private Component statusComp;

    public HostComp(HostInit init) {
        if (init.selfAdr instanceof Identifiable) {
            logPrefix = "<nid:" + ((Identifiable) init.selfAdr).getId() + "> ";
        } else {
            logPrefix = "<nid:" + init.selfAdr + "> ";
        }

        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control);

        connectComp(init);
    }

    private void connectComp(HostInit init) {
        pingComp = create(PingComp.class, new PingComp.PingInit(init.selfAdr, init.pingAdr));
        connect(pingComp.getNegative(Network.class), network, Channel.TWO_WAY);
        connect(pingComp.getNegative(Timer.class), timer, Channel.TWO_WAY);

        statusComp = create(StatusComp.class, new StatusComp.StatusInit(init.selfAdr));
        connect(statusComp.getNegative(Network.class), network, Channel.TWO_WAY);
        connect(statusComp.getNegative(PingStatusPort.class), pingComp.getPositive(PingStatusPort.class), Channel.TWO_WAY);
    }

    private Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
        }
    };

    public static class HostInit extends Init<HostComp> {

        public final Address selfAdr;
        public final Address pingAdr;

        public HostInit(Address selfAdr, Address pingAdr) {
            this.selfAdr = selfAdr;
            this.pingAdr = pingAdr;
        }
    }
}
