<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content activity">
                    <div class="module">
                        <h2 class="sub-head">${activityLabel}</h2>
                        <div class="list">
                            <ul>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${activityDailyCheckinLabel}" style="background-image:url('${staticServePath}/images/activities/checkin.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="<#if useCaptchaCheckin>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">
                                                每日签到随机获得 <code>[${pointActivityCheckinMin?c}, ${pointActivityCheckinMax?c}]</code>，每连续签到 10 天额外获得 <code>${pointActivityCheckinStreak?c}</code>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${activityYesterdayLivenessRewardLabel}" style="background-image:url('${staticServePath}/images/activities/yesterday.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">
                                                领取昨日 <a href="https://hacpai.com/article/1458624687933" rel="nofollow" class="ft-gray">活跃度奖励</a> ，目前最高可以获得 <code>${activitYesterdayLivenessRewardMaxPoint?c}</code>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${activity1A0001Label}" style="background-image:url('${staticServePath}/images/activities/1A0001.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="${servePath}/activity/1A0001">${activity1A0001Label}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">
                                                下注后，请在当天 16-24 点在本页面进行兑奖，逾期作废！
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${characterLabel}" style="background-image:url('${staticServePath}/images/activities/char.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="${servePath}/activity/character">${characterLabel}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">${activityCharacterTitleLabel}</span>
                                        </div>
                                    </div>
                                </li>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${eatingSnakeLabel}" style="background-image:url('${staticServePath}/images/activities/snak.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="${servePath}/activity/eating-snake">${eatingSnakeLabel}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">
                                                ${activityEatingSnakeTitleLabel}
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li>
                                    <div class='fn-flex'>
                                        <div class="avatar tooltipped tooltipped-ne"
                                             aria-label="${gobangLabel}" style="background-image:url('${staticServePath}/images/activities/gobang.png')"></div>
                                        <div class="fn-flex-1">
                                            <h2>
                                                <a href="${servePath}/activity/gobang">${gobangLabel}</a>
                                            </h2>
                                            <span class="ft-fade content-reset">
                                            ${activityGobangTitleLabel}
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>