<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
<head>
    <@head title="Manual for Hackers - ${symphonyLabel}" />
    <link rel="canonical" href="${servePath}/man">
</head>
<body>
<#include "../header.ftl">
    <div class="main">
        <div class="wrapper">
            <div class="content">
                <div class="fn-hr10"></div>
                <h2 class="sub-head">Hacker's Manual</h2>
                <div class="article-module">
                    <i class="ft-gray">Command manual for hackers, with <span class="ft-red">&hearts;</span> from <a
                            href="https://github.com/tldr-pages/tldr" target="_blank">tldr</a></i>
                    <br><br>
                    <div class="form">
                        <input id="manCmd" type="text" autofocus placeholder="${mans[0].manCmd}">
                    </div>
                    <div id="manHTML" class="content-reset">
                        ${mans[0].manHTML}
                    </div>
                </div>
            </div>
            <div class="side">
                <#include "../side.ftl">
            </div>
        </div>
    </div>
    <#include "../footer.ftl">
        <script>
    (function () {
            var obj = {};
            $("#manCmd").completed({
                onlySelect: true,
                data: [],
                afterSelected: function ($it) {
                    $("#manHTML").html(obj[$it.text()]);
                },
                afterKeyup: function (event) {
                     // 回车，自动添加标签
                    if (event.keyCode === 13) {
                        var selected = $('#manCmdSelectedPanel a.selected').text();
                        $("#manHTML").html(obj[selected]);
                        return false;
                    }

                    // ECS 隐藏面板
                    if (event.keyCode === 27) {
                        return false;
                    }

                    // 上下左右
                    if (event.keyCode === 38 || event.keyCode === 40) {
                        var selected = $('#manCmdSelectedPanel a.selected').text();
                        $("#manHTML").html(obj[selected]);
                        return false;
                    }

                    if ($("#manCmd").val().replace(/\s/g, '') === '') {
                        return false;
                    }

                    $.ajax({
                        url: Label.servePath + '/man/cmd?name=' + $("#manCmd").val(),
                        error: function (jqXHR, textStatus, errorThrown) {
                            console.log(textStatus);
                        },
                        success: function (result, textStatus) {
                            if (result.sc && result.mans && result.mans.length > 0) {
                                var tips = [];
                                for (var i = 0, ii = result.mans.length; i < ii; i++) {
                                    tips.push(result.mans[i].manCmd);
                                    obj[result.mans[i].manCmd] = result.mans[i].manHTML;
                                }
                                $("#manCmd").completed('updateData', tips);
                            } else {
                                $("#manHTML").html('${mans[0].manHTML}');
                            }
                        }
                    });
                }
            });

            $('#manCmdSelectedPanel').width($('#manCmd').outerWidth() - 2);
        })();
        </script>
</body>
</html>