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
package org.jivesoftware.smackx.jingle.internal;

import org.jivesoftware.smackx.jingle.element.JingleAction;

/**
 * Object that represents a pending jingle action. A pending jingle action is an action that was sent to the peer,
 * but a response hasn't yet received.
 */
public abstract class PendingJingleAction {
    private final JingleAction action;
    private final Content affectedContent;

    public PendingJingleAction(JingleAction action, Content content) {
        this.action = action;
        this.affectedContent = content;
    }

    public JingleAction getAction() {
        return action;
    }

    public Content getAffectedContent() {
        return affectedContent;
    }

    public static class TransportReplace extends PendingJingleAction {
        private final Transport<?> newTransport;

        public TransportReplace(Content content, Transport<?> newTransport) {
            super(JingleAction.transport_replace, content);
            this.newTransport = newTransport;
        }

        public Transport<?> getNewTransport() {
            return newTransport;
        }
    }
}
