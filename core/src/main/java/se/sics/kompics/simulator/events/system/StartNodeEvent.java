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
package se.sics.kompics.simulator.events.system;

import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.events.SystemEvent;

/**
 * @author Alex Ormenisan <aaor@sics.se>
 */
public abstract class StartNodeEvent extends SystemEvent {

    public StartNodeEvent() {
        super();
    }

    /**
     * Override to provide custom implementation. Default implementation provides no per node config
     *
     * @return per node configuration difference &lt;optionName,optionValue&gt;
     */
    public Map<String, Object> initConfigUpdate() {
        HashMap<String, Object> empty = new HashMap<>();
        return empty;
    }

    public abstract Address getNodeAddress();

    public abstract Class<? extends ComponentDefinition> getComponentDefinition();

    // TODO maybe make this properly conform to a single component type?
    public abstract Init getComponentInit();
}
