<#if isLoggedIn>
<div class="module">
    <div class="module-header nopanel person-info">
        <div class="info">
            <button class="btn red" onclick="window.location = '/post?tags=Q%26A&type=0'">${IHaveAQuestionLabel}</button>&nbsp;
            <#if !isDailyCheckin>
            <button class="btn green" onclick="window.location = '/activity/daily-checkin'">${activityDailyCheckinLabel}</button>
            <#else>
            <span class="ft-gray">
                ${checkinStreakLabel}
                ${currentUser.userCurrentCheckinStreak}/<span class="ft-red">${currentUser.userLongestCheckinStreak}</span>
                ${checkinStreakPart2Label}
            </span>
            </#if>
            <br/><br/>
            <a href="/member/${currentUser.userName}">${userName}</a>
            <a href="/member/${currentUser.userName}/points" class="ft-gray" title="${pointLabel} ${currentUser.userPoint?c}">
                <#if 0 == currentUser.userAppRole>0x${currentUser.userPointHex}<#else><div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div></#if></a>
        </div>

        <ul class="status fn-flex">
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/tags'">
                <strong>${currentUser.followingTagCnt?c}</strong>
                <span class="ft-gray">${followingTagsLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/users'">
                <strong>${currentUser.followingUserCnt?c}</strong>
                <span class="ft-gray">${followingUsersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/articles'">
                <strong>${currentUser.followingArticleCnt?c}</strong>
                <span class="ft-gray">${followingArticlesLabel}</span>
            </li>
        </ul>

        <div class="fn-clear ranking">
            <span class="ft-red">♠</span><a href="/top/balance">${wealthLabel}${rankingLabel}</a>
            <span class="fn-right">
                <span class="ft-blue">♦</span><a href="/top/consumption">${consumptionLabel}${rankingLabel}</a>
            </span>
        </div>
    </div> 
</div>
</#if>