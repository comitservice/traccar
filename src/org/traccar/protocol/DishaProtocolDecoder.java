/*
 * Copyright 2015 - 2016 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.model.Event;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.util.regex.Pattern;

public class DishaProtocolDecoder extends BaseProtocolDecoder {

    public DishaProtocolDecoder(DishaProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$A#A#")
            .number("(d+)#")                     // imei
            .expression("([AVMX])#")             // validity
            .number("(dd)(dd)(dd)#")             // time
            .number("(dd)(dd)(dd)#")             // date (ddmmyy)
            .number("(dd)(dd.d+)#")              // latitude
            .expression("([NS])#")
            .number("(ddd)(dd.d+)#")             // longitude
            .expression("([EW])#")
            .number("(d+.d+)#")                  // speed
            .number("(d+.d+)#")                  // course
            .number("(d+)#")                     // satellites
            .number("(d+.d+)#")                  // hdop
            .number("(d+)#")                     // gsm
            .expression("([012])#")              // power mode
            .number("(d+)#")                     // battery
            .number("(d+)#")                     // adc 1
            .number("(d+)#")                     // adc 2
            .number("d+.d+#")                    // day distance
            .number("(d+.d+)#")                  // odometer
            .expression("([01]+)")               // digital inputs
            .text("*")
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        if (!identify(parser.next(), channel, remoteAddress)) {
            return null;
        }
        position.setDeviceId(getDeviceId());

        position.setValid(parser.next().equals("A"));

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt())
                .setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());

        position.setSpeed(parser.nextDouble());
        position.setCourse(parser.nextDouble());

        position.set(Event.KEY_SATELLITES, parser.next());
        position.set(Event.KEY_HDOP, parser.next());
        position.set(Event.KEY_GSM, parser.next());
        position.set(Event.KEY_CHARGE, parser.nextInt() == 2);
        position.set(Event.KEY_BATTERY, parser.next());

        position.set(Event.PREFIX_ADC + 1, parser.nextInt());
        position.set(Event.PREFIX_ADC + 1, parser.nextInt());

        position.set(Event.KEY_ODOMETER, parser.next());
        position.set(Event.KEY_INPUT, parser.next());

        return position;
    }

}
