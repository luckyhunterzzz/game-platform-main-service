package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroBugReportsAdminResponse(
        HeroBugReportResponse activeReport,
        List<HeroBugReportResponse> history
) {
}
