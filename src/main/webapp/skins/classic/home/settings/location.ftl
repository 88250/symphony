<#include "macro-settings.ftl">
<@home "location">
<div class="module">
    <div class="module-header">
        <h2>${geoInfoTipLabel}</h2>
    </div>

    <div class="module-panel form fn-clear">
        <br/>
        <input id="cityName" type="text" placeholder="${geoInfoPlaceholderLabel}" value="${user.userCity}" 
               readonly="readonly"/>
        <br/><br/>
        <select id="geoStatus" onchange="Settings.changeGeoStatus('${csrfToken}')">
            <option name="public" value="0" <#if 0 == user.userGeoStatus>selected</#if>>${publicLabel}</option>
            <option name="private" value="1" <#if 1 == user.userGeoStatus>selected</#if>>${privateLabel}</option>
        </select>
    </div>
</div>
</@home>