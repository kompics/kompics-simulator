package se.sics.kompics.simulator.core.impl;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.network.identifier.Identifier;
import se.sics.kompics.simulator.util.GlobalView;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public class GlobalViewImpl implements GlobalView {

    private final SimulatorMngrComp simMngr;
    private final Random rand;
    private final Map<Identifier, Address> aliveNodes = new HashMap<>();
    private final Map<Identifier, Address> deadNodes = new HashMap<>();
    private final Map<String, Object> otherContext = new HashMap<>();
    private boolean terminated;

    public GlobalViewImpl(SimulatorMngrComp simMngr, Random rand) {
        this.simMngr = simMngr;
        this.rand = rand;
        terminated = false;
    }

    public Random getRand() {
        return rand;
    }

    @Override
    public Map<Identifier, Address> getAliveNodes() {
        return aliveNodes;
    }

    public void startNode(Identifier id, Address adr) {
        aliveNodes.put(id, adr);
    }

    @Override
    public Map<Identifier, Address> getDeadNodes() {
        return deadNodes;
    }

    public void killNode(Identifier id) {
        deadNodes.put(id, aliveNodes.remove(id));
    }

    @Override
    public void terminate() {
        if (!terminated) {
            terminated = true;
            simMngr.terminate();
        }
    }

    public boolean terminated() {
        return terminated;
    }

    @Override
    public boolean setValue(String key, Object value) {
        otherContext.put(key, value);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(String key, Class<T> type) throws ClassCastException {
        return (T) otherContext.get(key);
    }
}
