package eu.clarin.cmdi.curation.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;

public abstract class AbstractProcessor<R extends Report<?>> {
	
	private static final Logger _logger = LoggerFactory.getLogger(AbstractProcessor.class);

	public Report<?> process(CurationEntity entity) {
		Report<?> report = createReport();
		try{
			for(ProcessingStep step : createPipeline()){
				step.process(entity, report);
				report.addSegmentScore(step.calculateScore(report));
			}
				
			return report;
		}catch (FileSizeException e) {
			_logger.error(e.getMessage());
			return new ErrorReport(entity.toString(), e.getMessage());
		}catch(Exception e){
			_logger.error("Error while processing {}", entity, e);
			return new ErrorReport(entity.toString(), getStackTrace(e));
		}
		
	}

	protected abstract Collection<ProcessingStep> createPipeline();

	protected abstract R createReport();
	
	private String getStackTrace(final Throwable ex) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

}