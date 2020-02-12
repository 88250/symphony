/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor;

import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.symphony.processor.channel.*;
import org.b3log.symphony.processor.middleware.AnonymousViewCheckMidware;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.PermissionMidware;

/**
 * 请求路由映射.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Feb 10, 2020
 * @since 3.6.2
 */
public final class Router {

    /**
     * 请求路由映射.
     */
    public static void requestMapping() {
        final BeanManager beanManager = BeanManager.getInstance();

        // 注册 HTTP 处理器
        registerProcessors();

        // 注册 WebSocket 处理器
        final ArticleChannel articleChannel = beanManager.getReference(ArticleChannel.class);
        Dispatcher.webSocket("/article-channel", articleChannel);
        final ArticleListChannel articleListChannel = beanManager.getReference(ArticleListChannel.class);
        Dispatcher.webSocket("/article-list-channel", articleListChannel);
        final ChatroomChannel chatroomChannel = beanManager.getReference(ChatroomChannel.class);
        Dispatcher.webSocket("/chat-room-channel", chatroomChannel);
        final GobangChannel gobangChannel = beanManager.getReference(GobangChannel.class);
        Dispatcher.webSocket("/gobang-game-channel", gobangChannel);
        final UserChannel userChannel = beanManager.getReference(UserChannel.class);
        Dispatcher.webSocket("/user-channel", userChannel);

        // 注册 HTTP 错误处理
        final ErrorProcessor errorProcessor = beanManager.getReference(ErrorProcessor.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);
        Dispatcher.error("/error/{statusCode}", errorProcessor::handle);

        // 配置顶层中间件
        Dispatcher.startRequestHandler = new BeforeRequestHandler();
        Dispatcher.endRequestHandler = new AfterRequestHandler();

        Dispatcher.mapping();
    }

    private static void registerProcessors() {
        // 活动
        ActivityProcessor.register();
        // 帖子
        ArticleProcessor.register();
        // 清风明月
        BreezemoonProcessor.register();
        // 验证码
        CaptchaProcessor.register();
        // 聊天室
        ChatroomProcessor.register();
        // 回帖
        CommentProcessor.register();
        // 文件上传
        FileUploadProcessor.register();
        // 关注
        FollowProcessor.register();
        // 首页
        IndexProcessor.register();
        // 登录
        LoginProcessor.register();
        // 通知
        NotificationProcessor.register();
        // 个人设置
        SettingsProcessor.register();
        // 标签
        TagProcessor.register();
        // 榜单
        TopProcessor.register();
        // 用户
        UserProcessor.register();
        // 投票
        VoteProcessor.register();

        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);
        final AnonymousViewCheckMidware anonymousViewCheckMidware = beanManager.getReference(AnonymousViewCheckMidware.class);

        // 搜索
        final SearchProcessor searchProcessor = beanManager.getReference(SearchProcessor.class);
        Dispatcher.get("/search", searchProcessor::search);
        // Sitemap
        final SitemapProcessor sitemapProcessor = beanManager.getReference(SitemapProcessor.class);
        Dispatcher.get("/sitemap.xml", sitemapProcessor::sitemap);
        // 统计
        final StatisticProcessor statisticProcessor = beanManager.getReference(StatisticProcessor.class);
        Dispatcher.get("/statistic", statisticProcessor::showStatistic, anonymousViewCheckMidware::handle);
        // 跳转页
        final ForwardProcessor forwardProcessor = beanManager.getReference(ForwardProcessor.class);
        Dispatcher.get("/forward", forwardProcessor::showForward);
        // 领域
        final DomainProcessor domainProcessor = beanManager.getReference(DomainProcessor.class);
        Dispatcher.get("/domain/{domainURI}", domainProcessor::showDomainArticles, anonymousViewCheckMidware::handle);
        Dispatcher.get("/domains", domainProcessor::showDomains, anonymousViewCheckMidware::handle);
        // RSS 订阅
        final FeedProcessor feedProcessor = beanManager.getReference(FeedProcessor.class);
        Dispatcher.group().router().get().head().uri("/rss/recent.xml").handler(feedProcessor::genRecentRSS);
        Dispatcher.group().router().get().head().uri("/rss/domain/{domainURI}.xml").handler(feedProcessor::genDomainRSS);
        // 同城
        final CityProcessor cityProcessor = beanManager.getReference(CityProcessor.class);
        Dispatcher.group().middlewares(loginCheck::handle).router().get().uris(new String[]{"/city/{city}", "/city/{city}/articles"}).handler(cityProcessor::showCityArticles);
        Dispatcher.get("/city/{city}/users", cityProcessor::showCityUsers, loginCheck::handle);

        // 管理后台
        AdminProcessor.register();
    }
}
