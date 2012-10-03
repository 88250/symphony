/*
 * Copyright (c) 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview home.
 *
 * @author <a href="mailto:LLY219@gmail.com">Liyuan Li</a>
 * @version 1.0.0.3, Sep 27, 2012
 */

/**
 * @description Home function
 * @static
 */
var Home = {
    /**
   * @description 文章 tab 和评论 tab 切换
   */
    tab: function () {
        var $tabs = $(".tab"),
        $lis = $tabs.find("li");
        var $li1 = $($lis.get(0)),
        $li2 = $($lis.get(1)),
        $contents = $tabs.next().children("div");
        $li1.click(function () {
            $($contents.get(1)).hide();
            $($contents.get(0)).show();
            $li2.removeClass("current");
            $li1.addClass("current");
        });
        $li2.click(function () {
            $($contents.get(1)).show();
            $($contents.get(0)).hide();
            $li1.removeClass("current");
            $li2.addClass("current");
        });
    } 
};

Home.tab();