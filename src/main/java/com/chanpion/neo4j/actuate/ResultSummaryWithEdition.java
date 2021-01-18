package com.chanpion.neo4j.actuate;

import org.neo4j.driver.summary.ResultSummary;

/**
 * @author April Chen
 * @date 2021/1/18 20:27
 */
final class ResultSummaryWithEdition {
    final ResultSummary resultSummary;

    final String edition;

    ResultSummaryWithEdition(ResultSummary resultSummary, String edition) {
        this.resultSummary = resultSummary;
        this.edition = edition;
    }
}
