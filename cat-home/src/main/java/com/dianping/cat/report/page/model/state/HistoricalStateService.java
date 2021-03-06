package com.dianping.cat.report.page.model.state;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalStateService extends BaseHistoricalModelService<StateReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportService;

	public HistoricalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	protected StateReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		StateReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private StateReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryStateReport(domain, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private StateReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, StateAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
