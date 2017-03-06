<#if isLoggedIn>
<div class="module">
    <div class="module-header nopanel">
        <div class="person-info">
            <div class="fn-clear">
                <div class="fn-right">
                    <a href="/settings">
                        <img class="avatar" title="${currentUser.userName}" src="${currentUser.userAvatarURL}" />
                    </a>
                </div>
                <div class="fn-left">
                    <div class="fn-hr5"></div>
                    <a href="/member/${currentUser.userName}">${currentUser.userName}</a>  &nbsp;
                    <a href="/member/${currentUser.userName}/points" class="ft-small" title="${pointLabel} ${currentUser.userPoint?c}">
                        <#if 0 == currentUser.userAppRole>
                        0x${currentUser.userPointHex}
                        <#else>
                        <div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div>
                        </#if>
                    </a><br/>
                    <#if !isDailyCheckin>
                    <a class="btn small red" href="/activity/daily-checkin">${dailyCheckinLabel}</a>
                    <#else>
                    <span class="ft-small">
                        ${checkinStreakLabel}
                        ${currentUser.userCurrentCheckinStreak}/<span class="ft-red">${currentUser.userLongestCheckinStreak}</span>
                        ${checkinStreakPart2Label}
                    </span>
                    </#if>
                </div>
            </div>
            <div>
                ${currentUser.userIntro}
            </div>
        </div>

        <ul class="status fn-flex">
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/tags'">
                <strong>${currentUser.followingTagCnt}</strong>
                <span class="ft-small">${followingTagsLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/users'">
                <strong>${currentUser.followingUserCnt}</strong>
                <span class="ft-small">${followingUsersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '${servePath}/member/${currentUser.userName}/following/articles'">
                <strong>${currentUser.followingArticleCnt}</strong>
                <span class="ft-small">${followingArticlesLabel}</span>
            </li>
        </ul>
    </div> 
</div>
</#if>