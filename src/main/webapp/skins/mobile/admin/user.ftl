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
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/user/${user.oId}" method="POST">
                <label for="userPassword">${userPasswordLabel}</label>
                <input type="text" id="userPassword" name="userPassword" value="${user.userPassword}" />

                <label for="userURL">URL</label>
                <input type="text" id="userURL" name="userURL" value="${user.userURL}" />

                <label for="userQQ">QQ</label>
                <input type="text" id="userQQ" name="userQQ" value="${user.userQQ}" />

                <label for="userIntro">${userIntroLabel}</label>
                <input type="text" id="userIntro" name="userIntro" value="${user.userIntro}" />

                <label for="userIntro">${avatarURLLabel}</label>
                <input type="text" id="userAvatarURL" name="userAvatarURL" value="${user.userAvatarURL}" />

                <label>${cmtViewModeLabel}</label>
                <select id="userCommentViewMode" name="userCommentViewMode">
                    <option value="0"<#if 0 == user.userCommentViewMode> selected</#if>>${traditionLabel}</option>
                    <option value="1"<#if 1 == user.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
                </select>

                <label>${roleLabel}</label>
                <select id="userRole" name="userRole">
                    <option value="adminRole"<#if "adminRole" == user.userRole> selected</#if>>${administratorLabel}</option>
                    <option value="defaultRole"<#if "defaultRole" == user.userRole> selected</#if>>${defaultUserLabel}</option>
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
                    <option value="1"<#if 2 == user.userStatus> selected</#if>>${notVerifiedLabel}</option>
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

    <div class="module">
        <div class="module-header">
            <h2>${advancedUpdateLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/user/${user.oId}/email" method="POST">
                <label for="userEmail">${userEmailLabel}</label>
                <input type="text" id="userEmail" name="userEmail" value="${user.userEmail}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
            <br/>

            <form action="/admin/user/${user.oId}/username" method="POST">
                <label for="userName">${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${chargePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/user/${user.oId}/charge-point" method="POST">
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

    <div class="module">
        <div class="module-header">
            <h2>${exchangePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/user/${user.oId}/exchange-point" method="POST">
                <label>${userNameLabel}</label>
                <input type="text" name="userName" value="${user.userName}" readonly="readonly" />

                <label>${pointLabel}</label>
                <input type="text" name="point" value="" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${abusePointLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/user/${user.oId}/abuse-point" method="POST">
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
</div>
</@admin>