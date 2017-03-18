package org.b3log.symphony.event;

import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.service.ArticleMgmtService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Article update audio handler.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 18, 2017
 * @since 2.1.0
 */
@Named
@Singleton
public class ArticleUpdateAudioHandler extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleUpdateAudioHandler.class);

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_ARTICLE;
    }

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                event.getType(), data, ArticleAddNotifier.class.getName());

        final JSONObject originalArticle = data.optJSONObject(Article.ARTICLE);
        final String authorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);

        articleMgmtService.genArticleAudio(originalArticle, authorId);
    }
}
