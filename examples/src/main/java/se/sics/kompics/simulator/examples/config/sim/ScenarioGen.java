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
package se.sics.kompics.simulator.examples.config.sim;

import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.examples.config.ConfigReadingComp;
import se.sics.kompics.simutil.identifiable.Identifiable;
import se.sics.kompics.simutil.identifiable.Identifier;
import se.sics.kompics.simutil.identifiable.impl.IntIdentifier;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class ScenarioGen {

    static Operation1<StartNodeEvent, Integer> startNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer node) {
            return new StartNodeEvent<ConfigReadingComp>() {
                Address selfAdr;

                {
                    selfAdr = ScenarioSetup.nodeAddressMap.get(node);
                }

                @Override
                public Identifier getNodeId() {
                    return ((Identifiable) selfAdr).getId();
                }

                @Override
                public Class getComponentDefinition() {
                    return ConfigReadingComp.class;
                }

                @Override
                public ConfigReadingComp.ConfigReadingInit getComponentInit() {
                    return new ConfigReadingComp.ConfigReadingInit();
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    Integer nodeId = ((Identifiable<IntIdentifier>) selfAdr).getId().id;
                    nodeConfig.put("system.id", nodeId);
                    if (nodeId % 2 == 0) {
                        nodeConfig.put("example.val", "value is" + nodeId);
                    }
                    return nodeConfig;
                }
            };
        }
    };

    public static SimulationScenario simpleBoot() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(4, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };

                startPeers.start();
                terminateAfterTerminationOf(20000, startPeers);
            }
        };

        return scen;
    }
}
