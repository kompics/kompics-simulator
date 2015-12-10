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
package se.sics.kompics.simulator.core.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.Channel;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kill;
import se.sics.kompics.Kompics;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.SimulationSetup;
import se.sics.kompics.simulator.core.SimulatorComp;
import se.sics.kompics.simulator.core.SimulatorControlPort;
import se.sics.kompics.simulator.core.SimulatorPort;
import se.sics.kompics.simulator.core.impl.selector.SimTrafficSelector;
import se.sics.kompics.simulator.events.SetupEvent;
import se.sics.kompics.simulator.events.TerminateExperiment;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simutil.selectors.DestinationHostSelector;
import se.sics.kompics.simutil.identifiable.Identifier;
import se.sics.kompics.simutil.msg.ContentMsg;
import se.sics.kompics.simutil.msg.impl.BasicAddress;
import se.sics.kompics.simulator.util.GlobalViewHandler;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class SimulatorMngrComp extends ComponentDefinition implements SimulatorComp {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorMngrComp.class);
    private String logPrefix = "";

    private final Positive simPort = requires(SimulatorPort.class);
    private final Positive simControlPort = requires(SimulatorControlPort.class);
    private final Positive requiredNetwork = requires(Network.class);
    private final Positive timer = requires(Timer.class);

    private final Negative providedNetwork = provides(Network.class);

    private final SimulationContextImpl simulationContext;
    private Map<Identifier, Pair<Component, Channel[]>> systemNodes = new HashMap<>();

    public SimulatorMngrComp(SimulatorMngrInit init) {
        LOG.info("{}initiating...", logPrefix);
        simulationContext = new SimulationContextImpl(this, SimulationScenario.getRandom());

        subscribe(handleStart, control);
        subscribe(handleSetup, simPort);
        subscribe(handleStartNode, simPort);
        subscribe(handleKillNode, simPort);
        subscribe(handleTerminateExperiment, simControlPort);

        Config.Builder cb = config().modify(id());
        cb.setValue(SimulationSetup.GLOBAL_VIEW_ADDRESS, SimulationSetup.globalViewAddress);
        updateConfig(cb.finalise());

        connect(providedNetwork, requiredNetwork, SimTrafficSelector.passAppTraffic(true), Channel.TWO_WAY);
    }

    //**********CONTROL HANDLERS************************************************
    private final Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
        }
    };
    //************************TESTING_HANDLERS**********************************
    private final Handler handleNet = new Handler<Msg>() {
        @Override
        public void handle(Msg msg) {
            LOG.debug("{}net msg:{}", logPrefix, msg);
        }
    };
    //**************************************************************************
    private final Handler handleSetup = new Handler<SetupEvent>() {
        @Override
        public void handle(SetupEvent setup) {
            LOG.debug("{}received setup:{}", logPrefix, setup);
            //subscribe custom handlers for specific network events
            for (final GlobalViewHandler globalViewHandler : setup.getGlobalViewHandlers()) {
                globalViewHandler.setSimulationContext(simulationContext);
                subscribe(globalViewHandler, providedNetwork);
            }
            setup.setupSystemContext();
            setup.setupSimulationContext(simulationContext);
        }
    };

    private final Handler handleStartNode = new Handler<StartNodeEvent>() {

        @Override
        public void handle(StartNodeEvent startNode) {
            LOG.debug("{}received start:{} for node:{}", new Object[]{logPrefix, startNode,
                startNode.getNodeId()});

            Config.Builder cb = config().modify(id());
            //TODO Alex netbeans 8.0.2 bug? need to manually cast the Set to the correct thing
            for (Map.Entry<String, Object> confUpdate : (Set<Map.Entry<String, Object>>) startNode.initConfigUpdate().entrySet()) {
                cb.setValue(confUpdate.getKey(), confUpdate.getValue());
            }
            ConfigUpdate configUpdate = cb.finalise();

            Component node = create(startNode.getComponentDefinition(), startNode.getComponentInit(), configUpdate);
            Channel[] channels = new Channel[2];
            channels[0] = connect(node.getNegative(Timer.class), timer, Channel.TWO_WAY);
            channels[1] = connect(node.getNegative(Network.class), providedNetwork.getPair(),
                    new DestinationHostSelector(startNode.getNodeId(), true), Channel.TWO_WAY);

            systemNodes.put(startNode.getNodeId(), Pair.with(node, channels));
            simulationContext.startNode(startNode.getNodeId());
            trigger(Start.event, node.control());
        }
    };

    private Handler handleKillNode = new Handler<KillNodeEvent>() {

        @Override
        public void handle(KillNodeEvent killNode) {
            LOG.debug("{}received kill:{} for node:{}", new Object[]{logPrefix,
                killNode, killNode.getNodeId()});
            Pair<Component, Channel[]> node = systemNodes.remove(killNode.getNodeId());
            if (node == null) {
                throw new RuntimeException("node does not exist");
            }
            disconnect(node.getValue1()[0]);
            disconnect(node.getValue1()[1]);
            trigger(Kill.event, node.getValue0().control());
        }
    };

    public void terminate() {
        LOG.debug("{}sending terminate", logPrefix);
        trigger(new TerminateExperiment(), simControlPort);
    }

    private final Handler handleTerminateExperiment = new Handler<TerminateExperiment>() {

        @Override
        public void handle(TerminateExperiment event) {
            LOG.info("{}terminating simulation...", logPrefix);
            Kompics.forceShutdown();
        }
    };

    public static class SimulatorMngrInit extends Init<SimulatorMngrComp> {
    }
}
