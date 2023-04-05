/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
package org.b3log.symphony.processor;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.util.Symphonys;

public class UnusedInviteCodeStatus implements InviteCodeStatus{
    private long objectId;

    LangPropsService langPropsService =new LangPropsService();

    public UnusedInviteCodeStatus(long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String getMessage() {
        String msg = langPropsService.get("invitecodeOkLabel");
        msg = msg.replace("${time}", DateFormatUtils.format(objectId + Symphonys.INVITECODE_EXPIRED, "yyyy-MM-dd HH:mm"));
        return msg;
    }
}
