/**
 *
 * Copyright 2017 Paul Schaub
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
package org.jivesoftware.smackx.jingle_ibb.provider;

import org.jivesoftware.smackx.jingle.provider.JingleContentTransportProvider;
import org.jivesoftware.smackx.jingle_ibb.element.JingleInBandByteStreamTransport;
import org.xmlpull.v1.XmlPullParser;

/**
 * Parse JingleByteStreamTransport elements.
 */
public class JingleInBandByteStreamTransportProvider extends JingleContentTransportProvider<JingleInBandByteStreamTransport> {
    @Override
    public JingleInBandByteStreamTransport parse(XmlPullParser parser, int initialDepth) throws Exception {
        String blockSizeString = parser.getAttributeValue(null, JingleInBandByteStreamTransport.ATTR_BLOCK_SIZE);
        String sid = parser.getAttributeValue(null, JingleInBandByteStreamTransport.ATTR_SID);

        short blockSize = -1;
        if (blockSizeString != null) {
            blockSize = Short.valueOf(blockSizeString);
        }

        return new JingleInBandByteStreamTransport(blockSize, sid);
    }
}
