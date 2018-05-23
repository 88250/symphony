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
                    <input id="userWatchingArticleStatus" <#if 0 == currentUser.userWatchingArticleStatus> checked="checked"</#if> type="checkbox" />
                    ${userWatchingArticleStatusLabel}
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    <input id="userFollowerStatus" <#if 0 == currentUser.userFollowerStatus> checked="checked"</#if> type="checkbox" />
                    ${userFollowerStatusLabel}
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
                    <input id="userBreezemoonStatus" <#if 0 == currentUser.userBreezemoonStatus> checked="checked"</#if> type="checkbox" />
                    ${userBreezemoonStatusLabel}
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
        <button class="fn-right" onclick="Settings.update('privacy', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>