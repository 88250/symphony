<#include "macro-admin.ftl">
<@admin "invitecodes">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${invitecode.oId}" readonly="readonly" />

            <label for="code">Code</label>
            <input type="text" id="code" name="code" value="${invitecode.code}" readonly="readonly" />
            
            <label for="userId">${invitecodeUserLabel}</label>
            <input type="text" id="userId" name="userId" value="${invitecode.userId}" readonly="readonly" />
            
            <label for="useTime">${useTimeLabel}</label>
            <input type="text" id="useTime" name="useTime" value="${invitecode.useTime}" readonly="readonly" />
        </div>
    </div>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/invitecode/${invitecode.oId}" method="POST">
                <label>${statusLabel}</label>
                <select id="status" name="status">
                    <option value="0"<#if 0 == invitecode.status> selected</#if>>${usedLabel}</option>
                    <option value="1"<#if 1 == invitecode.status> selected</#if>>${unusedLabel}</option>
                    <option value="2"<#if 2 == invitecode.status> selected</#if>>${stopUseLabel}</option>
                </select>

                <label for="memo">${memoLabel}</label>
                <input type="text" id="memo" name="memo" value="${invitecode.memo}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>