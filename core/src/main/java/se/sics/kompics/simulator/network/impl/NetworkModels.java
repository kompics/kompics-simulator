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

package se.sics.kompics.simulator.network.impl;

import org.javatuples.Pair;
import se.sics.kompics.network.Msg;
import se.sics.kompics.simulator.network.NetworkModel;
import se.sics.kompics.simulator.network.PartitionMapper;
import se.sics.kompics.simulator.network.identifier.Identifier;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Paris Carbone {@literal <parisc@kth.se>}
 */
public class NetworkModels {

    public static NetworkModel withConstantDelay(final long delay) {
        return new NetworkModel() {
            @SuppressWarnings("rawtypes")
            @Override
            public long getLatencyMs(Msg message) {
                return delay;
            }
        };
    }

    public static UniformRandomModel withUniformRandomDelay(long min, long max) {
        return new UniformRandomModel(min, max);
    }

    public static UniformRandomModel withUniformRandomDelay(long min, long max, Random rand) {
        return new UniformRandomModel(min, max, rand);
    }

    public static BasicLossyLinkModel withLoss(NetworkModel baseModel, int lossRate, Random rand) {
        return new BasicLossyLinkModel(baseModel, lossRate, rand);
    }

    public static BasicLossyLinkModel withLoss(NetworkModel baseModel, int lossRate) {
        return new BasicLossyLinkModel(baseModel, lossRate, new Random(1));
    }

    public static NetworkModel withTotalLoss() {
        return NetworkModels.withConstantDelay(-1);
    }

    public static DeadLinkNetworkModel withDeadLinks(IdentifierExtractor idE, NetworkModel baseNM,
            Set<Pair<Identifier, Identifier>> deadLinks) {
        return new DeadLinkNetworkModel(idE, baseNM, deadLinks);
    }

    public static KingLatencyModel withKingLatency(IdentifierExtractor extractor) {
        return new KingLatencyModel(extractor);
    }

    public static KingLatencyModel withKingLatency(IdentifierExtractor extractor, int seed) {
        return new KingLatencyModel(extractor, seed);
    }

    public static BinaryNetworkModel withBinaryModel(IdentifierExtractor extr, NetworkModel firstNM,
            NetworkModel secondNM, Set<Identifier> secondModelIDs) {
        return new BinaryNetworkModel(extr, firstNM, secondNM, secondModelIDs);
    }

    public static DisconnectedNodesNetworkModel withDisconnectedModel(IdentifierExtractor extr, NetworkModel firstNM,
            Set<Identifier> disconnected) {
        return new DisconnectedNodesNetworkModel(extr, firstNM, disconnected);
    }

    public static SelectiveModelBuilder withSelectiveModel(IdentifierExtractor extractor, NetworkModel firstNM) {
        return new SelectiveModelBuilder(firstNM, extractor);
    }

    public static PartitionedNetworkModel withPartitionedModel(IdentifierExtractor extractor, NetworkModel model,
            PartitionMapper<Identifier> mapper) {
        return new PartitionedNetworkModel(extractor, model, mapper);
    }

    protected static class SelectiveModelBuilder {
        private final NetworkModel baseModel;
        private final IdentifierExtractor extr;
        private Set<Identifier> nodes = new HashSet<>();

        public SelectiveModelBuilder(NetworkModel baseModel, IdentifierExtractor extr) {
            this.baseModel = baseModel;
            this.extr = extr;
        }

        public SelectiveModelBuilder addNode(Identifier identifier) {
            nodes.add(identifier);
            return this;
        }

        public BinaryNetworkModel withSelectiveModel(NetworkModel model) {
            return new BinaryNetworkModel(extr, baseModel, model, nodes);
        }
    }

}
