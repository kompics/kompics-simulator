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

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan {@literal <aaor@kth.se>}
 */
public class ConfigReadingComp extends ComponentDefinition {

    // Test throws an exception if these are removed (but doesn't fail^^)
    @SuppressWarnings("unused")
    private final Positive<Network> network = requires(Network.class);
    @SuppressWarnings("unused")
    private final Positive<Timer> timer = requires(Timer.class);

    public ConfigReadingComp(ConfigReadingInit init) {
        loggingCtxPutAlways("nId", config().getValue("system.id", Integer.class).toString());

        subscribe(handleStart, control);
    }

    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            logger.info("config value:{} with default(reference)", config().getValue("example.val", String.class));
        }
    };

    public static class ConfigReadingInit extends Init<ConfigReadingComp> {
    }
}
