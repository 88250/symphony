<#include "macro-settings.ftl">
<@home "privacy">
<div class="module">
    <div class="module-header">${privacyTipLabel}</div>
    <div class="module-panel form fn-clear">
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userArticleStatusLabel}
                    <input id="userArticleStatus" <#if 0 == currentUser.userArticleStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userCommentStatusLabel}
                    <input id="userCommentStatus" <#if 0 == currentUser.userCommentStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userFollowingUserStatusLabel}
                    <input id="userFollowingUserStatus" <#if 0 == currentUser.userFollowingUserStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userFollowingTagStatusLabel}
                    <input id="userFollowingTagStatus" <#if 0 == currentUser.userFollowingTagStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userWatchingArticleStatusLabel}
                    <input id="userWatchingArticleStatus" <#if 0 == currentUser.userWatchingArticleStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
            <div>
                <label>
                    ${userFollowingArticleStatusLabel}
                    <input id="userFollowingArticleStatus" <#if 0 == currentUser.userFollowingArticleStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userFollowerStatusLabel}
                    <input id="userFollowerStatus" <#if 0 == currentUser.userFollowerStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
            <div>
                <label>
                    ${userForgeLinkStatusLabel}
                    <input id="userForgeLinkStatus" <#if 0 == currentUser.userForgeLinkStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userPointStatusLabel}
                    <input id="userPointStatus" <#if 0 == currentUser.userPointStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userOnlineStatusLabel}
                    <input id="userOnlineStatus" <#if 0 == currentUser.userOnlineStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${displayUALabel}
                    <input id="userUAStatus" <#if 0 == currentUser.userUAStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userTimelineStatusLabel}
                    <input id="userTimelineStatus" <#if 0 == currentUser.userTimelineStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${joinBalanceRankLabel}
                    <input id="joinPointRank" <#if 0 == currentUser.userJoinPointRank> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${joinCosumptionRankLabel}
                    <input id="joinUsedPointRank" <#if 0 == currentUser.userJoinUsedPointRank> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>

        <div id="privacyTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('privacy', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>