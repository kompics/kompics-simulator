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
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.simulator.SimulationSetup;
import se.sics.kompics.simutil.identifiable.Identifiable;
import se.sics.kompics.simutil.msg.ContentMsg;
import se.sics.kompics.simutil.msg.impl.BasicContentMsg;
import se.sics.kompics.simutil.msg.impl.BasicHeader;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class StatusComp extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(StatusComp.class);
    private String logPrefix;

    private final Positive pingStatusPort = requires(PingStatusPort.class);
    private final Positive network = requires(Network.class);
    
    private final Address selfAdr;
    
    public StatusComp(StatusInit init) {
        selfAdr = init.selfAdr;
        if (selfAdr instanceof Identifiable) {
            logPrefix = "<nid:" + ((Identifiable)selfAdr).getId() + "> ";
        } else {
            logPrefix = "<nid:" + selfAdr + "> ";
        }
        
        LOG.info("{}initiating...", logPrefix);
        
        subscribe(handleStart, control);
        subscribe(handleDone, pingStatusPort);
    }
    
    private Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
        }
    };
    
    private void send(Object content, Address target) {
        Header header = new BasicHeader(selfAdr, target, Transport.UDP);
        ContentMsg msg = new BasicContentMsg(header, content);
        LOG.info("{}sending:{}", new Object[]{logPrefix, msg.getContent()});
        trigger(msg, network);
    }
    
    private Handler handleDone = new Handler<PingPongDoneEvent>() {
        @Override
        public void handle(PingPongDoneEvent event) {
            LOG.info("{}done", logPrefix);
            send(event, SimulationSetup.globalViewAddress);
        }
    };
    
    public static class StatusInit extends Init<StatusComp> {
        public final Address selfAdr;
        
        public StatusInit(Address selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
