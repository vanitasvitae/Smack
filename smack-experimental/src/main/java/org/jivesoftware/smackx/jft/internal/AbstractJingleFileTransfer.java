package org.jivesoftware.smackx.jft.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smackx.jft.controller.JingleFileTransferController;
import org.jivesoftware.smackx.jft.element.JingleFileTransferElement;
import org.jivesoftware.smackx.jft.listener.ProgressListener;
import org.jivesoftware.smackx.jingle.components.JingleDescription;

/**
 * Created by vanitas on 22.07.17.
 */
public abstract class AbstractJingleFileTransfer extends JingleDescription<JingleFileTransferElement> implements JingleFileTransferController {

    public static final String NAMESPACE_V5 = "urn:xmpp:jingle:apps:file-transfer:5";
    public static final String NAMESPACE = NAMESPACE_V5;

    public abstract boolean isOffer();
    public abstract boolean isRequest();

    protected State state;

    protected final List<ProgressListener> progressListeners = Collections.synchronizedList(new ArrayList<ProgressListener>());

    @Override
    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
        //TODO: Notify new listener?
    }

    @Override
    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    public void notifyProgressListeners(float progress) {
        for (ProgressListener p : progressListeners) {
            p.progress(progress);
        }
    }

    @Override
    public State getState() {
        return state;
    }
}
