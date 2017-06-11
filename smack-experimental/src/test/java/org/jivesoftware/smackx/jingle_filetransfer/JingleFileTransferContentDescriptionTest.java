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
package org.jivesoftware.smackx.jingle_filetransfer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;

import org.jivesoftware.smack.test.util.SmackTestSuite;
import org.jivesoftware.smack.test.util.TestUtils;
import org.jivesoftware.smackx.hashes.HashManager;
import org.jivesoftware.smackx.hashes.element.HashElement;
import org.jivesoftware.smackx.jingle.element.JingleContentDescription;
import org.jivesoftware.smackx.jingle.element.JingleContentDescriptionChildElement;
import org.jivesoftware.smackx.jingle_filetransfer.element.JingleFileTransferChild;
import org.jivesoftware.smackx.jingle_filetransfer.element.JingleFileTransferContentDescription;
import org.jivesoftware.smackx.jingle_filetransfer.element.Range;
import org.jivesoftware.smackx.jingle_filetransfer.provider.JingleFileTransferContentDescriptionProvider;
import org.junit.Test;
import org.jxmpp.util.XmppDateTime;

/**
 * Test the JingleContentDescriptionFileTransfer element and provider.
 */
public class JingleFileTransferContentDescriptionTest extends SmackTestSuite {

    @Test
    public void parserTest() throws Exception {
        String dateString = "2012-01-02T03:04:05.000+00:00";
        String descriptionString = "The Description";
        String mediaTypeString = "text/plain";
        String nameString = "the-file.txt";
        int sizeInt = 4096;
        HashManager.ALGORITHM algorithm = HashManager.ALGORITHM.SHA_256;
        String hashB64 = "f4OxZX/x/FO5LcGBSKHWXfwtSx+j1ncoSt3SABJtkGk=";
        String xml =
                "<description xmlns='urn:xmpp:jingle:apps:file-transfer:5'>" +
                    "<file>" +
                        "<date>" + dateString + "</date>" +
                        "<desc>" + descriptionString + "</desc>" +
                        "<media-type>" + mediaTypeString + "</media-type>" +
                        "<name>" + nameString + "</name>" +
                        "<range/>" +
                        "<size>" + sizeInt + "</size>" +
                        "<hash xmlns='urn:xmpp:hashes:2' algo='" + algorithm + "'>" +
                            hashB64 +
                        "</hash>" +
                    "</file>" +
                "</description>";
        HashElement hashElement = new HashElement(algorithm, hashB64);
        Range range = new Range();
        Date date = XmppDateTime.parseDate(dateString);
        JingleFileTransferChild jingleFileTransferChild = new JingleFileTransferChild(date, descriptionString, hashElement, mediaTypeString, nameString, sizeInt, range);
        ArrayList<JingleContentDescriptionChildElement> payloads = new ArrayList<>();
        payloads.add(jingleFileTransferChild);

        JingleFileTransferContentDescription descriptionFileTransfer =
                new JingleFileTransferContentDescription(payloads);
        assertEquals(xml, descriptionFileTransfer.toXML().toString());

        JingleContentDescription parsed = new JingleFileTransferContentDescriptionProvider()
                .parse(TestUtils.getParser(xml));
        assertEquals(xml, parsed.toXML().toString());

        JingleFileTransferChild payload = (JingleFileTransferChild) parsed.getJingleContentDescriptionChildren().get(0);
        assertEquals(date, payload.getDate());
        assertEquals(descriptionString, payload.getDescription());
        assertEquals(mediaTypeString, payload.getMediaType());
        assertEquals(nameString, payload.getName());
        assertEquals(sizeInt, payload.getSize());
        assertEquals(range, payload.getRange());
        assertEquals(hashElement, payload.getHash());

        JingleFileTransferContentDescription descriptionFileTransfer1 = new JingleFileTransferContentDescription(null);
        assertNotNull(descriptionFileTransfer1.getJingleContentDescriptionChildren());
    }

    @Test
    public void parserTest2() throws Exception {
        HashManager.ALGORITHM algorithm = HashManager.ALGORITHM.SHA_256;
        String hashB64 = "f4OxZX/x/FO5LcGBSKHWXfwtSx+j1ncoSt3SABJtkGk=";
        HashElement hashElement = new HashElement(algorithm, hashB64);
        Range range = new Range(2048, 1024, hashElement);
        String xml =
                "<description xmlns='urn:xmpp:jingle:apps:file-transfer:5'>" +
                    "<file>" +
                        "<range offset='2048' length='1024'>" +
                            "<hash xmlns='urn:xmpp:hashes:2' algo='" + algorithm + "'>" +
                                hashB64 +
                            "</hash>" +
                        "</range>" +
                    "</file>" +
                "</description>";
        JingleFileTransferChild payload = new JingleFileTransferChild(null, null, null, null, null, -1, range);
        ArrayList<JingleContentDescriptionChildElement> list = new ArrayList<>();
        list.add(payload);
        JingleFileTransferContentDescription fileTransfer = new JingleFileTransferContentDescription(list);
        assertEquals(xml, fileTransfer.toXML().toString());
        JingleFileTransferContentDescription parsed = new JingleFileTransferContentDescriptionProvider()
                .parse(TestUtils.getParser(xml));
        assertEquals(xml, parsed.toXML().toString());
    }
}