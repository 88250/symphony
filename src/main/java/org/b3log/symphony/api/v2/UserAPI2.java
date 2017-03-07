package org.b3log.symphony.api.v2;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.AvatarQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * User API v2.
 * <p>
 * <ul>
 * <li>Gets a user (/api/v2/user/{userName}), GET</li>
 * <li>Gets a user's articles (/api/v2/user/{userName}/articles), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 7, 2016
 * @since 2.1.0
 */
@RequestProcessor
public class UserAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserAPI2.class);
    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;
    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Gets a user by the specified username.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param userName the specified username
     */
    @RequestProcessing(value = {"/api/v2/user/{userName}"}, method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void getTags(final HTTPRequestContext context, final HttpServletRequest request,
                        final String userName) {
        final JSONObject ret = new JSONObject();
        context.renderJSONPretty(ret);

        ret.put(Keys.STATUS_CODE, StatusCodes.ERR);
        ret.put(Keys.MSG, "");

        if (UserRegisterValidation.invalidUserName(userName)) {
            ret.put(Keys.MSG, "User not found");
            ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

            return;
        }

        JSONObject data = null;
        try {
            final JSONObject user = userQueryService.getUserByName(userName);
            if (null == user) {
                ret.put(Keys.MSG, "User not found");
                ret.put(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

            data = new JSONObject();
            data.put(User.USER, user);
            V2s.cleanUser(user);

            ret.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            final String msg = "Gets a user failed";

            LOGGER.log(Level.ERROR, msg, e);
            ret.put(Keys.MSG, msg);
        }

        ret.put(Common.DATA, data);
    }
}
