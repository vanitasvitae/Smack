/**
 *
 * Copyright 2020 Paul Schaub
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
package org.jivesoftware.smackx.mix.core.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.test.util.TestUtils;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jivesoftware.smackx.mix.core.element.LeaveElement;

import org.junit.jupiter.api.Test;

public class LeaveElementProviderTest {

    @Test
    public void v1ProviderTest() throws XmlPullParserException, IOException, SmackParsingException {
        String xml = "<leave xmlns='urn:xmpp:mix:core:1'/>";
        LeaveElementProvider.V1 provider = new LeaveElementProvider.V1();
        XmlPullParser parser = TestUtils.getParser(xml);

        LeaveElement element = provider.parse(parser);

        assertNotNull(element);
    }
}
