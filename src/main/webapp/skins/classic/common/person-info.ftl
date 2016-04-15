<#if isLoggedIn>
<div class="module person-info" data-percent="${liveness}">
    <div class="info fn-clear">
        <button class="btn red" title="${addArticleLabel}" onclick="window.location = '/pre-post'">${postArticleLabel}</button>
        <#if !isDailyCheckin>
        <a class="fn-right" href="<#if useCaptchaCheckin>/activity/checkin<#else>/activity/daily-checkin</#if>">${dailyCheckinLabel}</a>
        <#else>
        <a class="ft-gray fn-right" title="${checkinStreakLabel}" href="/top/checkin">
            ${currentUser.userCurrentCheckinStreak}/<span class="ft-red">${currentUser.userLongestCheckinStreak}</span>
        </a>
        </#if>
    </div>
    <div class="module-panel">
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
            <span class="ft-red">♠</span> <a href="/top/balance">${wealthLabel}${rankingLabel}</a>
            <span class="ft-green">♥</span> <a href="/top/consumption">${consumptionLabel}${rankingLabel}</a>
            <a href="/member/${currentUser.userName}/points" class="ft-gray fn-right" title="${pointLabel} ${currentUser.userPoint?c}">
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