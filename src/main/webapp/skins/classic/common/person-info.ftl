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
<#if tipsLabel??>
<div class="module">
    <div class="module-header"><h2>${smallTipLabel}</h2></div>
    <div class="module-panel">
        <ul class="module-list small-tips">
            <li>
                <span class="ft-gray">${tipsLabel}</a></span>
            </li>
        </ul>
    </div>
</div>
</#if>

<#if isLoggedIn>
<div class="module person-info" data-percent="${liveness}">
    <div class="module-panel tooltipped tooltipped-s" aria-label="${todayActivityLabel} ${liveness}%">
        <ul class="status fn-flex">
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/tags'">
                <strong>${currentUser.followingTagCnt?c}</strong>
                <span class="ft-gray">${followingTagsLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/users'">
                <strong>${currentUser.followingUserCnt?c}</strong>
                <span class="ft-gray">${followingUsersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/articles'">
                <strong>${currentUser.followingArticleCnt?c}</strong>
                <span class="ft-gray">${followingArticlesLabel}</span>
            </li>
        </ul>

        <div class="fn-clear">
            <span>♠</span> <a href="${servePath}/top/balance">${wealthRankLabel}</a>
            <span class="ft-red">♥</span> <a href="${servePath}/top/consumption">${consumptionRankLabel}</a>

            <div class="fn-right">
                <#if !isDailyCheckin>
                    <a class="ft-gray" href="<#if useCaptchaCheckin>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${dailyCheckinLabel}</a>
                <#else>
                    <a class="tooltipped tooltipped-w ft-fade" aria-label="${checkinStreakLabel}/${checkinStreakPart0Label}" href="${servePath}/top/checkin">
                    ${currentUser.userCurrentCheckinStreak}/<span class="ft-gray">${currentUser.userLongestCheckinStreak}</span>
                    </a>
                </#if>

                <a href="${servePath}/member/${currentUser.userName}/points" class="tooltipped tooltipped-w ft-fade"
                   aria-label="${pointLabel} ${currentUser.userPoint?c}">
                    <#if 0 == currentUser.userAppRole>0x${currentUser.userPointHex}<#else><div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div></#if></a>
            </div>
        </div>
    </div> 
    <div class="top-left activity-board"></div>
    <div class="top-right activity-board"></div>
    <div class="right activity-board"></div>
    <div class="bottom activity-board"></div>
    <div class="left activity-board"></div>
</div>
</#if>