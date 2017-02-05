<#include "macro-settings.ftl">
<@home "function">
<div class="module">
    <div class="module-header">${functionTipLabel}</div>
    <div class="module-panel form fn-clear">
        <label>${userListPageSizeLabel}</label>
        <input id="userListPageSize" type="number" value="${currentUser.userListPageSize}" /> 
        <label>${cmtViewModeLabel}</label>
        <select id="userCommentViewMode" name="userCommentViewMode">
            <option value="0"<#if 0 == currentUser.userCommentViewMode> selected</#if>>${traditionLabel}</option>
            <option value="1"<#if 1 == currentUser.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
        </select>
        <label>${avatarViewModeLabel}</label>
        <select id="userAvatarViewMode" name="userAvatarViewMode">
            <option value="0"<#if 0 == currentUser.userAvatarViewMode> selected</#if>>${orgImgLabel}</option>
            <option value="1"<#if 1 == currentUser.userAvatarViewMode> selected</#if>>${staticImgLabel}</option>
        </select>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${useNotifyLabel} 
                    <input id="userNotifyStatus" <#if 0 == currentUser.userNotifyStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
            <div>
                <label>
                    ${subMailLabel} 
                    <input id="userSubMailStatus" <#if 0 == currentUser.userSubMailStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${enableKbdLabel}
                    <input id="userKeyboardShortcutsStatus" <#if 0 == currentUser.userKeyboardShortcutsStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
        </div>
        <div class="fn-clear"></div>
        <div id="functionTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('function', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${setEmotionLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <textarea id="emotionList" rows="3" placeholder="${setEmotionTipLabel}" >${emotions}</textarea>
        <table id="emojiGrid">
            <#list shortLists as shortlist>
            <tr>
                <#list shortlist as emoji>
                    <#if emoji != "endOfEmoji">
                        <td><img alt="${emoji}" src="${servePath}/emoji/graphics/${emoji}.png"></td>
                    <#else>
                        <td colspan="2"><a href="${servePath}/emoji/index.html">${moreLabel}</a></td>
                    </#if>
                </#list>
            </tr>
            </#list>
        </table>
        <br><br>
        <div class="fn-clear"></div>
        <div id="emotionListTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('emotionList', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>
<script>
    Settings.initFunction();
</script>