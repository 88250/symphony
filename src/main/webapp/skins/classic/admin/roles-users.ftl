<#include "macro-admin.ftl">
<@admin "roles">
<div class="content admin">
    <div class="module list">
        <form class="form">
            <input type="text">
            <button class="red">add</button>
        </form>
        <ul>
            <li class="last">
                <a rel="nofollow" href="http://localhost:8080/member/samkm">
                    <div class="avatar-small fn-left tooltipped tooltipped-n" aria-label="samkm 离线" style="background-image:url('https://img.hacpai.com/avatar/1439777033225?1439777553854')"></div>
                </a> &nbsp;
                <h2 class="fn-inline">
                    <a rel="nofollow" href="http://localhost:8080/member/samkm">samkm</a>
                </h2>
                <button class="fn-right red small" onclick="Util.unfollow(this, '1439777033225', 'user')">
                    remove
                </button>
            </li>
        </ul>
    </div>
</div>
</@admin>