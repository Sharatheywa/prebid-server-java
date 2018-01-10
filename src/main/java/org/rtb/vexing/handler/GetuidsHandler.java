package org.rtb.vexing.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.rtb.vexing.cookie.UidsCookie;
import org.rtb.vexing.cookie.UidsCookieService;

import java.util.Objects;

public class GetuidsHandler implements Handler<RoutingContext> {

    private final UidsCookieService uidsCookieService;

    public GetuidsHandler(UidsCookieService uidsCookieService) {
        this.uidsCookieService = Objects.requireNonNull(uidsCookieService);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final UidsCookie uidsCookie = uidsCookieService.parseFromRequest(routingContext);
        final Cookie cookie = uidsCookieService.toCookie(uidsCookie);
        routingContext.addCookie(cookie).response().end(uidsCookie.toJson());
    }
}