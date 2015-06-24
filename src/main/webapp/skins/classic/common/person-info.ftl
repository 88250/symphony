<#if isLoggedIn>
<div class="module">
    <div class="module-header nopanel">
        <div class="fn-clear person-info">
            <div class="fn-right">
                <a href="/settings">
                    <img class="avatar" 
                         title="${userName}" src="${currentUser.userThumbnailURL}" />
                </a>
            </div>
            <div class="fn-left">
                Hi, <a href="/member/${currentUser.userName}">${userName}</a> <br/>
                <a href="/add-article" title="${addArticleLabel}${articleLabel}" class="ft-red icon icon-addfile">${addArticleLabel}${articleLabel}</a>
            </div>
        </div>

        <div>
            ${currentUser.userIntro}
        </div>

        <ul class="status fn-clear">
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/tags'">
                <strong>${currentUser.followingTagCnt}</strong>
                <span class="ft-small">${followingTagsLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/users'">
                <strong>${currentUser.followingUserCnt}</strong>
                <span class="ft-small">${followingUsersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/articles'">
                <strong>${currentUser.followingArticleCnt}</strong>
                <span class="ft-small">${followingArticlesLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/followers'">
                <strong>${currentUser.followerUserCnt}</strong>
                <span class="ft-small">${followersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}'">
                <strong>${currentUser.userArticleCount}</strong>
                <span class="ft-small">${articleLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/comments'">
                <strong>${currentUser.userCommentCount}</strong>
                <span class="ft-small">${cmtLabel}</span>
            </li>
        </ul>
    </div> 
</div>
</#if>