<#include "macro-settings.ftl">
<@home "help">

<div class="module">
    <div class="module-header">
        <h2>${userGuideLabel}</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <li>
                <a href="${servePath}/about">${getStartLabel}</a>
                <span class="ft-gray">${getStartTipLabel}</span>
            </li>
            <li>
                <a href="${servePath}/tag/user_guide">${basicLabel}</a>
                <span class="ft-gray">${basicTipLabel}</span>
            </li>
            <li>
                <a href="https://hacpai.com/article/1474030007391">${hotKeyLabel}</a>
                <span class="ft-gray">${hotKeyTipLabel}</span>
            </li>
            <li>
                <a href="${servePath}/guide/markdown">Markdown ${tutorialLabel}</a>
                <span class="ft-gray">${markdownTutorialTipLabel}</span>
            </li>
        </ul>
    </div>
</div>
</@home>