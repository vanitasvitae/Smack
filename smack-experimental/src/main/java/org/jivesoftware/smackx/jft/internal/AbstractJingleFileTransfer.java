package org.jivesoftware.smackx.jft.internal;

import org.jivesoftware.smackx.jingle.components.JingleDescription;
import org.jivesoftware.smackx.jft.element.JingleFileTransferElement;

/**
 * Created by vanitas on 22.07.17.
 */
public abstract class AbstractJingleFileTransfer extends JingleDescription<JingleFileTransferElement> {

    public static final String NAMESPACE_V5 = "urn:xmpp:jingle:apps:file-transfer:5";
    public static final String NAMESPACE = NAMESPACE_V5;

    public abstract boolean isOffer();
    public abstract boolean isRequest();

}
