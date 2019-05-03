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
package se.sics.kompics.simulator.examples.networkModel.sim;

import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.examples.networkModel.BasicPingComp;
import se.sics.kompics.simulator.network.impl.UniformRandomModel;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class ScenarioGen {

    @SuppressWarnings("serial")
    static Operation<ChangeNetworkModelEvent> networkSetupOp = new Operation<ChangeNetworkModelEvent>() {

        @Override
        public ChangeNetworkModelEvent generate() {
            return new ChangeNetworkModelEvent(new UniformRandomModel(3, 7));
        }
    };

    @SuppressWarnings("serial")
    static Operation1<StartNodeEvent, Integer> startNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer node) {
            return new StartNodeEvent() {
                Address selfAdr;
                Address pingAdr;

                {
                    selfAdr = ScenarioSetup.nodeAddressMap.get(node);
                    pingAdr = ScenarioSetup.nodePing.get(node);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class<BasicPingComp> getComponentDefinition() {
                    return BasicPingComp.class;
                }

                @Override
                public BasicPingComp.BasicPingInit getComponentInit() {
                    return new BasicPingComp.BasicPingInit(selfAdr, pingAdr);
                }
            };
        }
    };

    @SuppressWarnings("serial")
    public static SimulationScenario simplePing() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess networkSetup = new StochasticProcess() {
                    {
                        raise(1, networkSetupOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(4, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };

                networkSetup.start();
                startPeers.startAfterTerminationOf(0, networkSetup);
                terminateAfterTerminationOf(20000, startPeers);
            }
        };

        return scen;
    }
}
