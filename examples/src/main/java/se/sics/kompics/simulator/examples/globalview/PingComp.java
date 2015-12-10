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
package se.sics.kompics.simulator.examples.globalview;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.simutil.msg.ContentMsg;
import se.sics.kompics.simutil.msg.impl.BasicContentMsg;
import se.sics.kompics.simutil.msg.impl.BasicHeader;
import se.sics.kompics.simutil.identifiable.Identifiable;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class PingComp extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(PingComp.class);
    private String logPrefix;

    private final Positive network = requires(Network.class);
    private final Positive timer = requires(Timer.class);
    private final Negative status = provides(PingStatusPort.class);

    private Address selfAdr;
    private Address pingAdr;

    private UUID pingTid;

    public PingComp(PingInit init) {
        selfAdr = init.selfAdr;
        if (selfAdr instanceof Identifiable) {
            logPrefix = "<nid:" + ((Identifiable)selfAdr).getId() + "> ";
        } else {
            logPrefix = "<nid:" + selfAdr + "> ";
        }
        pingAdr = init.pingAdr;
        
        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control);
        subscribe(handlePingTimeout, timer);
        subscribe(handlePing, network);
        subscribe(handlePong, network);
    }

    private Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
            schedulePeriodicPing();
        }
    };

    private void send(Object content, Address target) {
        Header header = new BasicHeader(selfAdr, target, Transport.UDP);
        ContentMsg msg = new BasicContentMsg(header, content);
        LOG.info("{}sending:{} from:{} to:{}", new Object[]{logPrefix, msg.getContent(), 
            msg.getHeader().getSource(), msg.getHeader().getDestination()});
        trigger(msg, network);
    }
    private Handler handlePingTimeout = new Handler<PingTimeout>() {

        @Override
        public void handle(PingTimeout timeout) {
            LOG.info("{}ping timeout", logPrefix);
            if (pingAdr == null) {
                cancelPeriodicPing();
                return;
            }
            send(new Ping(), pingAdr);
        }
    };

    ClassMatchedHandler handlePing
            = new ClassMatchedHandler<Ping, ContentMsg<Address, Header<Address>, Ping>>() {

                @Override
                public void handle(Ping ping, ContentMsg<Address, Header<Address>, Ping> container) {
                    LOG.info("{}received:{}", new Object[]{logPrefix, container});
                    send(ping.pong(), container.getHeader().getSource());
                }
            };

    ClassMatchedHandler handlePong
            = new ClassMatchedHandler<Pong, ContentMsg<Address, Header<Address>, Pong>>() {

                @Override
                public void handle(Pong pong, ContentMsg<Address, Header<Address>, Pong> container) {
                    LOG.info("{}received:{}", new Object[]{logPrefix, container});
                    pingAdr = null;
                    cancelPeriodicPing();
                    trigger(new PingPongDoneEvent(), status);
                }
            };

    public static class PingInit extends Init<PingComp> {

        public final Address selfAdr;
        public final Address pingAdr;

        public PingInit(Address selfAdr, Address pingAdr) {
            this.selfAdr = selfAdr;
            this.pingAdr = pingAdr;
        }
    }

    private void schedulePeriodicPing() {
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(1000, 1000);
        PingTimeout ping = new PingTimeout(spt);
        spt.setTimeoutEvent(ping);
        trigger(spt, timer);
        pingTid = ping.getTimeoutId();
    }

    private void cancelPeriodicPing() {
        trigger(new CancelPeriodicTimeout(pingTid), timer);
        pingTid = null;
    }

    public static class PingTimeout extends Timeout {

        public PingTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
}
