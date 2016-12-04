<#include "macro-admin.ftl">
<@admin "roles">
<div class="content admin">
    <div class="module list">
        <form class="form">
            <input type="text" placeholder="title" id="rolesTitle">
            <input type="text" placeholder="desc" id="rolesDesc">
            <button class="red">创建</button>
        </form>
        <ul>
            <li class="last">
                <div class="fn-flex">
                    <h2 class="fn-flex-1"><a href="#">title</a></h2>
                    <div class="">
                        <a class="mid btn" href="${servePath}/admin/roles/users">user</a>
                        <a class="mid btn" href="${servePath}/admin/roles/permissions">permission</a>
                    </div>
                </div>


                <div class="ft-gray">description</div>
            </li>
        </ul>
    </div>
</div>
</@admin>