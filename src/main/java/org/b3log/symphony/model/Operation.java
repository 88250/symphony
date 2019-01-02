/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.model;

import org.b3log.latke.Keys;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.util.Headers;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * This class defines all operation model relevant keys. https://github.com/b3log/symphony/issues/786
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 19, 2018
 * @since 3.4.4
 */
public final class Operation {

    /**
     * Operation.
     */
    public static final String OPERATION = "operation";

    /**
     * Operations.
     */
    public static final String OPERATIONS = "operations";

    /**
     * Key of operation user id.
     */
    public static final String OPERATION_USER_ID = "operationUserId";

    /**
     * Key of operation code.
     */
    public static final String OPERATION_CODE = "operationCode";

    /**
     * Key of operation data id.
     */
    public static final String OPERATION_DATA_ID = "operationDataId";

    /**
     * Key of operation created at.
     */
    public static final String OPERATION_CREATED = "operationCreated";

    /**
     * Key of operation IP.
     */
    public static final String OPERATION_IP = "operationIP";

    /**
     * Key of operation UA.
     */
    public static final String OPERATION_UA = "operationUA";

    // Code constants
    /**
     * Operation code - make report ignored.
     */
    public static final int OPERATION_CODE_C_MAKE_REPORT_IGNORED = 0;

    /**
     * Operation code - make report handled.
     */
    public static final int OPERATION_CODE_C_MAKE_REPORT_HANDLED = 1;

    /**
     * Operation code - update user address.
     */
    public static final int OPERATION_CODE_C_UPDATE_USER_ADDR = 2;

    /**
     * Operation code - remove role.
     */
    public static final int OPERATION_CODE_C_REMOVE_ROLE = 3;

    /**
     * Operation code - change article email push order.
     */
    public static final int OPERATION_CODE_C_CHANGE_ARTICLE_EMAIL_PUSH_ORDER = 4;

    /**
     * Operation code - update breezemoon.
     */
    public static final int OPERATION_CODE_C_UPDATE_BREEZEMOON = 5;

    /**
     * Operation code - remove breezemoon.
     */
    public static final int OPERATION_CODE_C_REMOVE_BREEZEMOON = 6;

    /**
     * Operation code - push telegram.
     */
    public static final int OPERATION_CODE_C_PUSH_TELEGRAM = 7;

    /**
     * Operation code - withdraw B3T.
     */
    public static final int OPERATION_CODE_C_WITHDRAW_B3T = 8;

    /**
     * Operation code - withdraw B3T.
     */
    public static final int OPERATION_CODE_C_REMOVE_UNUSED_TAGS = 9;

    /**
     * Operation code - add role.
     */
    public static final int OPERAIONT_CODE_C_ADD_ROLE = 10;

    /**
     * Operation code - update role permissions.
     */
    public static final int OPERATION_CODE_C_UPDATE_ROLE_PERMS = 11;

    /**
     * Operation code - add ad pos.
     */
    public static final int OPERATION_CODE_C_ADD_AD_POS = 12;

    /**
     * Operation code - update ad pos.
     */
    public static final int OPERATION_CODE_C_UPDATE_AD_POS = 13;

    /**
     * Operation code - add tag.
     */
    public static final int OPERATION_CODE_C_ADD_TAG = 14;

    /**
     * Operation code - stick article.
     */
    public static final int OPERATION_CODE_C_STICK_ARTICLE = 15;

    /**
     * Operation code - cancel stick article.
     */
    public static final int OPERATION_CODE_C_CANCEL_STICK_ARTICLE = 16;

    /**
     * Operation code - generate invitecodes.
     */
    public static final int OPERATION_CODE_C_GENERATE_INVITECODES = 17;

    /**
     * Operation code - update invitecode.
     */
    public static final int OPERATION_CODE_C_UPDATE_INVITECODE = 18;

    /**
     * Operation code - add article.
     */
    public static final int OPERATION_CODE_C_ADD_ARTICLE = 19;

    /**
     * Operation code - add reserved word.
     */
    public static final int OPERATION_CODE_C_ADD_RESERVED_WORD = 20;

    /**
     * Operation code - update reserved word.
     */
    public static final int OPERATION_CODE_C_UPDATE_RESERVED_WORD = 21;

    /**
     * Operation code - remove reserved word.
     */
    public static final int OPERATION_CODE_C_REMOVE_RESERVED_WORD = 22;

    /**
     * Operation code - remove comment.
     */
    public static final int OPERATION_CODE_C_REMOVE_COMMENT = 23;

    /**
     * Operation code - remove article.
     */
    public static final int OPERATION_CODE_C_REMOVE_ARTICLE = 24;

    /**
     * Operation code - add user.
     */
    public static final int OPERATION_CODE_C_ADD_USER = 25;

    /**
     * Operation code - update user.
     */
    public static final int OPERATION_CODE_C_UPDATE_USER = 26;

    /**
     * Operation code - update user email.
     */
    public static final int OPERATION_CODE_C_UPDATE_USER_EMAIL = 27;

    /**
     * Operation code - update user username.
     */
    public static final int OPERATION_CODE_C_UPDATE_USER_NAME = 28;

    /**
     * Operation code - charge point.
     */
    public static final int OPERATION_CODE_C_CHARGE_POINT = 29;

    /**
     * Operation code - deduct point.
     */
    public static final int OPERATION_CODE_C_DEDUCT_POINT = 30;

    /**
     * Operation code - init point.
     */
    public static final int OPERATION_CODE_C_INIT_POINT = 31;

    /**
     * Operation code - exchange point.
     */
    public static final int OPERATION_CODE_C_EXCHANGE_POINT = 32;

    /**
     * Operation code - update article.
     */
    public static final int OPERATION_CODE_C_UPDATE_ARTICLE = 33;

    /**
     * Operation code - update comment.
     */
    public static final int OPERATION_CODE_C_UPDATE_COMMENT = 34;

    /**
     * Operation code - update misc.
     */
    public static final int OPERATION_CODE_C_UPDATE_MISC = 35;

    /**
     * Operation code - update tag.
     */
    public static final int OPERATION_CODE_C_UPDATE_TAG = 36;

    /**
     * Operation code - update domain.
     */
    public static final int OPERATION_CODE_C_UPDATE_DOMAIN = 37;

    /**
     * Operation code - add domain.
     */
    public static final int OPERATION_CODE_C_ADD_DOMAIN = 38;

    /**
     * Operation code - remove domain.
     */
    public static final int OPERATION_CODE_C_REMOVE_DOMAIN = 39;

    /**
     * Operation code - add domain tag.
     */
    public static final int OPERATION_CODE_C_ADD_DOMAIN_TAG = 40;

    /**
     * Operation code - remove domain tag.
     */
    public static final int OPERATION_CODE_C_REMOVE_DOMAIN_TAG = 41;

    /**
     * Operation code - rebuild algolia tag.
     */
    public static final int OPERATION_CODE_C_REBUILD_ALGOLIA_TAG = 42;

    /**
     * Operation code - rebuild algolia user.
     */
    public static final int OPERATION_CODE_C_REBUILD_ALGOLIA_USER = 43;

    /**
     * Operation code - rebuild articles search index.
     */
    public static final int OPERATION_CODE_C_REBUILD_ARTICLES_SEARCH = 44;

    /**
     * Operation code - rebuild article search index.
     */
    public static final int OPERATION_CODE_C_REBUILD_ARTICLE_SEARCH = 45;

    //// Transient ////

    /**
     * Key of operation user name.
     */
    public static final String OPERATION_T_USER_NAME = "operationUserName";

    /**
     * Key of operation content.
     */
    public static final String OPERATION_T_CONTENT = "operationContent";

    /**
     * Key of operation time.
     */
    public static final String OPERATION_T_TIME = "operationTime";

    /**
     * Creates an operation with the specified request and code,
     *
     * @param request the specified request
     * @param code    the specified code
     * @param dataId  the specified data id
     * @return an operation
     */
    public static JSONObject newOperation(final HttpServletRequest request, final int code, final String dataId) {
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);

        return new JSONObject().
                put(Operation.OPERATION_USER_ID, user.optString(Keys.OBJECT_ID)).
                put(Operation.OPERATION_CREATED, System.currentTimeMillis()).
                put(Operation.OPERATION_CODE, code).
                put(Operation.OPERATION_DATA_ID, dataId).
                put(Operation.OPERATION_IP, ip).
                put(Operation.OPERATION_UA, ua);
    }
}
