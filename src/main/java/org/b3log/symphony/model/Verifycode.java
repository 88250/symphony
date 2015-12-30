/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
package org.b3log.symphony.model;

/**
 * This class defines all verifycode model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Dec 8, 2015
 * @since 1.3.0
 */
public final class Verifycode {

    /**
     * Verifycode.
     */
    public static final String VERIFYCODE = "verifycode";

    /**
     * Verifycodes.
     */
    public static final String VERIFYCODES = "verifycodes";

    /**
     * Key of user id.
     */
    public static final String USER_ID = "userId";

    /**
     * Key of type.
     */
    public static final String TYPE = "type";

    /**
     * Key of business type.
     */
    public static final String BIZ_TYPE = "bizType";

    /**
     * Key of receiver.
     */
    public static final String RECEIVER = "receiver";

    /**
     * Key of code.
     */
    public static final String CODE = "code";

    /**
     * Key of status.
     */
    public static final String STATUS = "status";

    /**
     * Key of expired.
     */
    public static final String EXPIRED = "expired";

    // Type constants
    /**
     * Type - Email.
     */
    public static final int TYPE_C_EMAIL = 0;

    // Business type constants
    /**
     * Business type - Register.
     */
    public static final int BIZ_TYPE_C_REGISTER = 0;
    
    /**
     * Business type - Reset password.
     */
    public static final int BIZ_TYPE_C_RESET_PWD = 1;

    // Status constants
    /**
     * Status - Unsent.
     */
    public static final int STATUS_C_UNSENT = 0;

    /**
     * Status- Sent.
     */
    public static final int STATUS_C_SENT = 1;

    /**
     * Private constructor.
     */
    private Verifycode() {
    }
}
