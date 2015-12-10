/*
 * This file is part of the Kompics Simulator.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) 
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * This program is free software; you can redistribute it and/or
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
package se.sics.kompics.simulator.examples.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ConfigReadingComp extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigReadingComp.class);
    private String logPrefix;

    private final Positive network = requires(Network.class);
    private final Positive timer = requires(Timer.class);
    
    public ConfigReadingComp(ConfigReadingInit init) {
        logPrefix = "<nid:" + config().getValue("system.id", Integer.class) + "> ";
        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control); 
    }
    
    private Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
            LOG.info("{}config value:{} with default(reference)", logPrefix, config().getValue("example.val", String.class));
        }
    };
    
    public static class ConfigReadingInit extends Init<ConfigReadingComp> {
    }
}
