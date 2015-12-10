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
package se.sics.kompics.simulator.examples.globalview.sim;

import java.util.HashSet;
import java.util.Set;
import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.examples.globalview.HostComp;
import se.sics.kompics.simulator.examples.globalview.PingPongDoneEvent;
import se.sics.kompics.simutil.identifiable.Identifiable;
import se.sics.kompics.simutil.identifiable.Identifier;
import se.sics.kompics.simutil.msg.ContentMsg;
import se.sics.kompics.simulator.util.GlobalViewHandler;
import se.sics.kompics.simulator.util.SimulationContext;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class ScenarioGen {

    static Operation setupOp = new Operation<SetupEvent>() {

        @Override
        public SetupEvent generate() {
            final GlobalViewHandler handleGlobalPingDone = new GlobalViewHandler<PingPongDoneEvent>() {
                @Override
                public void handle(PingPongDoneEvent content, ContentMsg<Address, Header<Address>, PingPongDoneEvent> container, SimulationContext simContext) {
                    AllPingPong view = (AllPingPong) simContext.get(AllPingPong.OPT_NAME);
                    view.pingpongCount++;
                    if (view.pingpongCount == 3) {
                        simContext.terminate();
                    }
                }
            };

            return new SetupEvent() {
                @Override
                public Set<GlobalViewHandler> getGlobalViewHandlers() {
                    Set<GlobalViewHandler> globalViewHandlers = new HashSet<>();
                    globalViewHandlers.add(handleGlobalPingDone);
                    return globalViewHandlers;
                }

                @Override
                public void setupSimulationContext(SimulationContext simContext) {
                    simContext.register(AllPingPong.OPT_NAME, new AllPingPong());
                }
            };
        }
    };

    static Operation1 startNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer node) {
            return new StartNodeEvent<HostComp>() {
                Address selfAdr;
                Address pingAdr;

                {
                    selfAdr = ScenarioSetup.nodeAddressMap.get(node);
                    pingAdr = ScenarioSetup.nodePing.get(node);
                }

                @Override
                public Identifier getNodeId() {
                    return ((Identifiable) selfAdr).getId();
                }

                @Override
                public Class getComponentDefinition() {
                    return HostComp.class;
                }

                @Override
                public HostComp.HostInit getComponentInit() {
                    return new HostComp.HostInit(selfAdr, pingAdr);
                }
            };
        }
    };

    public static SimulationScenario simplePing() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess setupSimulation = new StochasticProcess() {
                    {
                        raise(1, setupOp);
                    }
                };

                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(4, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };

                setupSimulation.start();
                startPeers.startAfterTerminationOf(1000, setupSimulation);
                terminateAfterTerminationOf(20000, startPeers);
            }
        };
        return scen;
    }
}
