package org.n1.av2.statistics

import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.platform.util.TimeService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class StatisticsController(
    private val iceStatisticsService: IceStatisticsService,
    private val timeService: TimeService,
) {

    @GetMapping("/gm/statistics")
    fun getStatistics(response: HttpServletResponse) {

        val timestamp = timeService.formatDateTime(timeService.now())
        val csv = iceStatisticsService.getAllAsCsv()
        response.contentType = "text/csv"
        response.setHeader("Content-Disposition", "attachment; filename=\"ice-hack-statistics-${timestamp}.csv\"")
        response.writer.write(csv)
        response.writer.flush()
    }
}
