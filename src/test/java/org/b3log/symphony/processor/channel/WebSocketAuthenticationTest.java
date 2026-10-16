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
package org.b3log.symphony.processor.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.http.Cookie;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.Session;
import org.b3log.latke.http.WebSocketChannel;
import org.b3log.latke.http.WebSocketSession;
import org.b3log.latke.ioc.Bean;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.util.Crypts;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.ActivityMgmtService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket 登录认证、匹配和消息处理回归测试，不连接数据库。
 */
public class WebSocketAuthenticationTest {

    private static final String PLAYER1 = "websocket-test-player1";
    private static final String PLAYER2 = "websocket-test-player2";
    private final List<EmbeddedChannel> channels = new ArrayList<>();
    private final List<String> statusUpdates = new ArrayList<>();
    private final List<String> startedPlayers = new ArrayList<>();
    private final UserChannel userChannel = new UserChannel();
    private final GobangChannel gobangChannel = new GobangChannel();

    @BeforeClass
    public void registerServices() {
        final Properties properties = new Properties();
        properties.setProperty("runtimeDatabase", "NONE");
        Latkes.setLocalProps(properties);
        register(UserMgmtService.class, new UserMgmtService() {
            @Override
            public void updateOnlineStatus(final String userId, final String ip,
                                           final boolean onlineFlag, final boolean force) {
                statusUpdates.add(userId + ":" + onlineFlag);
            }
        });
        register(ActivityMgmtService.class, new ActivityMgmtService() {
            @Override
            public synchronized JSONObject startGobang(final String userId) {
                startedPlayers.add(userId);
                return new JSONObject();
            }
        });
        register(UserRepository.class, new UserRepository() {
            @Override
            public JSONObject get(final String userId) {
                return PLAYER1.equals(userId) ? user(PLAYER1) : null;
            }
        });
    }

    @BeforeMethod
    public void prepareUsers() {
        Sessions.put(PLAYER1, user(PLAYER1));
        Sessions.put(PLAYER2, user(PLAYER2));
        statusUpdates.clear();
        startedPlayers.clear();
    }

    @AfterMethod
    public void clearSessions() {
        UserChannel.SESSIONS.clear();
        GobangChannel.SESSIONS.clear();
        GobangChannel.chessPlaying.clear();
        GobangChannel.chessRandomWait.clear();
        GobangChannel.antiPlayer.clear();
        CacheFactory.getCache("sessions").remove(PLAYER1);
        CacheFactory.getCache("sessions").remove(PLAYER2);
        for (final EmbeddedChannel channel : channels) {
            channel.finishAndReleaseAll();
        }
        channels.clear();
    }

    @Test
    public void authenticatesCookieWithoutHttpSession() throws Exception {
        final WebSocketSession session = session(PLAYER1);
        Assert.assertNull(session.getHttpSession());
        final JSONObject actual = Sessions.currentUser(session);
        Assert.assertEquals(actual.optString(Keys.OBJECT_ID), PLAYER1);
        actual.put(User.USER_NAME, "modified");
        Assert.assertEquals(Sessions.currentUser(session).optString(User.USER_NAME), PLAYER1);
    }

    @Test
    public void retainsHttpCookieAuthenticationAndIp() throws Exception {
        final Request request = new Request(null, null) {
            @Override
            public String getRealRemoteAddr() {
                return "192.0.2.1";
            }
        };
        request.setCookies(session(PLAYER1).getCookies());
        Assert.assertEquals(Sessions.currentUser(request).optString(UserExt.USER_LATEST_LOGIN_IP), "192.0.2.1");
    }

    @Test
    public void restoresUserOnCacheMiss() throws Exception {
        CacheFactory.getCache("sessions").remove(PLAYER1);
        Assert.assertEquals(Sessions.currentUser(session(PLAYER1)).optString(Keys.OBJECT_ID), PLAYER1);
    }

    @Test
    public void releasesWebSocketAuthenticationConnection() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean();
        final Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        closed.set(true);
                    }
                    return null;
                });
        JdbcRepository.CONN.set(connection);
        Sessions.currentUser(session(PLAYER1));
        Assert.assertTrue(closed.get());
        Assert.assertNull(JdbcRepository.CONN.get());
    }

    @DataProvider(name = "invalidStatuses")
    public Object[][] invalidStatuses() {
        return new Object[][]{
                {UserExt.USER_STATUS_C_INVALID},
                {UserExt.USER_STATUS_C_INVALID_LOGIN},
                {UserExt.USER_STATUS_C_DEACTIVATED}
        };
    }

    @Test(dataProvider = "invalidStatuses")
    public void rejectsDisabledUsers(final int status) throws Exception {
        Sessions.put(PLAYER1, user(PLAYER1).put(UserExt.USER_STATUS, status));
        Assert.assertNull(Sessions.currentUser(session(PLAYER1)));
    }

    @Test
    public void rejectsInvalidCookieAndChangedPassword() throws Exception {
        final WebSocketSession invalid = session(null);
        invalid.getCookies().add(new Cookie(Sessions.COOKIE_NAME, "invalid-cookie"));
        Assert.assertNull(Sessions.currentUser(invalid));
        final WebSocketSession changedPassword = session(PLAYER1);
        Sessions.put(PLAYER1, user(PLAYER1).put(User.USER_PASSWORD, "changed-password"));
        Assert.assertNull(Sessions.currentUser(changedPassword));
    }

    @Test
    public void ignoresUnauthenticatedConnectionsAndMessages() throws Exception {
        final WebSocketSession anonymous = session(null);
        final Session stale = new Session("stale-session");
        stale.setAttribute(User.USER, user(PLAYER1).toString());
        final Field httpSession = WebSocketSession.class.getDeclaredField("session");
        httpSession.setAccessible(true);
        httpSession.set(anonymous, stale);
        userChannel.onConnect(anonymous);
        gobangChannel.onConnect(anonymous);
        userChannel.onMessage(message(anonymous, "{}"));
        gobangChannel.onMessage(message(anonymous, "{}"));
        userChannel.onClose(anonymous);
        gobangChannel.onClose(anonymous);
        Assert.assertTrue(UserChannel.SESSIONS.isEmpty());
        Assert.assertTrue(GobangChannel.SESSIONS.isEmpty());
        Assert.assertTrue(statusUpdates.isEmpty());
    }

    @Test
    public void tracksUserTabsAndCleansUpAfterCookieLoss() throws Exception {
        final WebSocketSession first = session(PLAYER1);
        final WebSocketSession second = session(PLAYER1);
        userChannel.onConnect(first);
        userChannel.onConnect(second);
        Assert.assertEquals(UserChannel.SESSIONS.get(PLAYER1).size(), 2);
        userChannel.onMessage(message(first, "ping"));
        Assert.assertEquals(statusUpdates.size(), 3);
        first.getCookies().clear();
        second.getCookies().clear();
        userChannel.onClose(first);
        Assert.assertEquals(statusUpdates.size(), 3);
        userChannel.onClose(second);
        userChannel.onClose(second);
        Assert.assertEquals(statusUpdates.size(), 4);
        Assert.assertEquals(statusUpdates.get(3), PLAYER1 + ":false");
        Assert.assertTrue(UserChannel.SESSIONS.get(PLAYER1).isEmpty());
    }

    @Test
    public void matchesPlayersAndProcessesMovesFromMessageText() throws Exception {
        final WebSocketSession first = session(PLAYER1);
        final WebSocketSession second = session(PLAYER2);
        gobangChannel.onConnect(first);
        Assert.assertEquals(receive(0).optInt("type"), 3);
        gobangChannel.onConnect(second);
        Assert.assertEquals(receive(0).optString("player"), PLAYER1);
        Assert.assertEquals(receive(1).optString("player"), PLAYER2);
        Assert.assertEquals(startedPlayers.size(), 2);
        final JSONObject move = new JSONObject().put("type", 2).put("x", 30).put("y", 30)
                .put("size", 30).put("player", PLAYER2);
        gobangChannel.onMessage(message(first, move.toString()));
        Assert.assertEquals(GobangChannel.chessPlaying.get(PLAYER1).getChess()[1][1], 1);
        Assert.assertEquals(receive(0).optString("player"), PLAYER1);
        Assert.assertEquals(receive(1).optInt("step"), 2);
        move.put("x", 60).put("player", PLAYER1);
        gobangChannel.onMessage(message(second, move.toString()));
        Assert.assertEquals(GobangChannel.chessPlaying.get(PLAYER1).getChess()[2][1], 2);
        Assert.assertEquals(receive(0).optInt("step"), 1);
        Assert.assertEquals(receive(1).optString("player"), PLAYER2);
    }

    private static JSONObject user(final String id) {
        return new JSONObject().put(Keys.OBJECT_ID, id).put(User.USER_NAME, id)
                .put(User.USER_PASSWORD, "test-password").put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);
    }

    private WebSocketSession session(final String userId) throws Exception {
        final EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        channels.add(channel);
        final Constructor<WebSocketSession> constructor = WebSocketSession.class
                .getDeclaredConstructor(ChannelHandlerContext.class);
        constructor.setAccessible(true);
        final WebSocketSession session = constructor.newInstance(channel.pipeline().firstContext());
        if (null != userId) {
            final JSONObject cookie = new JSONObject().put(Keys.OBJECT_ID, userId)
                    .put(Keys.TOKEN, "test-password:random");
            session.getCookies().add(new Cookie(Sessions.COOKIE_NAME,
                    Crypts.encryptByAES(cookie.toString(), Symphonys.COOKIE_SECRET)));
        }
        return session;
    }

    private static WebSocketChannel.Message message(final WebSocketSession session, final String text) throws Exception {
        final Constructor<WebSocketChannel.Message> constructor = WebSocketChannel.Message.class
                .getDeclaredConstructor(String.class, WebSocketSession.class);
        constructor.setAccessible(true);
        return constructor.newInstance(text, session);
    }

    private JSONObject receive(final int channelIndex) {
        final TextWebSocketFrame frame = channels.get(channelIndex).readOutbound();
        Assert.assertNotNull(frame);
        try {
            return new JSONObject(frame.text());
        } finally {
            frame.release();
        }
    }

    private static <T> void register(final Class<T> type, final T service) {
        final BeanManager manager = BeanManager.getInstance();
        manager.addBean(new Bean<T>(manager, type.getName(), type, Collections.singleton(type),
                Collections.singleton(Singleton.class)) {
            @Override
            public T create() {
                return service;
            }
        });
    }
}
