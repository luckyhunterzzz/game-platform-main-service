package com.gameplatform.mainservice.summon.dto.response;

import java.util.List;

public record SummonCalendarFeedResponse(
        List<SummonPublicResponse> current,
        List<SummonPublicResponse> upcoming
) {
}
