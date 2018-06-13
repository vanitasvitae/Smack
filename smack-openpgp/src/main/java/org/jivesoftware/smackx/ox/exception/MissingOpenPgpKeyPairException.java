/**
 *
 * Copyright 2018 Paul Schaub.
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
package org.jivesoftware.smackx.ox.exception;

import org.jxmpp.jid.BareJid;

/**
 * Exception that gets thrown whenever an operation is missing an OpenPGP key pair.
 */
public class MissingOpenPgpKeyPairException extends Exception {

    private static final long serialVersionUID = 1L;

    private final BareJid owner;

    /**
     * Create a new {@link MissingOpenPgpKeyPairException}.
     *
     * @param owner owner of the missing key pair.
     * @param e
     */
    public MissingOpenPgpKeyPairException(BareJid owner, Throwable e) {
        super("Missing OpenPGP key pair for user " + owner);
        this.owner = owner;
    }

    /**
     * Return the owner of the missing OpenPGP key pair.
     *
     * @return owner
     */
    public BareJid getOwner() {
        return owner;
    }
}
