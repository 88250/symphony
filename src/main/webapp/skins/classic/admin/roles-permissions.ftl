<#include "macro-admin.ftl">
<@admin "roles">
<div class="content admin">
    <div class="module list">
        <div class="form fn-clear">
            <button class="green fn-right">Save</button>
        </div>
        <ul>
            <li class="last">
                <div class="fn-clear">
                    <h2 class="fn-left">title</h2>
                    <div class="fn-right">
                        <button class="mid">全选</button> &nbsp;
                        <button class="mid">反选</button>
                    </div>
                </div>
                <label><input type="checkbox"> user</label> &nbsp; &nbsp;
                <label><input type="checkbox"> user</label>
            </li>
        </ul>
    </div>
</div>
</@admin>