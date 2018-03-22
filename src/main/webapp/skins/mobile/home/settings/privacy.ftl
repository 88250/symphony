<#include "macro-settings.ftl">
<@home "privacy">
<div class="module">
    <div class="module-header">${privacyTipLabel}</div>
    <div class="module-panel form fn-clear">
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userArticleStatus" <#if 0 == currentUser.userArticleStatus> checked="checked"</#if> type="checkbox" />
                    ${userArticleStatusLabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="userCommentStatus" <#if 0 == currentUser.userCommentStatus> checked="checked"</#if> type="checkbox" />
                    ${userCommentStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userFollowingUserStatus" <#if 0 == currentUser.userFollowingUserStatus> checked="checked"</#if> type="checkbox" />
                    ${userFollowingUserStatusLabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="userFollowingTagStatus" <#if 0 == currentUser.userFollowingTagStatus> checked="checked"</#if> type="checkbox" />
                    ${userFollowingTagStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userFollowingArticleStatus" <#if 0 == currentUser.userFollowingArticleStatus> checked="checked"</#if> type="checkbox" />
                    ${userFollowingArticleStatusLabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="userFollowerStatus" <#if 0 == currentUser.userFollowerStatus> checked="checked"</#if> type="checkbox" />
                    ${userFollowerStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userWatchingArticleStatus" <#if 0 == currentUser.userWatchingArticleStatus> checked="checked"</#if> type="checkbox" />
                    ${userWatchingArticleStatusLabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="userPointStatus" <#if 0 == currentUser.userPointStatus> checked="checked"</#if> type="checkbox" />
                    ${userPointStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userUAStatus" <#if 0 == currentUser.userUAStatus> checked="checked"</#if> type="checkbox" />
                    ${displayUALabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="userOnlineStatus" <#if 0 == currentUser.userOnlineStatus> checked="checked"</#if> type="checkbox" />
                    ${userOnlineStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="joinPointRank" <#if 0 == currentUser.userJoinPointRank> checked="checked"</#if> type="checkbox" />
                    ${joinBalanceRankLabel}
                </label>
            </div>
            <div>
                <label>
                    <input id="joinUsedPointRank" <#if 0 == currentUser.userJoinUsedPointRank> checked="checked"</#if> type="checkbox" />
                    ${joinCosumptionRankLabel}
                </label>
            </div>
        </div>

        <div id="privacyTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('privacy', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>