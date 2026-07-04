package com.gameplatform.mainservice.event.repository.projection;

public interface EventBlockCountProjection {

    Long getEventId();

    long getBlockCount();
}
