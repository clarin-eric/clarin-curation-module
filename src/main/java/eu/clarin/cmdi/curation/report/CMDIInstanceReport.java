package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.main.Config;

/**
 * @author dostojic
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDIInstanceReport implements Report<CollectionReport> {

    static final double MAX_SCORE = 11;

    public String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    transient double score;

    @XmlElement(name = "score")
    public String overallScore;

    public boolean isValid = true;

    // for score calculation

    // file
    public transient boolean sizeExceeded = false;
    // schema
    public transient boolean schemaAvailable = false;
    public transient boolean schemaInCCR = false;

    // profile
    public transient boolean mdProfileExists = true;
    public transient boolean mdCollectionDispExists = true;
    public transient boolean mdSelfLinkExists = true;

    // for passing values
    public transient int numOfLinks = 0;
    public transient int numOfResProxiesLinks = 0;
    public transient int numOfUniqueLinks = 0;

    public transient List<Message> xmlErrors;

    // sub reports **************************************

    // Header
    @XmlElement(name = "header-section")
    HeaderReport headerReport;

    // file
    @XmlElement(name = "file-section")
    FileReport fileReport;

    // ResProxy
    @XmlElement(name = "resProxy-section")
    ResProxyReport resProxyReport;

    // XMLValidator
    @XmlElement(name = "xml-validation-section")
    XMLReport xmlReport;

    // URL
    @XmlElement(name = "url-validation-section")
    URLReport urlReport;

    // facets
    public FacetReport facets;

    @Override
    public double getMaxScore() {
	return MAX_SCORE;
    };

    @Override
    public void calculateScore() {
	score = 0;

	// fileSize
	if (!sizeExceeded)
	    score += 1; // * weight

	// schema
	double sectionScore = 0;
	if (schemaAvailable)
	    sectionScore += 1; // * weight
	if (schemaInCCR)
	    sectionScore += 1; // * weight

	score += sectionScore; // * schema factor

	sectionScore = 0;
	if (mdProfileExists)
	    sectionScore += 1;
	if (mdCollectionDispExists)
	    sectionScore += 1;
	if (mdSelfLinkExists)
	    sectionScore += 1;
	score += sectionScore; // * header factor

	sectionScore = 0;

	sectionScore += resProxyReport.percOfResProxiesWithMime + resProxyReport.percOfResProxiesWithReferences;

	score += sectionScore; // * resProxy factor

	score += xmlReport.percOfPopulatedElements; // * xmlValidation factor

	sectionScore = 0;
	// it can influence the score, if one collection was done with enabled
	// and the other without
	if (!Double.isNaN(urlReport.percOfValidLinks)) {
	    sectionScore += urlReport.percOfValidLinks;
	} // else

	score += sectionScore; // * urlValidation factor

	score += facets.instance.coverage;// *facetCoverage factor

	overallScore = formatScore(score, getMaxScore());

    }

    @Override
    public void mergeWithParent(CollectionReport parentReport) {

	if (!isValid) {
	    parentReport.addNewInvalid(fileReport.path);
	}

	parentReport.score += score;

	// ResProxies
	parentReport.resProxyReport.totNumOfResProxies += resProxyReport.numOfResProxies;
	parentReport.resProxyReport.totNumOfResProxiesWithMime += resProxyReport.numOfResProxiesWithMime;
	parentReport.resProxyReport.totNumOfResProxiesWithReferences += resProxyReport.numOfResProxiesWithReferences;

	// XMLValidator
	parentReport.xmlReport.totNumOfXMLElements += xmlReport.numOfXMLElements;
	parentReport.xmlReport.totNumOfXMLSimpleElements += xmlReport.numOfXMLSimpleElements;
	parentReport.xmlReport.totNumOfXMLEmptyElement += xmlReport.numOfXMLEmptyElement;

	// URL
	parentReport.urlReport.totNumOfLinks += urlReport.numOfLinks;
	parentReport.urlReport.totNumOfUniqueLinks += urlReport.numOfUniqueLinks;
	parentReport.urlReport.totNumOfResProxiesLinks += urlReport.numOfResProxiesLinks;
	parentReport.urlReport.totNumOfBrokenLinks += urlReport.numOfBrokenLinks;

	// Facet
	if (facets != null && facets.instance != null)
	    parentReport.avgFacetCoverageByInstanceSum += facets.instance.coverage;

	parentReport.handleProfile(headerReport.profile);

    }

    @Override
    public void marshal(OutputStream os) throws Exception {
	try {

	    JAXBContext jaxbContext = JAXBContext.newInstance(CMDIInstanceReport.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	    // output pretty printed
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	    jaxbMarshaller.marshal(this, os);

	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public String getProfile() {
	return headerReport.profile;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class FileReport {
	String path;
	long size;

	@XmlElementWrapper(name = "details")
	List<Message> messages = null;

	public FileReport(String path, long size, List<Message> messages) {
	    this.path = path;
	    this.size = size;
	    this.messages = messages;
	}

	public FileReport() {
	};
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class HeaderReport {
	String profile;

	@XmlElementWrapper(name = "details")
	List<Message> messages = null;

	public HeaderReport(String profile, List<Message> messages) {
	    this.profile = profile;
	    this.messages = messages;
	}

	public HeaderReport() {
	};
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class ResProxyReport {
	int numOfResProxies;
	int numOfResProxiesWithMime;
	double percOfResProxiesWithMime;
	int numOfResProxiesWithReferences;
	double percOfResProxiesWithReferences;

	@XmlElementWrapper(name = "resourceTypes")
	List<ResourceType> resourceType;

	// @XmlElementWrapper(name = "details") List<Message> messages = null;

	public ResProxyReport(int numOfResProxies, int numOfResProxiesWithMime, double percOfResProxiesWithMime,
		int numOfResProxiesWithReferences, double percOfResProxiesWithReferences,
		List<ResourceType> resourceTypes) {
	    this.numOfResProxies = numOfResProxies;
	    this.numOfResProxiesWithMime = numOfResProxiesWithMime;
	    this.percOfResProxiesWithMime = percOfResProxiesWithMime;
	    this.numOfResProxiesWithReferences = numOfResProxiesWithReferences;
	    this.percOfResProxiesWithReferences = percOfResProxiesWithReferences;
	    this.resourceType = resourceTypes;
	}

	public ResProxyReport() {
	}

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class ResourceType {
	@XmlAttribute
	String type;

	@XmlAttribute
	int count;

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class XMLReport {
	int numOfXMLElements;
	int numOfXMLSimpleElements;
	int numOfXMLEmptyElement;
	double percOfPopulatedElements;

	@XmlElementWrapper(name = "details")
	List<Message> messages = null;

	public XMLReport(int numOfXMLElements, int numOfXMLSimpleElements, int numOfXMLEmptyElement,
		double percOfPopulatedElements, List<Message> messages) {
	    this.numOfXMLElements = numOfXMLElements;
	    this.numOfXMLSimpleElements = numOfXMLSimpleElements;
	    this.numOfXMLEmptyElement = numOfXMLEmptyElement;
	    this.percOfPopulatedElements = percOfPopulatedElements;
	    this.messages = messages;
	}

	public XMLReport() {
	}
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    static class URLReport {
	int numOfLinks;
	int numOfUniqueLinks;
	int numOfResProxiesLinks;
	int numOfBrokenLinks;
	double percOfValidLinks;

	@XmlElementWrapper(name = "details")
	List<Message> messages = null;

	public URLReport(int numOfLinks, int numOfUniqueLinks, int numOfResProxiesLinks, int numOfBrokenLinks,
		double percOfValidLinks, List<Message> messages) {
	    this.numOfLinks = numOfLinks;
	    this.numOfUniqueLinks = numOfUniqueLinks;
	    this.numOfResProxiesLinks = numOfResProxiesLinks;
	    this.numOfBrokenLinks = numOfBrokenLinks;
	    this.percOfValidLinks = percOfValidLinks;
	    this.messages = messages;
	}

	public URLReport() {
	}

    }

    public void addFileReport(String path, long size, List<Message> messages) {
	fileReport = new FileReport(path, size, messages);
    }

    public void addHeaderReport(String profile, List<Message> messages) {
	headerReport = new HeaderReport(profile, messages);
    }

    public void addResProxyReport(int numOfResProxies, int numOfResProxiesWithMime, int numOfResProxiesWithReferences,
	    Map<String, Integer> resourceTypeMap) {

	double percOfResProxiesWithMime = 0;
	double percOfResProxiesWithReferences = 0;

	if (numOfResProxies > 0) {
	    percOfResProxiesWithMime = (double) numOfResProxiesWithMime / numOfResProxies;
	    percOfResProxiesWithReferences = (double) numOfResProxiesWithReferences / numOfResProxies;
	}

	// prepare resTypes
	List<ResourceType> resourceTypes = null;
	if (!resourceTypeMap.isEmpty()) {
	    resourceTypes = new LinkedList<>();
	    for (Entry<String, Integer> resType : resourceTypeMap.entrySet()) {
		ResourceType res = new ResourceType();
		res.type = resType.getKey();
		res.count = resType.getValue();
		resourceTypes.add(res);
	    }
	}

	resProxyReport = new ResProxyReport(numOfResProxies, numOfResProxiesWithMime, percOfResProxiesWithMime,
		numOfResProxiesWithReferences, percOfResProxiesWithReferences, resourceTypes);
    }

    public void addXmlReport(int numOfXMLElements, int numOfXMLSimpleElements, int numOfXMLEmptyElement,
	    List<Message> messages) {

	double percOfPopulatedElements = (numOfXMLSimpleElements - numOfXMLEmptyElement)
		/ (double) numOfXMLSimpleElements;
	xmlReport = new XMLReport(numOfXMLElements, numOfXMLSimpleElements, numOfXMLEmptyElement,
		percOfPopulatedElements, messages);
    }

    public void addURLReport(int numOfBrokenLinks, List<Message> messages) {

	double percOfValidLinks = Double.NaN;
	if (Config.HTTP_VALIDATION())
	    percOfValidLinks = (numOfUniqueLinks - numOfBrokenLinks) / (double) numOfUniqueLinks;
	urlReport = new URLReport(numOfLinks, numOfUniqueLinks, numOfResProxiesLinks, numOfBrokenLinks,
		percOfValidLinks, messages);
    }
}
