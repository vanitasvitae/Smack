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
package org.jivesoftware.smackx.jingle.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.Async;
import org.jivesoftware.smackx.jingle.JingleDescriptionManager;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.element.JingleAction;
import org.jivesoftware.smackx.jingle.element.JingleContentElement;
import org.jivesoftware.smackx.jingle.element.JingleElement;
import org.jivesoftware.smackx.jingle.element.JingleReasonElement;
import org.jivesoftware.smackx.jingle.exception.UnsupportedDescriptionException;
import org.jivesoftware.smackx.jingle.exception.UnsupportedSecurityException;
import org.jivesoftware.smackx.jingle.exception.UnsupportedTransportException;
import org.jivesoftware.smackx.jingle.util.Role;

import org.jxmpp.jid.FullJid;

/**
 * Class that represents a Jingle session.
 */
public class JingleSession {
    private static final Logger LOGGER = Logger.getLogger(JingleSession.class.getName());

    private final ConcurrentHashMap<String, JingleContent> contents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JingleContent> proposedContents = new ConcurrentHashMap<>();
    private final JingleManager jingleManager;

    private final FullJid initiator, responder;
    private final Role role;
    private final String sessionId;

    public enum SessionState {
        fresh,      //pre-session-inititate
        pending,    //pre-session-accept
        active,     //pre-session-terminate
        ended       //post-session-terminate
    }

    private SessionState sessionState;

    public JingleSession(JingleManager manager, FullJid initiator, FullJid responder, Role role, String sessionId) {
        this.jingleManager = manager;
        this.initiator = initiator;
        this.responder = responder;
        this.role = role;
        this.sessionId = sessionId;
        this.sessionState = SessionState.fresh;
    }

    public static JingleSession fromSessionInitiate(JingleManager manager, JingleElement initiate)
            throws UnsupportedSecurityException, UnsupportedDescriptionException, UnsupportedTransportException {
        if (initiate.getAction() != JingleAction.session_initiate) {
            throw new IllegalArgumentException("Jingle-Action MUST be 'session-initiate'.");
        }

        JingleSession session = new JingleSession(manager, initiate.getInitiator(), initiate.getResponder(), Role.responder, initiate.getSid());
        List<JingleContentElement> initiateContents = initiate.getContents();

        for (JingleContentElement content : initiateContents) {
            session.addContent(content, manager);
        }

        session.sessionState = SessionState.pending;

        return session;
    }

    public void initiate(XMPPConnection connection) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        if (this.sessionState != SessionState.fresh) {
            throw new IllegalStateException("Session is not in fresh state.");
        }

        connection.createStanzaCollectorAndSend(createSessionInitiate()).nextResultOrThrow();
        this.sessionState = SessionState.pending;
    }

    public void accept(XMPPConnection connection) throws SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        if (this.sessionState != SessionState.pending) {
            throw new IllegalStateException("Session is not in pending state.");
        }

        for (JingleContent content : contents.values()) {
            content.onAccept(connection);
        }

        connection.createStanzaCollectorAndSend(createSessionAccept()).nextResultOrThrow();
        this.sessionState = SessionState.active;
    }

    public JingleElement createSessionInitiate() {
        if (role != Role.initiator) {
            throw new IllegalStateException("Sessions role is not initiator.");
        }

        List<JingleContentElement> contentElements = new ArrayList<>();
        for (JingleContent c : contents.values()) {
            contentElements.add(c.getElement());
        }

        return JingleElement.createSessionInitiate(getInitiator(), getResponder(), getSessionId(), contentElements);
    }

    public JingleElement createSessionAccept() {
        if (role != Role.responder) {
            throw new IllegalStateException("Sessions role is not responder.");
        }

        List<JingleContentElement> contentElements = new ArrayList<>();
        for (JingleContent c : contents.values()) {
            contentElements.add(c.getElement());
        }

        return JingleElement.createSessionAccept(getInitiator(), getResponder(), getSessionId(), contentElements);
    }

    public IQ handleJingleRequest(JingleElement request) {
        switch (request.getAction()) {
            case content_modify:
            case description_info:
            case security_info:
            case session_info:
            case transport_accept:
            case transport_info:
            case transport_reject:
            case transport_replace:
                return getSoleAffectedContentOrThrow(request).handleJingleRequest(request, jingleManager.getConnection());
            case content_accept:
                return handleContentAccept(request);
            case content_add:
                return handleContentAdd(request);
            case content_reject:
                return handleContentReject(request);
            case content_remove:
                return handleContentRemove(request);
            case session_accept:
                return handleSessionAccept(request);
            case session_initiate:
                return handleSessionInitiate(request);
            case session_terminate:
                return handleSessionTerminate(request);
            default:
                throw new AssertionError("Illegal jingle action: " + request.getAction());
        }
    }

    /* ############## Processed in this class ############## */

    /**
     * Handle incoming session-accept stanza.
     * @param request session-accept stanza.
     * @return result.
     */
    private IQ handleSessionAccept(final JingleElement request) {
        this.sessionState = SessionState.active;

        for (final JingleContent content : contents.values()) {
            Async.go(new Runnable() {
                @Override
                public void run() {
                    content.handleSessionAccept(request, jingleManager.getConnection());
                }
            });
        }

        return IQ.createResultIQ(request);
    }

    private IQ handleSessionInitiate(JingleElement request) {
        JingleDescription<?> description = getSoleContentOrThrow().getDescription();
        JingleDescriptionManager descriptionManager = jingleManager.getDescriptionManager(description.getNamespace());

        if (descriptionManager == null) {
            LOGGER.log(Level.WARNING, "Unsupported description type: " + description.getNamespace());
            return JingleElement.createSessionTerminate(getPeer(), getSessionId(), JingleReasonElement.Reason.unsupported_applications);
        }

        descriptionManager.notifySessionInitiate(this);

        return IQ.createResultIQ(request);
    }

    private IQ handleSessionTerminate(JingleElement request) {
        this.sessionState = SessionState.ended;
        JingleReasonElement reason = request.getReason();

        if (reason == null) {
            throw new AssertionError("Reason MUST not be null! (I guess)...");
        }

        //TODO: Inform client.
        jingleManager.removeSession(this);

        return IQ.createResultIQ(request);
    }

    private IQ handleContentAccept(final JingleElement request) {
        for (JingleContentElement a : request.getContents()) {
            final JingleContent accepted = proposedContents.get(a.getName());

            if (accepted == null) {
                throw new AssertionError("Illegal content name!");
            }

            proposedContents.remove(accepted.getName());
            contents.put(accepted.getName(), accepted);

            Async.go(new Runnable() {
                @Override
                public void run() {
                    accepted.handleContentAccept(request, jingleManager.getConnection());
                }
            });
        }

        return IQ.createResultIQ(request);
    }

    private IQ handleContentAdd(JingleElement request) {
        final JingleContent proposed = getSoleProposedContentOrThrow(request);

        final JingleDescriptionManager descriptionManager = jingleManager.getDescriptionManager(proposed.getDescription().getNamespace());

        if (descriptionManager == null) {
            throw new AssertionError("DescriptionManager is null: " + proposed.getDescription().getNamespace());
        }

        Async.go(new Runnable() {
            @Override
            public void run() {
                descriptionManager.notifyContentAdd(JingleSession.this, proposed);
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleContentReject(JingleElement request) {
        for (JingleContentElement r : request.getContents()) {
            final JingleContent rejected = proposedContents.get(r.getName());

            if (rejected == null) {
                throw new AssertionError("Illegal content name!");
            }

            proposedContents.remove(rejected.getName());

            /*
            Async.go(new Runnable() {
                @Override
                public void run() {
                    rejected.handleContentReject(request, jingleManager.getConnection());
                }
            });
            */
        }

        return IQ.createResultIQ(request);
    }

    private IQ handleContentRemove(final JingleElement request) {
        for (JingleContentElement r : request.getContents()) {
            final JingleContent removed = contents.get(r.getName());

            if (removed == null) {
                throw new AssertionError("Illegal content name!");
            }

            contents.remove(removed.getName());

            Async.go(new Runnable() {
                @Override
                public void run() {
                    removed.handleContentRemove(JingleSession.this, jingleManager.getConnection());
                }
            });
        }

        return IQ.createResultIQ(request);
    }

    /* ############## Processed further down ############## */

    private IQ handleContentModify(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);

        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleContentModify(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleDescriptionInfo(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);

        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleDescriptionInfo(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleSecurityInfo(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleSecurityInfo(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleSessionInfo(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleSessionInfo(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleTransportAccept(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleTransportAccept(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleTransportInfo(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleTransportInfo(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleTransportReject(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleTransportReject(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    private IQ handleTransportReplace(final JingleElement request) {
        final JingleContent content = getSoleAffectedContentOrThrow(request);
        Async.go(new Runnable() {
            @Override
            public void run() {
                content.handleTransportReplace(request, jingleManager.getConnection());
            }
        });

        return IQ.createResultIQ(request);
    }

    /* ################ Other getters and setters ############### */

    public FullJid getInitiator() {
        return initiator;
    }

    public FullJid getResponder() {
        return responder;
    }

    public FullJid getPeer() {
        return role == Role.initiator ? responder : initiator;
    }

    public FullJid getOurJid() {
        return role == Role.initiator ? initiator : responder;
    }

    public boolean isInitiator() {
        return role == Role.initiator;
    }

    public boolean isResponder() {
        return role == Role.responder;
    }

    public String getSessionId() {
        return sessionId;
    }

    public JingleManager getJingleManager() {
        return jingleManager;
    }

    private HashMap<JingleContentElement, JingleContent> getAffectedContents(JingleElement request) {
        HashMap<JingleContentElement, JingleContent> map = new HashMap<>();
        for (JingleContentElement e : request.getContents()) {
            JingleContent c = contents.get(e.getName());
            if (c == null) {
                throw new AssertionError("Unknown content: " + e.getName());
            }
            map.put(e, c);
        }
        return map;
    }

    private JingleContent getSoleAffectedContentOrThrow(JingleElement request) {
        if (request.getContents().size() != 1) {
            throw new AssertionError("More/less than 1 content in request!");
        }

        JingleContent content = contents.get(request.getContents().get(0).getName());
        if (content == null) {
            throw new AssertionError("Illegal content name!");
        }

        return content;
    }

    private JingleContent getSoleProposedContentOrThrow(JingleElement request) {
        if (request.getContents().size() != 1) {
            throw new AssertionError("More/less than 1 content in request!");
        }

        return JingleContent.fromElement(request.getContents().get(0));
    }

    public void addContent(JingleContent content) {
        contents.put(content.getName(), content);
        content.setParent(this);
    }

    public void addContent(JingleContentElement content, JingleManager manager)
            throws UnsupportedSecurityException, UnsupportedTransportException, UnsupportedDescriptionException {
        addContent(JingleContent.fromElement(content));
    }

    public ConcurrentHashMap<String, JingleContent> getContents() {
        return contents;
    }

    public JingleContent getContent(String name) {
        return contents.get(name);
    }

    public JingleContent getSoleContentOrThrow() {
        if (contents.isEmpty()) {
            return null;
        }

        if (contents.size() > 1) {
            throw new IllegalStateException();
        }

        return contents.values().iterator().next();
    }

    public SessionState getSessionState() {
        return sessionState;
    }
}