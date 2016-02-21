<#if isLoggedIn>
<div class="module">
    <div class="module-header nopanel">
        <div class="person-info">
            <div class="fn-clear">
                <div class="fn-right">
                    <a href="/settings">
                        <div class="avatar" 
                             title="${userName}" style="background-image:url('${currentUser.userAvatarURL}-64.jpg?${currentUser.userUpdateTime?c}')"></div>
                    </a>
                </div>
                <div class="fn-left">
                    <div class="fn-hr5"></div>
                    <a href="/member/${currentUser.userName}">${userName}</a>&nbsp;
                    <a href="/member/${currentUser.userName}/points" class="ft-gray" title="${pointLabel} ${currentUser.userPoint?c}">
                        <#if 0 == currentUser.userAppRole>0x${currentUser.userPointHex}<#else><div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div></#if></a>&nbsp;
                    <a class="btn small red" href="/post?tags=Q%26A&type=0">${IHaveAQuestionLabel}</a>
                    <br/>
                    <#if !isDailyCheckin>
                    <a class="btn small red" href="/activity/daily-checkin">${activityDailyCheckinLabel}</a>
                    <#else>
                    <span class="ft-gray">
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
    </div> 
</div>
</#if>