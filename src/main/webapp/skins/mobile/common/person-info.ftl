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
<#if isLoggedIn>
<div class="module person-info" data-percent="${liveness}">
    <div class="info fn-clear">
        <button class="btn red" title="${addArticleLabel}" onclick="window.location = '/pre-post'">${postArticleLabel}</button>
        <#if !isDailyCheckin>
        <a class="fn-right" href="<#if useCaptchaCheckin>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${dailyCheckinLabel}</a>
        <#else>
        <a class="ft-gray fn-right" title="${checkinStreakLabel}" href="${servePath}/top/checkin">
            ${currentUser.userCurrentCheckinStreak}/<span class="ft-red">${currentUser.userLongestCheckinStreak}</span>
        </a>
        </#if>
    </div>
    <div class="module-panel">
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

        <div class="fn-clear ranking">
            <span>♠</span> <a href="${servePath}/top/balance">${wealthRankLabel}</a>
            <span class="ft-red">♥</span> <a href="${servePath}/top/consumption">${consumptionRankLabel}</a>
            <a href="${servePath}/member/${currentUser.userName}/points" class="ft-gray fn-right" title="${pointLabel} ${currentUser.userPoint?c}">
                <#if 0 == currentUser.userAppRole>0x${currentUser.userPointHex}<#else><div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div></#if></a>
        </div>
    </div> 
    <div class="top-left activity-board"></div>
    <div class="top-right activity-board"></div>
    <div class="right activity-board"></div>
    <div class="bottom activity-board"></div>
    <div class="left activity-board"></div>
</div>
</#if>