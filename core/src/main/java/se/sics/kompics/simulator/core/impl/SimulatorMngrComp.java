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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.core.SimulatorComp;
import se.sics.kompics.simulator.core.SimulatorControlPort;
import se.sics.kompics.simulator.core.SimulatorPort;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.TerminateExperiment;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.network.identifier.DestinationHostSelector;
import se.sics.kompics.simulator.network.identifier.Identifier;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.kompics.simulator.network.identifier.impl.SocketIdExtractor;
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

    private final GlobalViewImpl globalView;
    private IdentifierExtractor idE;
    private Map<Identifier, Pair<Component, Channel[]>> systemNodes = new HashMap<>();
    private Map<Component, Pair<Component, Channel[]>> dyingNodes = new HashMap<>();

    public SimulatorMngrComp(SimulatorMngrInit init) {
        LOG.info("{}initiating...", logPrefix);
        globalView = new GlobalViewImpl(this, SimulationScenario.getRandom());

        subscribe(handleStart, control);
        subscribe(handleSetup, simPort);
        subscribe(handleStartNode, simPort);
        subscribe(handleKillNode, simPort);
        subscribe(handleKilledNode, control);
        subscribe(handleTerminateExperiment, simControlPort);

        defaultSetup();
    }
    
    @Override
    public Fault.ResolveAction handleFault(Fault fault) {
        LOG.error("{}fault:{}", logPrefix, fault.getCause());
        return Fault.ResolveAction.ESCALATE;
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
    private void defaultSetup() {
        connect(providedNetwork, requiredNetwork, Channel.TWO_WAY);
        idE = new SocketIdExtractor();
    }
    
    private final Handler handleSetup = new Handler<SetupEvent>() {
        @Override
        public void handle(SetupEvent setup) {
            LOG.debug("{}received setup:{}", logPrefix, setup);

            idE = setup.getIdentifierExtractor();
            
//            Pair<Address, Set<GlobalViewHandler>> globalViewSetup = setup.getGlobalViewSetup();
//            if (globalViewSetup != null) {
//                for (final GlobalViewHandler globalViewHandler : globalViewSetup.getValue1()) {
//                    globalViewHandler.setSimulationContext(globalView);
//                    subscribe(globalViewHandler, providedNetwork);
//                }
//            }
            setup.setupSystemContext();
            setup.setupGlobalView(globalView);
        }
    };

    private final Handler handleStartNode = new Handler<StartNodeEvent>() {

        @Override
        public void handle(StartNodeEvent startNode) {
            LOG.debug("{}received start:{} for node:{}", new Object[]{logPrefix, startNode,
                startNode.getNodeAddress()});

            Config.Builder cb = config().modify(id());
            //TODO Alex netbeans 8.0.2 bug? need to manually cast the Set to the correct thing
            for (Map.Entry<String, Object> confUpdate : (Set<Map.Entry<String, Object>>) startNode.initConfigUpdate().entrySet()) {
                cb.setValue(confUpdate.getKey(), confUpdate.getValue());
            }
            cb.setValue("simulation.globalview", globalView);
            ConfigUpdate configUpdate = cb.finalise();

            Component node;
            if(startNode.getComponentInit() instanceof Init.None) {
                node = create(startNode.getComponentDefinition(), Init.NONE, configUpdate);
            } else {
                node = create(startNode.getComponentDefinition(), startNode.getComponentInit(), configUpdate);
            }
            Channel[] channels = new Channel[2];
            channels[0] = connect(node.getNegative(Timer.class), timer, Channel.TWO_WAY);
            channels[1] = connect(node.getNegative(Network.class), providedNetwork.getPair(),
                    new DestinationHostSelector(idE.extract(startNode.getNodeAddress()), idE, true), Channel.TWO_WAY);

            systemNodes.put(idE.extract(startNode.getNodeAddress()), Pair.with(node, channels));
            globalView.startNode(idE.extract(startNode.getNodeAddress()), startNode.getNodeAddress());
            trigger(Start.event, node.control());
        }
    };

    private Handler handleKillNode = new Handler<KillNodeEvent>() {

        @Override
        public void handle(KillNodeEvent killNode) {
            LOG.debug("{}received kill:{}", new Object[]{logPrefix, killNode});
            Pair<Component, Channel[]> node = systemNodes.remove(idE.extract(killNode.getNodeAddress()));
            if (node == null) {
                throw new RuntimeException("node does not exist");
            }
            globalView.killNode(idE.extract(killNode.getNodeAddress()));
            LOG.debug("skipping disconnecting channels");
            dyingNodes.put(node.getValue0(), node);
            //disconnect(node.getValue1()[0]);
            //disconnect(node.getValue1()[1]);
            trigger(Kill.event, node.getValue0().control());
        }
    };

    private Handler handleKilledNode = new Handler<Killed>() {

        @Override
        public void handle(Killed killedNode) {
            LOG.debug("Received Killed from {}, about to disconnect channels", killedNode.component);
            Pair<Component, Channel[]> node = dyingNodes.get(killedNode.component);
            if (node == null) {
                throw new RuntimeException("node was never dying");
            }
            disconnect(node.getValue1()[0]);
            disconnect(node.getValue1()[1]);
            dyingNodes.remove(node.getValue0());
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
