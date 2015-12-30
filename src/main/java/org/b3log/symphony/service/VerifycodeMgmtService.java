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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Verifycode;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.repository.VerifycodeRepository;
import org.b3log.symphony.util.Mails;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Verifycode management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Dec 8, 2015
 * @since 1.3.0
 */
@Service
public class VerifycodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VerifycodeMgmtService.class.getName());

    /**
     * Verifycode repository.
     */
    @Inject
    private VerifycodeRepository verifycodeRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Adds a verifycode with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "type": int,
     *     "bizType": int,
     *     "receiver": "",
     *     "code": "",
     *     "status": int,
     *     "expired": long
     * }
     * </pre>
     *
     * @return verifycode id
     * @throws ServiceException service exception
     */
    @Transactional
    public String addVerifycode(final JSONObject requestJSONObject) throws ServiceException {
        try {
            return verifycodeRepository.add(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds verifycode failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Removes expired verifycodes.
     */
    @Transactional
    public void removeExpiredVerifycodes() {
        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.EXPIRED,
                FilterOperator.LESS_THAN, new Date().getTime()));

        try {
            final JSONObject result = verifycodeRepository.get(query);
            final JSONArray verifycodes = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < verifycodes.length(); i++) {
                final String id = verifycodes.optJSONObject(i).optString(Keys.OBJECT_ID);
                verifycodeRepository.remove(id);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Expires verifycodes failed", e);
        }
    }

    /**
     * Sends email verifycode.
     */
    @Transactional
    public void sendEmailVerifycode() {
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Verifycode.TYPE, FilterOperator.EQUAL, Verifycode.TYPE_C_EMAIL));
        filters.add(new PropertyFilter(Verifycode.STATUS, FilterOperator.EQUAL, Verifycode.STATUS_C_UNSENT));
        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = verifycodeRepository.get(query);
            final JSONArray verifycodes = result.optJSONArray(Keys.RESULTS);

            final Map<String, List<String>> vars = new HashMap<String, List<String>>();
            final List<String> var1 = new ArrayList<String>();
            final List<String> var2 = new ArrayList<String>();
            vars.put("%1%", var1);
            vars.put("%2%", var2);
            final List<String> toMails = new ArrayList<String>();

            for (int i = 0; i < verifycodes.length(); i++) {
                final JSONObject verifycode = verifycodes.optJSONObject(i);

                final String userId = verifycode.optString(Verifycode.USER_ID);
                final JSONObject user = userRepository.get(userId);
                if (null == user) {
                    continue;
                }

                final String userName = user.optString(User.USER_NAME);
                final String toMail = verifycode.optString(Verifycode.RECEIVER);
                final String code = verifycode.optString(Verifycode.CODE);

                var1.add(userName);

                final int bizType = verifycode.optInt(Verifycode.BIZ_TYPE);
                switch (bizType) {
                    case Verifycode.BIZ_TYPE_C_REGISTER:
                        var2.add(Latkes.getServePath() + "/register?code=" + code);

                        break;
                    case Verifycode.BIZ_TYPE_C_RESET_PWD:
                        var2.add(Latkes.getServePath() + "/reset-pwd?code=" + code);

                        break;
                    default:
                        LOGGER.warn("Send email verify code failed with wrong biz type [" + bizType + "]");

                        continue;
                }

                toMails.add(toMail);

                verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_SENT);
                verifycodeRepository.update(verifycode.optString(Keys.OBJECT_ID), verifycode);
            }

            if (0 != verifycodes.length()) {
                Mails.send(langPropsService.get("verifycodeEmailSubjectLabel"), "sym_register", toMails, vars);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Sends verifycode failed", e);
        }
    }
}
