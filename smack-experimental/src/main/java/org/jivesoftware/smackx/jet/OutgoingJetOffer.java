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
package org.jivesoftware.smackx.jet;

import java.io.File;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle_filetransfer.OutgoingJingleFileOffer;

import org.jxmpp.jid.FullJid;

/**
 * Created by vanitas on 14.07.17.
 */
public class OutgoingJetOffer extends OutgoingJingleFileOffer {

    public OutgoingJetOffer(XMPPConnection connection, FullJid responder, String sid) {
        super(connection, responder, sid);
    }

    public OutgoingJetOffer(XMPPConnection connection, FullJid recipient) {
        super(connection, recipient);
    }

    @Override
    public void send(File file) {

    }
}