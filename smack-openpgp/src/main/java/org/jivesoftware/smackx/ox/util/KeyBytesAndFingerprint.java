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
package org.jivesoftware.smackx.ox.util;

import java.util.Arrays;

import org.jivesoftware.smackx.ox.OpenPgpV4Fingerprint;

public class KeyBytesAndFingerprint {

    private final byte[] bytes;
    private final OpenPgpV4Fingerprint fingerprint;

    public KeyBytesAndFingerprint(byte[] bytes, OpenPgpV4Fingerprint fingerprint) {
        this.bytes = bytes;
        this.fingerprint = fingerprint;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public OpenPgpV4Fingerprint getFingerprint() {
        return fingerprint;
    }
}
