/**
 *
 * Copyright © 2017 Paul Schaub
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
package org.jivesoftware.smackx.jingle_ibb;

import org.jivesoftware.smack.test.util.SmackTestSuite;
import org.jivesoftware.smack.test.util.TestUtils;
import org.jivesoftware.smackx.jingle_ibb.element.JingleInBandByteStreamTransport;
import org.jivesoftware.smackx.jingle_ibb.provider.JingleInBandByteStreamTransportProvider;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;

/**
 * Test JingleInBandByteStreamTransport provider and element.
 */
public class JingleInBandByteStreamTransportTest extends SmackTestSuite {

    @Test
    public void parserTest() throws Exception {
        String sid = JingleInBandByteStreamManager.generateSessionId();
        short size = 8192;

        String xml = "<transport xmlns='urn:xmpp:jingle:transports:ibb:1' block-size='8192' sid='" + sid + "'/>";

        JingleInBandByteStreamTransport transport = new JingleInBandByteStreamTransport(size, sid);
        assertEquals(xml, transport.toXML().toString());
        assertEquals(size, transport.getBlockSize());
        assertEquals(sid, transport.getSessionId());

        JingleInBandByteStreamTransport parsed = new JingleInBandByteStreamTransportProvider()
                .parse(TestUtils.getParser(xml));
        assertEquals(transport, parsed);
        assertTrue(transport.equals(parsed));
        assertEquals(xml, parsed.toXML().toString());

        JingleInBandByteStreamTransport transport1 = new JingleInBandByteStreamTransport((short) 1024);
        assertEquals((short) 1024, transport1.getBlockSize());
        assertNotSame(transport, transport1);
        assertNotSame(transport.getSessionId(), transport1.getSessionId());

        assertFalse(transport.equals(null));

        JingleInBandByteStreamTransport transport2 = new JingleInBandByteStreamTransport();
        assertEquals(JingleInBandByteStreamTransport.DEFAULT_BLOCK_SIZE, transport2.getBlockSize());
        assertFalse(transport1.equals(transport2));

        JingleInBandByteStreamTransport transport3 = new JingleInBandByteStreamTransport((short) -1024);
        assertEquals(JingleInBandByteStreamTransport.DEFAULT_BLOCK_SIZE, transport3.getBlockSize());
    }
}
