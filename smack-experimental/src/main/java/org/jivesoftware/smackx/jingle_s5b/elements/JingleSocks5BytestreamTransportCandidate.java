package org.jivesoftware.smackx.jingle_s5b.elements;

import java.util.logging.Logger;

import org.jivesoftware.smack.util.Objects;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.jingle.element.JingleContentTransportCandidate;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * TransportCandidate for Jingle Socks5Bytestream transports.
 */
public class JingleSocks5BytestreamTransportCandidate extends JingleContentTransportCandidate {

    private static final Logger LOGGER = Logger.getLogger(JingleSocks5BytestreamTransportCandidate.class.getName());

    public static final String ATTR_CID = "cid";
    public static final String ATTR_HOST = "host";
    public static final String ATTR_JID = "jid";
    public static final String ATTR_PORT = "port";
    public static final String ATTR_PRIORITY = "priority";
    public static final String ATTR_TYPE = "type";

    private final String cid;
    private final String host;
    private final Jid jid;
    private final int port;
    private final int priority;
    private final Type type;

    public JingleSocks5BytestreamTransportCandidate(String candidateId, String host, Jid jid, int port, int priority, Type type) {
        this.cid = candidateId;
        this.host = host;
        this.jid = jid;
        this.port = port;
        this.priority = priority;
        this.type = type;
    }

    public enum Type {
        assisted (120),
        direct (126),
        proxy (10),
        tunnel (110),
        ;

        private final int weight;

        public int getWeight() {
            return weight;
        }

        Type(int weight) {
            this.weight = weight;
        }

        public static Type fromString(String name) {
            for (Type t : Type.values()) {
                if (t.toString().equals(name)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Illegal type: " + name);
        }
    }

    public String getCandidateId() {
        return cid;
    }

    public String getHost() {
        return host;
    }

    public Jid getJid() {
        return jid;
    }

    public int getPort() {
        return port;
    }

    public int getPriority() {
        return priority;
    }

    public Type getType() {
        return type;
    }

    @Override
    public CharSequence toXML() {
        XmlStringBuilder xml = new XmlStringBuilder();
        xml.halfOpenElement(this);
        xml.attribute(ATTR_CID, cid);
        xml.attribute(ATTR_HOST, host);
        xml.attribute(ATTR_JID, jid);
        if (port >= 0) {
            xml.attribute(ATTR_PORT, port);
        }
        xml.attribute(ATTR_PRIORITY, priority);
        xml.optAttribute(ATTR_TYPE, type);

        xml.closeEmptyElement();
        return xml;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String cid;
        private String host;
        private Jid jid;
        private int port = -1;
        private int priority = -1;
        private Type type;

        private Builder() {
        }

        public Builder setCandidateId(String cid) {
            this.cid = cid;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setJid(String jid) throws XmppStringprepException {
            this.jid = JidCreate.from(jid);
            return this;
        }

        public Builder setPort(int port) {
            if (port < 0) {
                throw new IllegalArgumentException("Port MUST NOT be less than 0.");
            }
            this.port = port;
            return this;
        }

        public Builder setPriority(int priority) {
            if (priority < 0) {
                throw new IllegalArgumentException("Priority MUST NOT be less than 0.");
            }
            this.priority = priority;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public JingleSocks5BytestreamTransportCandidate build() {
            Objects.requireNonNull(cid);
            Objects.requireNonNull(host);
            Objects.requireNonNull(jid);
            if (priority < 0) {
                throw new IllegalArgumentException("Priority MUST be present and NOT less than 0.");
            }
            return new JingleSocks5BytestreamTransportCandidate(cid, host, jid, port, priority, type);
        }
    }
}
