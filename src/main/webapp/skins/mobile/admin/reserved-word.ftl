<#include "macro-admin.ftl">
<@admin "reservedWords">
<div class="wrapper">
    <div class="fn-hr10"></div>
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <label for="oId">Id</label>
            <input type="text" id="oId" value="${word.oId}" readonly="readonly" />
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/reserved-word/${word.oId}" method="POST">
                <label for="optionValue">${reservedWordLabel}</label>
                <input type="text" id="optionValue" name="optionValue" value="${word.optionValue}" />

                <br/><br/>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>

    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeLabel}</h2>
        </div>
        <div class="module-panel form fn-clear">
            <form action="/admin/remove-reserved-word" method="POST" onsubmit="return window.confirm('Sure?')">
                <label for="id">Id</label>
                <input type="text" id="id" name="id" value="${word.oId}" readonly="readonly"/>

                <br/><br/>
                <button type="submit" class="green fn-right" >${submitLabel}</button>
            </form>
        </div>
    </div>
</div>
</@admin>