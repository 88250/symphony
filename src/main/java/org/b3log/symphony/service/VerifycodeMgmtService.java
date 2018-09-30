/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Verifycode;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.repository.VerifycodeRepository;
import org.b3log.symphony.util.Mails;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Verifycode management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.1, Jun 12, 2018
 * @since 1.3.0
 */
@Service
public class VerifycodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VerifycodeMgmtService.class);

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
     * Removes a verifycode with the specified code.
     *
     * @param code the specified code
     */
    @Transactional
    public void removeByCode(final String code) {
        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.CODE, FilterOperator.EQUAL, code));
        try {
            final JSONArray results = verifycodeRepository.get(query).optJSONArray(Keys.RESULTS);
            if (1 > results.length()) {
                return;
            }

            verifycodeRepository.remove(results.optJSONObject(0).optString(Keys.OBJECT_ID));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes by code [" + code + "] failed", e);
        }
    }

    /**
     * Adds a verifycode with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "userId"; "",
     *                          "type": int,
     *                          "bizType": int,
     *                          "receiver": "",
     *                          "code": "",
     *                          "status": int,
     *                          "expired": long
     *                          }
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
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Verifycode.TYPE, FilterOperator.EQUAL, Verifycode.TYPE_C_EMAIL));
        filters.add(new PropertyFilter(Verifycode.STATUS, FilterOperator.EQUAL, Verifycode.STATUS_C_UNSENT));
        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = verifycodeRepository.get(query);
            final JSONArray verifycodes = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < verifycodes.length(); i++) {
                final JSONObject verifycode = verifycodes.optJSONObject(i);

                final String userId = verifycode.optString(Verifycode.USER_ID);
                final JSONObject user = userRepository.get(userId);
                if (null == user) {
                    continue;
                }

                final Map<String, Object> dataModel = new HashMap<>();

                final String userName = user.optString(User.USER_NAME);
                dataModel.put(User.USER_NAME, userName);

                final String toMail = verifycode.optString(Verifycode.RECEIVER);
                final String code = verifycode.optString(Verifycode.CODE);
                String subject;

                final int bizType = verifycode.optInt(Verifycode.BIZ_TYPE);
                switch (bizType) {
                    case Verifycode.BIZ_TYPE_C_REGISTER:
                        dataModel.put(Common.URL, Latkes.getServePath() + "/register?code=" + code);
                        subject = langPropsService.get("registerEmailSubjectLabel", Latkes.getLocale());

                        break;
                    case Verifycode.BIZ_TYPE_C_RESET_PWD:
                        dataModel.put(Common.URL, Latkes.getServePath() + "/reset-pwd?code=" + code);
                        subject = langPropsService.get("forgetEmailSubjectLabel", Latkes.getLocale());

                        break;
                    case Verifycode.BIZ_TYPE_C_BIND_EMAIL:
                        dataModel.put(Common.CODE, code);
                        subject = langPropsService.get("bindEmailSubjectLabel", Latkes.getLocale());

                        break;
                    default:
                        LOGGER.warn("Send email verify code failed with wrong biz type [" + bizType + "]");

                        continue;
                }

                verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_SENT);
                verifycodeRepository.update(verifycode.optString(Keys.OBJECT_ID), verifycode);

                final String fromName = langPropsService.get("symphonyEnLabel")
                        + " " + langPropsService.get("verifycodeEmailFromNameLabel", Latkes.getLocale());
                Mails.sendHTML(fromName, subject, toMail, Mails.TEMPLATE_NAME_VERIFYCODE, dataModel);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Sends verifycode failed", e);
        }
    }
}
