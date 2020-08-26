package org.bonitasoft.serverconfiguration.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bonitasoft.serverconfiguration.ComparaisonResult;

import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCELEVEL;
import org.bonitasoft.serverconfiguration.ComparaisonResult.DIFFERENCESTATUS;
import org.bonitasoft.serverconfiguration.ConfigAPI.ComparaisonParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

// Read more: https://javarevisited.blogspot.com/2017/04/how-to-compare-two-xml-files-in-java.html#ixzz64wEIRa7y
public class ContentTypeXml extends ContentTypeText {

    Logger logger = Logger.getLogger(ContentTypeXml.class.getName());

    private String content;

    public ContentTypeXml(File file) {
        super(file);
    }

    public ContentTypeXml(String content) {
        super(null);
        this.content = content;
    }

    @Override
    public String getName() {
        return "xml";
    }

    @Override
    public boolean matchFile(File file) {
        return file.getName().endsWith(".xml") || file.getName().endsWith(".xsd");

    }

    @Override
    public DIFFERENCELEVEL getLevel(ComparaisonParameter comparaisonParameter) {
        if (comparaisonParameter.referentielIsABundle && file.getName().endsWith("bonita.xml"))
            return DIFFERENCELEVEL.EXPECTED;
   
        return DIFFERENCELEVEL.IMPORTANT;
    }

    public void compareFile(File fileLocal, File fileReferentiel, ComparaisonParameter comparaisonParameter, ComparaisonResult comparaisonResult) {
        // compare 2 properties files
        comparaisonResult.info("   [" + fileLocal.getName() + "] (XML) <-> [" + fileReferentiel.getName() + "] (" + fileLocal.getAbsolutePath() + ") <-> (" + fileReferentiel.getAbsolutePath() + ")");
        try {
            FileInputStream fsReferentiel = new FileInputStream(fileReferentiel);
            FileInputStream fsLocal = new FileInputStream(fileLocal);

            DiffBuilder diffBuilder = DiffBuilder.compare(fsReferentiel);
            diffBuilder.withTest(fsLocal);
            diffBuilder.ignoreComments();
            diffBuilder.ignoreWhitespace();
            diffBuilder.ignoreElementContentWhitespace();
            Diff diff = diffBuilder.build();

            for (Difference difference : diff.getDifferences()) {
                comparaisonResult.report(fileLocal, DIFFERENCESTATUS.DIFFERENT, getLevel(comparaisonParameter), difference.toString(),comparaisonParameter);
            }
        } catch (Exception e) {
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionDetails = sw.toString();

                comparaisonResult.info("Error " + e.getMessage() + " at " + exceptionDetails);
            }
        }
    }

    /**
     * return the value of a node
     * 
     * @param node
     * @return
     */
    public NodeList getXmlNodes(String nodeName) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            if (file != null)
                doc = dBuilder.parse(file);
            else if (content != null)
                doc = dBuilder.parse(new InputSource(new StringReader(content)));
            else
                return null;
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(nodeName);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * return the first node from a nodename, with a special attribut
     * 
     * @param nodeName
     * @param attributFilter
     * @param attributValue
     * @return
     */
    public Element getXmlElement(String nodeName, String attributFilter, String attributValue) {

        NodeList nodeList = getXmlNodes(nodeName);
        if (nodeList == null)
            return null;
        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node node = nodeList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

                
                if (attributValue.equals(element.getAttribute(attributFilter)))
                    return element;
            }
        }
        return null;
    }
}
