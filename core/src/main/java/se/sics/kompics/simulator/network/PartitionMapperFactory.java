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

package se.sics.kompics.simulator.network;

import se.sics.kompics.simutil.identifiable.Identifier;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class PartitionMapperFactory {
    /**
     * @return if nodeIds have a uniform distribution - the two partitions should be roughly equal
     */
    public static <I extends Identifier> PartitionMapper get2EqualPartitions(Class<I> identifierClass) {
        return new PartitionMapper<I>() {
            @Override
            public int getPartition(I nodeId) {
                return nodeId.partition(2);
            }
        };
    }
    
    public static <I extends Identifier> PartitionMapper get2WeightedPartitions(Class<I> identifierClass, 
            final int weight1, final int weight2) {
        return new PartitionMapper<I>() {

            @Override
            public int getPartition(I nodeId) {
                if(nodeId.partition(weight1 + weight2) < weight1) {
                    return 0;
                } else {
                    return 1;
                }
            }
            
        };
    }
    
}
