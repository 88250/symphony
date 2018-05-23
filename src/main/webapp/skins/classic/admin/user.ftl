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
<#include "macro-admin.ftl">
<@admin "users">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label>Id</label>
            <input type="text" value="${user.oId}" readonly="readonly" />

            <label>${userNameLabel}</label>
            <input type="text" value="${user.userName}" readonly="readonly" />

            <label>${userNoLabel}</label>
            <input type="text" value="${user.userNo?c}" readonly="readonly" />

            <label>${userEmailLabel}</label>
            <input type="text" value="${user.userEmail}" readonly="readonly" />

            <label>${articleCountLabel}</label>
            <input type="text" value="${user.userArticleCount}" readonly="readonly" />

            <label>${commentCountLabel}</label>
            <input type="text" value="${user.userCommentCount}" readonly="readonly" />

            <label>${tagCountLabel}</label>
            <input type="text" value="${user.userTagCount}" readonly="readonly" />

            <label>${pointLabel}</label>
            <input type="text" value="${user.userPoint?c}" readonly="readonly" />

            <label>${countryLabel}</label>
            <input type="text" value="${user.userCountry}" readonly="readonly" />

            <label>${provinceLabel}</label>
            <input type="text" value="${user.userProvince}" readonly="readonly" />

            <label>${cityLabel}</label>
            <input type="text" value="${user.userCity}" readonly="readonly" />

            <label>IP</label>
            <input type="text" value="${user.userLatestLoginIP}" readonly="readonly" />

            <label>${registerTimeLabel}</label>
            <input type="text" value="${user.oId?number?number_to_datetime}" readonly="readonly" />

            <label>${loginTimeLabel}</label>
            <input type="text" value="${user.userLatestLoginTime?number_to_datetime}" readonly="readonly" />

            <label>${commentTimeLabel}</label>
            <input type="text" value="${user.userLatestCmtTime?number_to_datetime}" readonly="readonly" />

            <label>${articleTimeLabel}</label>
            <input type="text" value="${user.userLatestArticleTime?number_to_datetime}" readonly="readonly" />

            <label>${checkinStreakLabel}</label>
            <input type="text" value="${user.userCurrentCheckinStreak}" readonly="readonly" />

            <label>${checkinStreakPart0Label}</label>
            <input type="text" value="${user.userLongestCheckinStreak}" readonly="readonly" />
        </div>
    </div>

    <#if permissions["userUpdateUserBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}" method="POST">
                <label for="userPassword">${passwordLabel}</label>
                <input type="text" id="userPassword" name="userPassword" value="${user.userPassword}" />

                <label for="userNickname">${nicknameLabel}</label>
                <input type="text" id="userNickname" name="userNickname" value="${user.userNickname}" />

                <label for="userTags">${selfTagLabel}</label>
                <input type="text" id="userTags" name="userTags" value="${user.userTags}" />

                <label for="userURL">URL</label>
                <input type="text" id="userURL" name="userURL" value="${user.userURL}" />

                <#--
                <label for="userQQ">QQ</label>
                <input type="text" id="userQQ" name="userQQ" value="${user.userQQ}" />
                -->

                <label for="userIntro">${userIntroLabel}</label>
                <input type="text" id="userIntro" name="userIntro" value="${user.userIntro}" />

                <label for="userIntro">${avatarURLLabel}</label>
                <input type="text" id="userAvatarURL" name="userAvatarURL" value="${user.userAvatarURL}" />

                <label for="userListPageSize">${userListPageSizeLabel}</label>
                <input type="number" id="userListPageSize" name="userListPageSize" value="${user.userListPageSize}" />

                <label>${cmtViewModeLabel}</label>
                <select id="userCommentViewMode" name="userCommentViewMode">
                    <option value="0"<#if 0 == user.userCommentViewMode> selected</#if>>${traditionLabel}</option>
                    <option value="1"<#if 1 == user.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
                </select>

                <label>${avatarViewModeLabel}</label>
                <select id="userAvatarViewMode" name="userAvatarViewMode">
                    <option value="0"<#if 0 == user.userAvatarViewMode> selected</#if>>${orgImgLabel}</option>
                    <option value="1"<#if 1 == user.userAvatarViewMode> selected</#if>>${staticImgLabel}</option>
                </select>

                <label>${useNotifyLabel}</label>
                <select id="userNotifyStatus" name="userNotifyStatus">
                    <option value="0"<#if 0 == user.userNotifyStatus> selected</#if>>${yesLabel}</option>
                    <option value="1"<#if 1 == user.userNotifyStatus> selected</#if>>${noLabel}</option>
                </select>

                <label>${subMailLabel}</label>
                <select id="userSubMailStatus" name="userSubMailStatus">
                    <option value="0"<#if 0 == user.userSubMailStatus> selected</#if>>${yesLabel}</option>
                    <option value="1"<#if 1 == user.userSubMailStatus> selected</#if>>${noLabel}</option>
                </select>

                <label>${enableKbdLabel}</label>
                <select id="userKeyboardShortcutsStatus" name="userKeyboardShortcutsStatus">
                    <option value="0"<#if 0 == user.userKeyboardShortcutsStatus> selected</#if>>${yesLabel}</option>
                    <option value="1"<#if 1 == user.userKeyboardShortcutsStatus> selected</#if>>${noLabel}</option>
                </select>

                <label>${geoLabel}</label>
                <select id="userGeoStatus" name="userGeoStatus">
                    <option value="0"<#if 0 == user.userGeoStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userGeoStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userArticleStatusLabel}</label>
                <select id="userArticleStatus" name="userArticleStatus">
                    <option value="0"<#if 0 == user.userArticleStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userArticleStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userCommentStatusLabel}</label>
                <select id="userCommentStatus" name="userCommentStatus">
                    <option value="0"<#if 0 == user.userCommentStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userCommentStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userFollowingUserStatusLabel}</label>
                <select id="userFollowingUserStatus" name="userFollowingUserStatus">
                    <option value="0"<#if 0 == user.userFollowingUserStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userFollowingUserStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userFollowingTagStatusLabel}</label>
                <select id="userFollowingTagStatus" name="userFollowingTagStatus">
                    <option value="0"<#if 0 == user.userFollowingTagStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userFollowingTagStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userFollowingArticleStatusLabel}</label>
                <select id="userFollowingArticleStatus" name="userFollowingArticleStatus">
                    <option value="0"<#if 0 == user.userFollowingArticleStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userFollowingArticleStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userWatchingArticleStatusLabel}</label>
                <select id="userWatchingArticleStatus" name="userWatchingArticleStatus">
                    <option value="0"<#if 0 == user.userWatchingArticleStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userWatchingArticleStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userFollowerStatusLabel}</label>
                <select id="userFollowerStatus" name="userFollowerStatus">
                    <option value="0"<#if 0 == user.userFollowerStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userFollowerStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userPointStatusLabel}</label>
                <select id="userPointStatus" name="userPointStatus">
                    <option value="0"<#if 0 == user.userPointStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userPointStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${userOnlineStatusLabel}</label>
                <select id="userOnlineStatus" name="userOnlineStatus">
                    <option value="0"<#if 0 == user.userOnlineStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userOnlineStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${displayUALabel}</label>
                <select id="userUAStatus" name="userUAStatus">
                    <option value="0"<#if 0 == user.userUAStatus> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userUAStatus> selected</#if>>${privateLabel}</option>
                </select>

                <label>${joinBalanceRankLabel}</label>
                <select id="userJoinPointRank" name="userJoinPointRank">
                    <option value="0"<#if 0 == user.userJoinPointRank> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userJoinPointRank> selected</#if>>${privateLabel}</option>
                </select>

                <label>${joinCosumptionRankLabel}</label>
                <select id="userJoinUsedPointRank" name="userJoinUsedPointRank">
                    <option value="0"<#if 0 == user.userJoinUsedPointRank> selected</#if>>${publicLabel}</option>
                    <option value="1"<#if 1 == user.userJoinUsedPointRank> selected</#if>>${privateLabel}</option>
                </select>

                <label>${roleLabel}</label>
                <select id="userRole" name="userRole">
                    <#list roles as role>
                        <option value=${role.oId}<#if role.oId == user.userRole> selected</#if>>${role.roleName}</option>
                    </#list>
                </select>

                <label>${appRoleLabel}</label>
                <select id="userAppRole" name="userAppRole">
                    <option value="0"<#if 0 == user.userAppRole> selected</#if>>${hackerLabel}</option>
                    <option value="1"<#if 1 == user.userAppRole> selected</#if>>${painterLabel}</option>
                </select>

                <label>${userStatusLabel}</label>
                <select id="userStatus" name="userStatus">
                    <option value="0"<#if 0 == user.userStatus> selected</#if>>${validLabel}</option>
                    <option value="1"<#if 1 == user.userStatus> selected</#if>>${banLabel}</option>
                    <option value="2"<#if 2 == user.userStatus> selected</#if>>${notVerifiedLabel}</option>
                    <option value="3"<#if 3 == user.userStatus> selected</#if>>${invalidLoginLabel}</option>
                </select>

                <label>${syncWithSymphonyClientLabel}</label>
                <select id="syncWithSymphonyClient" name="syncWithSymphonyClient">
                    <option value="true"<#if user.syncWithSymphonyClient> selected</#if>>${yesLabel}</option>
                    <option value="false"<#if !user.syncWithSymphonyClient> selected</#if>>${noLabel}</option>
                </select>

                <label for="userB3Key">B3 Key</label>
                <input type="text" id="userB3Key" name="userB3Key" value="${user.userB3Key}" />

                <label for="userB3ClientAddArticleURL">${clientArticleLabel}</label>
                <input type="text" id="userB3ClientAddArticleURL" name="userB3ClientAddArticleURL" value="${user.userB3ClientAddArticleURL}" />

                <label for="userB3ClientUpdateArticleURL">${clientUpdateArticleLabel}</label>
                <input type="text" id="userB3ClientUpdateArticleURL" name="userB3ClientUpdateArticleURL" value="${user.userB3ClientUpdateArticleURL}" />

                <label for="userB3ClientAddCommentURL">${clientCmtLabel}</label>
                <input type="text" id="userB3ClientAddCommentURL" name="userB3ClientAddCommentURL" value="${user.userB3ClientAddCommentURL}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["userUpdateUserAdvanced"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${advancedUpdateLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}/email" method="POST">
                <label for="userEmail">${userEmailLabel}</label>
                <input type="text" id="userEmail" name="userEmail" value="${user.userEmail}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
            <br/>

            <form action="${servePath}/admin/user/${user.oId}/username" method="POST">
                <label for="userName">${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["userAddPoint"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${chargePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}/charge-point" method="POST">
                <label>${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" readonly="readonly" />

                <label>${pointLabel}</label>
                <input type="text" name="point" value="" />

                <label>${memoLabel}</label>
                <input type="text" name="memo" value="" placeholder="${chargePointPlaceholderLabel}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["userExchangePoint"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${exchangePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}/exchange-point" method="POST">
                <label>${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" readonly="readonly" />

                <label>${pointLabel}</label>
                <input type="text" name="point" value="" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["userDeductPoint"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${abusePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}/abuse-point" method="POST">
                <label>${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" readonly="readonly" />

                <label>${pointLabel}</label>
                <input type="text" name="point" value="" />

                <label>${memoLabel}</label>
                <input type="text" name="memo" value="" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>

    <div class="module">
        <div class="module-header">
            <h2>${compensateInitPointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="${servePath}/admin/user/${user.oId}/init-point" method="POST">
                <label>${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" readonly="readonly" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>