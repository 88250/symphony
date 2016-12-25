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
                <a href="${servePath}/tag/%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97">${basicLabel}</a>
                <span class="ft-gray">${basicTipLabel}</span>
            </li>
            <li>
                <a href="https://hacpai.com/article/1474030007391">${hotKeyLabel}</a>
                <span class="ft-gray">${hotKeyTipLabel}</span>
            </li>
            <li>
                <a href="${servePath}/guide/markdown">Markdown ${newbieGuideLabel}</a>
                <span class="ft-gray">话说排版很重要，赶快上手吧</span>
            </li>
        </ul>
    </div>
</div>
</@home>