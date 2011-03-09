/*******************************************************************************
 * org.havensoft.tools.xml.XMLHelper.java
 * 
 * Created on Jun 12, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.xml.impl.ElementVector;

/**
 * This (non-instanciable) class provides helper methods for dealing with DOM
 * XML.
 * 
 * @author tedmunds
 */
public abstract class XMLHelper
{
    /**
     * This class is not intended for instanciation.  Hence the
     * <code>private</code> default constructor.
     * 
     */
    private XMLHelper()
    {
        // Do nothing
    }
    
    /**
     * <code>XMLHelper</code> can run applications that are fully described by
     * XML files. If the arguments are <code>-r selfDescribingAppFile.xml</code>,
     * the named XML file will be opened, and the root tag will be loaded as a
     * self-describing object - it is up to the object's loader to make sure
     * that it stays alive for as long as needed (e.g. through an auto-start
     * attribute).
     * 
     * @param args
     *        the program arguments. See the
     *        {@linkplain #USAGE_STRING usage string}.
     * @throws XMLException
     *         if the XML file cannot be parsed.
     * @throws InitializationException
     *         if the self-describing object cannot be initialized.
     * @throws IOException
     *         if the XML file cannot be opened.
     */
    public static void main(String[] args)
        throws XMLException,
            InitializationException,
            IOException
    {
        if (args.length < 1)
        {
            System.err.println(USAGE_STRING);
            return;
        }
        String appType = args[0];
        if (appType.equals("-r"))
        {
            if (args.length < 2)
            {
                System.err.println(USAGE_STRING);
                return;
            }
            runSelfDescribingApp(args[1]);
            return;
        }
        System.err.println(USAGE_STRING);
    }

    /**
     * The usage string for running <code>XMLHelper</code> as an application.
     */
    public static final String USAGE_STRING = "Usage: "
                                              + XMLHelper.class.getName()
                                              + " -r selfDescribingAppFile.xml";

    /**
     * Runs a self-describing application. The named file is opened, and the
     * root element is loaded as a self-describing object. Note that the
     * behaviour of this method is identical to
     * {@link #loadSelfDescribingFromRoot(String)}; this method alias is
     * present to evoke a different view of the same functionality. Where the
     * plain loader can be used to load any old object, this method is for
     * loading objects that are expected to <em>do</em> something (e.g. launch
     * a thread, execute a loop, etc.), rather than just exist.
     * 
     * @param filename
     *        the name of the XML file containing the self-describing
     *        application
     * @throws XMLException
     *         if the XML file cannot be parsed.
     * @throws InitializationException
     *         if the self-describing application cannot be initialized.
     * @throws IOException
     *         if the XML file cannot be opened.
     */
    public static void runSelfDescribingApp(String filename)
        throws XMLException,
            InitializationException,
            IOException
    {
        loadSelfDescribingFromRoot(filename);
    }

    /**
     * Runs a self describing application in a new thread.
     * 
     * @param filename
     *        the name of the XML file containing the self-describing
     *        application
     * @param threadName the name to assign to the new thread
     * @param isDaemon whether to run the application as a daemon
     * @see #runSelfDescribingApp(String)
     */
    public static void runSelfDescribingAppInNewThread(String filename,
                                                       String threadName,
                                                       boolean isDaemon)
    {
        final String daemonFilename = filename;
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    runSelfDescribingApp(daemonFilename);
                }
                catch (XMLException e)
                {
                    throw new RuntimeException("Exception while running"
                                               + " self-describing app.", e);
                }
                catch (InitializationException e)
                {
                    throw new RuntimeException("Exception while running"
                                               + " self-describing app.", e);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Exception while running"
                                               + " self-describing app.", e);
                }
            }
        }, threadName);
        thread.setDaemon(isDaemon);
        thread.start();
    }

    /**
     * Opens the specified XML file and parses it into a DOM
     * <code>Document</code>.
     * 
     * @param inputFile the file to open
     * @return the <code>Document</code> built from the XML file
     * @throws XMLException if the file cannot be parsed as XML
     * @throws IOException if the file cannot be opened
     */
    public static Document openAsDOM(File inputFile)
        throws XMLException, IOException
    {
        InputStream inputStream = new FileInputStream(inputFile);
        if(inputFile.getName().endsWith(".gz"))
        {
            inputStream = new GZIPInputStream(inputStream);
        }
        return openAsDOM(inputStream);
    }

    /**
     * Opens the specified XML file and parses it into a DOM
     * <code>Document</code>.
     * 
     * @param inputStream the file to open
     * @return the <code>Document</code> built from the XML file
     * @throws XMLException if the file cannot be parsed as XML
     * @throws IOException if the file cannot be opened
     */
    public static Document openAsDOM(InputStream inputStream)
        throws XMLException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce)
        {
            throw new XMLException("Failed to load XML file.", pce);
        }
        try
        {
            document = builder.parse(inputStream);
        }
        catch (SAXException se)
        {
            throw new XMLException("Failed to parse XML file.", se);
        }
        return document;
    }

    /**
     * Opens the specified XML file and parses it into a DOM
     * <code>Document</code>.
     * 
     * @param filename the name of the file to open ({@link File#File(String)})
     * @return the <code>Document</code> built from the XML file
     * @throws XMLException if the file cannot be parsed as XML
     * @throws IOException if the file cannot be opened
     */
    public static Document openAsDOM(String filename)
        throws XMLException, IOException
    {
        File inputFile = new File(filename);
        return openAsDOM(inputFile);
    }

    /**
     * Opens the specified XML URL and parses it into a DOM
     * <code>Document</code>.
     * 
     * @param url the name of the url to open
     * @return the <code>Document</code> built from the XML file
     * @throws XMLException if the file cannot be parsed as XML
     * @throws IOException if the file cannot be opened
     */
    public static Document openAsDOM(URL url) throws XMLException, IOException
    {
        InputStream istream = url.openStream();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce)
        {
            throw new XMLException("Failed to load XML file.", pce);
        }
        try
        {
            document = builder.parse(istream);
        }
        catch (SAXException se)
        {
            throw new XMLException("Failed to parse XML file.", se);
        }
        return document;
    }

    /**
     * Constructs a DOM <code>Document</code> in memory.
     * 
     * @return the created document.
     * @throws XMLException if the DOM document cannot be created.
     */
    public static Document createDOMInMemory() throws XMLException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try
        {
            builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }
        catch (ParserConfigurationException pce)
        {
            throw new XMLException("Failed to construct DOM.", pce);
        }
        return document;
    }

    /**
     * Creates a root element (with the specified name) for the specified
     * document. Note that if the document already has a root element, the
     * created element will be illegal (but will still be created).
     * 
     * @param document
     *        the document for which a root element is to be created
     * @param rootName
     *        the name of the root element to be created
     * @return the created root element
     */
    public static Element createRootElement(Document document, String rootName)
    {
        Element retval = document.createElement(rootName);
        document.appendChild(retval);
        return retval;
    }

    /**
     * Gets an <code>ElementList</code> that contains all the child
     * <code>Element</code>s of the provided <code>Node</code>.  If there are
     * no child <code>Elements</code> (or if the type of the provided
     * <code>Node</code> prohibits it from having <code>Element</code>
     * children), an empty <code>ElementList</code> is returned.
     * 
     * @param parentNode the <code>Node</code> whose child <code>Element</code>s
     *                   are to be listed
     * @return the <code>ElementList</code> containing all the child
     *         <code>Element</code>s of the <code>parentNode</code>
     * @deprecated replaced by {@link #getChildElementList(Node)}
     */
    @Deprecated
    public static ElementList getChildElements(Node parentNode)
    {
        NodeList allChildNodes = parentNode.getChildNodes();
        ElementList childElements = new ElementVector();

        for (int i = 0; i < allChildNodes.getLength(); i++)
        {
            Node node = allChildNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                childElements.add((Element)node);
            }
        }

        return childElements;
    }

    /**
     * Gets an <code>ElementList</code> of all the <code>Element</code>s with a
     * given tag name that are <i>direct</i> children of the specified
     * <code>Node</code>.  If there are no matching child <code>Element</code>s,
     * an empty <code>ElementList</code> is returned.
     * 
     * @param parentNode the <code>Node</code> whose children are to be listed
     * @param name the <code>String</code> to match against the
     *             <code>Element</code> tag names.  The special value "*"
     *             matches any tag name.
     * @return the <code>ElementList</code> containing the matching children
     * @deprecated replaced by
     *             {@link #getChildElementListByTagName(Node, String)}
     */
    @Deprecated
    public static ElementList getChildElementsByTagName(
        Node parentNode,
        String name)
    {
        return getChildElementsByTagNameNS(parentNode, "*", name);
    }

    /**
     * Gets an <code>ElementList</code> of all the <code>Element</code>s with a
     * given namespace URI and local name that are <i>direct</i> children of
     * the specified <code>Node</code>.  If there are no matching child
     * <code>Element</code>s, an empty <code>ElementList</code> is returned.
     * 
     * @param parentNode the <code>Node</code> whose children are to be listed
     * @param namespaceURI the <code>String</code> to match against the
     *                     <code>Element</code> namespace URIs.  The special
     *                     value "*" matches any namespace URI.
     * @param localName the <code>String</code> to match against the
     *                  <code>Element</code> tag names.  The special value "*"
     *                  matches any tag name.
     * @return the <code>ElementList</code> containing the matching children
     * @deprecated replaced by
     *             {@link #getChildElementListByTagNameNS(Node, String, String)}
     */
    @Deprecated
    public static ElementList getChildElementsByTagNameNS(
        Node parentNode,
        String namespaceURI,
        String localName)
    {
        ElementList childElements = getChildElements(parentNode);
        if (namespaceURI.equals("*") && localName.equals("*"))
        {
            return childElements;
        }

        int i = 0;
        while (i < childElements.size())
        {
            Element childElement = childElements.getElement(i);
            if (!namespaceURI.equals("*")
                && !namespaceURI.equals(childElement.getNamespaceURI()))
            {
                childElements.remove(i);
                continue;
            }
            if (!localName.equals("*")
                && !childElement.getTagName().equals(localName))
            {
                childElements.remove(i);
                continue;
            }
            i++;
        }
        return childElements;
    }

    /**
     * Gets a <code>List&lt;Element&gt;</code> that contains all the child
     * <code>Element</code> s of the provided <code>Node</code>. If there
     * are no child <code>Element</code> s (or if the type of the provided
     * <code>Node</code> prohibits it from having <code>Element</code>
     * children), an empty <code>List&lt;Element&gt;</code> is returned.
     * 
     * @param parentNode
     *        the <code>Node</code> whose child <code>Element</code> s are
     *        to be listed
     * @return the <code>List&lt;Element&gt;</code> containing all the child
     *         <code>Element</code> s of the <code>parentNode</code>
     */
    public static List<Element> getChildElementList(Node parentNode)
    {
        NodeList allChildNodes = parentNode.getChildNodes();
        List<Element> childElements = new ArrayList<Element>();

        for (int i = 0; i < allChildNodes.getLength(); i++)
        {
            Node node = allChildNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                childElements.add((Element)node);
            }
        }

        return childElements;
    }

    /**
     * Gets a <code>List&lt;Element&gt;</code> of all the <code>Element</code>
     * s with a given tag name that are <i>direct </i> children of the specified
     * <code>Node</code>. If there are no matching child <code>Element</code>
     * s, an empty <code>List&lt;Element&gt;</code> is returned.
     * 
     * @param parentNode
     *        the <code>Node</code> whose children are to be listed
     * @param name
     *        the <code>String</code> to match against the
     *        <code>Element</code> tag names. The special value "*" matches
     *        any tag name.
     * @return the <code>List&lt;Element&gt;</code> containing the matching
     *         children
     */
    public static List<Element> getChildElementListByTagName(Node parentNode,
                                                             String name)
    {
        return getChildElementListByTagNameNS(parentNode, "*", name);
    }

    /**
     * Gets a <code>List&lt;Element&gt;</code> of all the <code>Element</code>
     * s with a given namespace URI and local name that are <i>direct </i>
     * children of the specified <code>Node</code>. If there are no matching
     * child <code>Element</code>s, an empty <code>List&lt;Element&gt;</code>
     * is returned.
     * 
     * @param parentNode
     *        the <code>Node</code> whose children are to be listed
     * @param namespaceURI
     *        the <code>String</code> to match against the
     *        <code>Element</code> namespace URIs. The special value "*"
     *        matches any namespace URI.
     * @param localName
     *        the <code>String</code> to match against the
     *        <code>Element</code> tag names. The special value "*" matches
     *        any tag name.
     * @return the <code>List&lt;Element&gt;</code> containing the matching
     *         children
     */
    public static List<Element> getChildElementListByTagNameNS(Node parentNode,
                                                               String namespaceURI,
                                                               String localName)
    {
        List<Element> childElements = getChildElementList(parentNode);
        if (namespaceURI.equals("*") && localName.equals("*"))
        {
            return childElements;
        }

        int i = 0;
        while (i < childElements.size())
        {
            Element childElement = childElements.get(i);
            if (!namespaceURI.equals("*")
                && !namespaceURI.equals(childElement.getNamespaceURI()))
            {
                childElements.remove(i);
                continue;
            }
            if (!localName.equals("*")
                && !childElement.getTagName().equals(localName))
            {
                childElements.remove(i);
                continue;
            }
            i++;
        }
        return childElements;
    }
    
    /**
     * Gets the first <code>Element</code> with the given tag name that is a
     * direct child of the specified <code>Node</code>.  If there is no
     * matching child <code>Element</code>, <code>null</code> is returned.
     * 
     * @param parentNode the <code>Node</code> whose child is to be fetched
     * @param name the <code>String</code> to match against the
     *             <code>Element</code> tag names.  The special value "*"
     *             matches any tag name.
     * @return the <code>Element</code> that is the first child that matches
     *         the provided name; <code>null</code> if there are no matches.
     */
    public static Element getFirstChildElementByTagName(
        Node parentNode,
        String name)
    {
        return getFirstChildElementByTagNameNS(parentNode, "*", name);
    }

    /**
     * Gets first <code>Element</code> with a given namespace URI and local name
     * that is a <i>direct</i> child of the specified <code>Node</code>.
     * If there is no matching child <code>Element</code>, <code>null</code> is
     * returned.
     * 
     * @param parentNode the <code>Node</code> whose child is to be fetched
     * @param namespaceURI the <code>string</code> to match against the
     *                     <code>Element</code> namespace URIs.  The special
     *                     value "*" matches any namespaceURI.
     * @param localName the <code>String</code> to match against the
     *                  <code>Element</code> tag names.  The special value "*"
     *                  matches any tag name.
     * @return the <code>Element</code> that is the first child that matches
     *         the provided name; <code>null</code> if there are no matches.
     */
    public static Element getFirstChildElementByTagNameNS(
        Node parentNode,
        String namespaceURI,
        String localName)
    {
        Node curNode = parentNode.getFirstChild();
        while (curNode != null)
        {
            if (curNode.getNodeType() != Node.ELEMENT_NODE)
            {
                curNode = curNode.getNextSibling();
                continue;
            }
            if (!namespaceURI.equals("*")
                && !curNode.getNamespaceURI().equals(namespaceURI))
            {
                curNode = curNode.getNextSibling();
                continue;
            }
            if (!localName.equals("*")
                && !curNode.getNodeName().equals(localName))
            {
                curNode = curNode.getNextSibling();
                continue;
            }
            // Success!
            return (Element) curNode;
        }
        // Failure!
        return null;
    }

    /**
     * Gets the first content node ({@link org.w3c.dom.Text Text} or
     * {@link org.w3c.dom.CDATASection CDATASection} that is a <i>direct</i>
     * child of the specified <code>Node</code>.  If there is no immediate child
     * content, <code>null</code> is returned.
     * 
     * @param parentNode the <code>Node</code> whose child content is to be
     *                   fetched
     * @return the fetched content
     */
    public static CharacterData getFirstChildContent(Node parentNode)
    {
        Node candidateNode = parentNode.getFirstChild();
        while (candidateNode != null
            && candidateNode.getNodeType() != Node.TEXT_NODE
            && candidateNode.getNodeType() != Node.CDATA_SECTION_NODE)
        {
            candidateNode = candidateNode.getNextSibling();
        }
        return (CharacterData) candidateNode;
    }
    
    /**
     * Creates a named element as the child of the specified parent node.
     * 
     * @param parentNode the node to which the newly created child element is to
     *                   be attached
     * @param childName the name with which to tag the new child element
     * @return the created child element.
     */
    public static Element createChildElement(Node parentNode, String childName)
    {
        Element childElement =
            parentNode.getOwnerDocument().createElement(childName);
        parentNode.appendChild(childElement);
        return childElement;
    }
    
    /**
     * Creates a named attribute attached to the specified parent element.
     * 
     * @param parentElement the element to which the newly created attribute is
     *                      to be attached
     * @param attributeName the name with which to label the new attribute
     * @return the created attribute.
     */
    public static Attr createAttribute(
        Element parentElement,
        String attributeName)
    {
        Attr childAttr =
            parentElement.getOwnerDocument().createAttribute(attributeName);
        parentElement.setAttributeNode(childAttr);
        return childAttr;
    }
    
    /**
     * Writes the provided XML document out to the specified output stream.
     * 
     * @param doc the document to be written
     * @param outputStream the stream to which the document is to be written
     */
    public static void writeDOMtoStream(Document doc, OutputStream outputStream)
    {
        try
        {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output stream
            Result result = new StreamResult(outputStream);

            // Write the DOM document to the output stream
            Transformer xformer =
                TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);

        }
        catch (TransformerConfigurationException tce)
        {
            // Error generated by the parser
            System.out.println("* Transformer Factory error");
            System.out.println("  " + tce.getMessage());

            // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null)
            {
                x = tce.getException();
            }
            x.printStackTrace();

        }
        catch (TransformerException te)
        {
            // Error generated by the parser
            System.out.println("* Transformation error");
            System.out.println("  " + te.getMessage());

            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null)
            {
                x = te.getException();
            }
            x.printStackTrace();
        }

    }

    /**
     * Writes an XML file from a DOM.
     * 
     * @param doc the document to write
     * @param file the file to be written to
     */
    public static void writeDOMtoFile(Document doc, File file)
    {
        try
        {
            OutputStream outputStream = new FileOutputStream(file);
            writeDOMtoStream(doc, outputStream);
        }
        catch (FileNotFoundException fnfe)
        {
            System.err.println("Could not write XML to " + file.getName()
                               + "\n File not found.");
            System.err.println("  " + fnfe.getMessage());
            fnfe.printStackTrace();

        }
    }

    /**
     * Writes an XML file from a DOM.
     * @param doc The document to write.
     * @param filename The filename of the xml file.
     */
    public static void writeDOMtoFile(Document doc, String filename)
    {
        // Prepare the output file
        File file = new File(filename);
        writeDOMtoFile(doc, file);
    }
    
    /**
     * Writes an XML file from a DOM, overwriting existing files only if
     * specified.
     * 
     * @param doc
     *        the document to write
     * @param filename
     *        the path to the xml file
     * @param overwrite
     *        whether to overwrite existing files
     * @throws IOException
     *         if <code>overwrite</code> is <code>false</code> and the
     *         specified file already exists
     */
    public static void writeDOMtoFile(Document doc,
                                      String filename,
                                      boolean overwrite) throws IOException
    {
        File file = new File(filename);
        if (!overwrite && file.exists())
        {
            throw new IOException("File \"" + filename + "\" already exists.");
        }
        writeDOMtoFile(doc, file);
    }

    /**
     * Exports the specified exportable data as the root element in an XML file.
     * If the file already exists, it is overwritten. If the specified file
     * location cannot be found, an error is printed, but no exception is
     * thrown.
     * 
     * @param filename
     *        the name of the XML file to be written
     * @param rootElementName
     *        the name to bestow upon the root element
     * @param exportable
     *        the data to be exported.
     * @throws XMLException
     *         if the data fails to export itself.
     */
    public static void writeAsRoot(String filename,
                                   String rootElementName,
                                   XMLExportable exportable)
        throws XMLException
    {
        writeAsRoot(new File(filename), rootElementName, exportable);
    }

    /**
     * Exports the specified exportable data as the root element in an XML file.
     * If the file already exists, it is overwritten. If the specified file
     * location cannot be found, an error is printed, but no exception is
     * thrown.
     * 
     * @param file
     *        the XML file to be written
     * @param rootElementName
     *        the name to bestow upon the root element
     * @param exportable
     *        the data to be exported.
     * @throws XMLException
     *         if the data fails to export itself.
     */
    public static void writeAsRoot(File file,
                                   String rootElementName,
                                   XMLExportable exportable)
        throws XMLException
    {
        Document document = createDOMInMemory();
        exportable.exportAsXML(createRootElement(document, rootElementName));
        writeDOMtoFile(document, file);
    }
    
    /**
     * Verifies that the specified <code>Node</code> is an <code>Element</code>,
     * and returns a handle to it as such.
     * 
     * @param dataNode the node that is supposed to be an <code>Element</code>
     * @return the <code>dataNode</code> as cast to an <code>Element</code>.
     * @throws XMLException if the provided <code>Node</code> is not actually an
     *                      <code>Element</code>
     */
    public static Element verifyNodeAsElement(Node dataNode) throws XMLException
    {
        return verifyNodeAsElement(dataNode, null);
    }
    
    /**
     * Verifies that the specified <code>Node</code> is an <code>Element</code>,
     * and returns a handle to it as such.
     * 
     * @param dataNode the node that is supposed to be an <code>Element</code>
     * @param mappingClass the class that is attempting to use the
     *                     <code>dataNode</code> as an <code>Element</code>
     *                     (used when reporting errors)
     * @return the <code>dataNode</code> as cast to an <code>Element</code>.
     * @throws XMLException if the provided <code>Node</code> is not actually an
     *                      <code>Element</code>
     */
    public static Element verifyNodeAsElement(Node dataNode, Class mappingClass)
            throws XMLException
    {
        String mappingClassName =
            (mappingClass == null ? getLoaderClassName() : mappingClass.getName());
        if (dataNode.getNodeType() != Node.ELEMENT_NODE) { throw new XMLException(
                "Invalid Node type.  " + mappingClassName
                        + " can only be loaded from/exported as an Element."); }
        return (Element) dataNode;
    }
    
    /**
     * Checks whether the specified element encodes a self-describing object.
     * 
     * @param element
     *        the element suspected of harbouring a self-describing object
     * @return whether the element does code for a self-describing object.
     */
    public static boolean isSelfDescribingObject(Element element)
    {
        return element.hasAttribute(SELF_DESC_CLASS_ATTRIBUTE_NAME);
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code> that is a child of the provided parent node.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @return the loaded object
     * @throws XMLException
     *         if the specified child cannot be found, or if the self-describing
     *         class attribute could not be loaded, or if the instanciated
     *         object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadSelfDescribingObjectFromChild(Node, String, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingObjectFromChild(Node parentNode,
                                                                                                String elementName)
        throws XMLException,
            InitializationException
    {
        return loadSelfDescribingObject(getRequiredElement(parentNode,
                                                           elementName), null);
    }

    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code> that is a child of the provided parent node.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the specified child cannot be found, or if the self-describing
     *         class attribute could not be loaded, or if the instanciated
     *         object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingObjectFromChild(Node parentNode,
                                                                                                String elementName,
                                                                                       Class loaderClass)
        throws XMLException,
            InitializationException
    {
        return loadSelfDescribingObject(getRequiredElement(parentNode,
                                                           elementName,
                                                           loaderClass),
                                        loaderClass);
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code> that is a child of the provided parent node. If
     * the specified element does not exist, <code>missingMeaning</code> is
     * returned instead.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param <M>
     *        the class of the object to be returned if the child is not present
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param missingMeaning
     *        the object to be returned if the specified child is not present
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Superclass extends XMLLoadable, M extends Superclass> Superclass loadSelfDescribingObjectFromChildIfPresent(Node parentNode,
                                                                                                                               String elementName,
                                                                                                                               M missingMeaning)
        throws XMLException,
            InitializationException
    {
        Element childElement = getFirstChildElementByTagName(parentNode, elementName);
        if (childElement == null)
        {
            return missingMeaning;
        }
        return loadSelfDescribingObject(childElement, null);
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute}that
     * specifies the name of the class (an implementor of the
     * <code>XMLLoadable</code> interface) that can be used to load the data
     * contained within the <code>Element</code>. The type of the returned
     * object is determined by how the caller uses it; if such use is
     * incompatible with the class read from the data (i.e. the return type is
     * not a superclass of the discovered class), a
     * <code>RuntimeException</code> will be thrown.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingObject(Element dataElement)
        throws XMLException,
            InitializationException
    {
        return loadSelfDescribingObject(dataElement, null);
    }

    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute}that
     * specifies the name of the class (an implementor of the
     * <code>XMLLoadable</code> interface) that can be used to load the data
     * contained within the <code>Element</code>. The type of the returned
     * object is determined by how the caller uses it; if such use is
     * incompatible with the class read from the data (i.e. the return type is
     * not a superclass of the discovered class), a
     * <code>RuntimeException</code> will be thrown.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingObject(Element dataElement,
                                                                                       Class loaderClass)
        throws XMLException,
            InitializationException
    {
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              loaderClass);

        try
        {
            return loadObject((Class< ? extends Superclass>)objectClass,
                              dataElement,
                              loaderClass);
        }
        catch (ClassCastException cce)
        {
            String loaderClassName = 
                (loaderClass == null ? getLoaderClassName() : loaderClass.getName());
            throw new RuntimeException("Programming Error:  the specified"
                                       + " self-describing data type "
                                       + objectClass.getName()
                                       + " doesn't subclass the class or"
                                       + " interface required by "
                                       + loaderClassName + ".", cce);
        }
    }

    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute} that
     * specifies the name of the class that can be used to load the data
     * contained within the <code>Element</code>.
     * <p>
     * If the provided data is not an element, or if the self-describing
     * attribute is missing, or if the class so specified is not assignable to
     * <code>requiredSuperclass</code> (which itself must be assignable to the
     * implicit return type of the method), the element is deemed to be
     * non-self-describing, and <code>missingMeaning</code> is returned
     * instead of attempting to load a self-describing object.
     * <p>
     * The type of the returned object is determined by how the caller uses it;
     * if such use is incompatible with the class specified as the required
     * superclass (i.e. the return type is not assignable from
     * <code>requiredSuperclass</code>), a <code>RuntimeException</code>
     * will be thrown.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param <M>
     *        the class of the provided object to be returned if the desired
     *        object is not found
     * @param requiredSuperclass
     *        the class to which the loaded object must be assignable; if the
     *        class specified by the self-desc attribute is not assignable to
     *        this class, the self-describing data is ignored, and the
     *        <code>missingMeaning</code> is returned.
     * @param dataNode
     *        the self-describing XML data from which an object will be loaded
     * @param missingMeaning
     *        the object to return if the element is found to be not
     *        self-describing (or not describing the correct class of object).
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Superclass extends XMLLoadable, M extends Superclass> Superclass loadSelfDescribingObjectIfPresent(Class< ? extends Superclass> requiredSuperclass,
                                                                                                                      Node dataNode,
                                                                                                                      M missingMeaning)
        throws XMLException,
            InitializationException
    {
        if (dataNode.getNodeType() != Node.ELEMENT_NODE)
        {
            return missingMeaning;
        }
        Element dataElement = (Element)dataNode;
        if (!isSelfDescribingObject(dataElement))
        {
            return missingMeaning;
        }
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              null);
        if (!requiredSuperclass.isAssignableFrom(objectClass))
        {
            return missingMeaning;
        }
        
        try
        {
            return loadObject((Class< ? extends Superclass>)objectClass,
                              dataElement,
                              null);
        }
        catch (ClassCastException cce)
        {
            // This should not occur, since we checked above that
            // requiredSuperclass is assignable from the loaded class
            throw new RuntimeException("Programming Error:  the specified"
                                           + " self-describing data type "
                                           + objectClass.getName()
                                           + " doesn't subclass "
                                           + requiredSuperclass.getName() + ".",
                                       cce);
        }
    }

    /**
     * Instanciates and loads the object represented by the specified XML data.
     * The loaded object will be of the specified class (which must implement
     * the <code>XMLLoadable</code> interface). If the specified class has a
     * constructor that takes (<code>Node</code>) as arguments, that method
     * of instanciation and loading will be preferred; otherwise, the class's
     * default constructor will be used to instanciate the object, and
     * {@link XMLLoadable#loadFromXML(Node)}will be used to load the data.
     * 
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see XMLLoadable
     */
    public static <InstanceType extends XMLLoadable> InstanceType loadObject(Class<InstanceType> objectClass,
                                                                             Node dataNode)
        throws XMLException,
            InitializationException
    {
        return loadObject(objectClass, dataNode, null);
    }

    /**
     * Instanciates and loads the object represented by the specified XML data.
     * The loaded object will be of the specified class (which must implement
     * the <code>XMLLoadable</code> interface). If the specified class has a
     * constructor that takes (<code>Node</code>) as arguments, that method
     * of instanciation and loading will be preferred; otherwise, the class's
     * default constructor will be used to instanciate the object, and
     * {@link XMLLoadable#loadFromXML(Node)}will be used to load the data.
     * 
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see XMLLoadable
     */
    public static <InstanceType extends XMLLoadable> InstanceType loadObject(Class<InstanceType> objectClass,
                                                                             Node dataNode,
                                                                             Class loaderClass)
        throws XMLException,
            InitializationException
    {
        String loaderClassName = 
            (loaderClass == null ? getLoaderClassName() : loaderClass.getName());
        try
        {
            // Try the one-step loading approach
            Constructor<InstanceType> oneStepConstructor;
            try
            {
                oneStepConstructor = objectClass.getConstructor(new Class[]
                {
                    Node.class
                });
            }
            catch (NoSuchMethodException nsme)
            {
                // Ok, we'll try the two-step method, then
                oneStepConstructor = null;
            }
            if (oneStepConstructor != null)
            {
                try
                {
                    return oneStepConstructor.newInstance(new Object[]
                    {
                        dataNode
                    });
                }
                catch (IllegalAccessException iae)
                {
                    // We'll try the two-step method
                }
            }

            // Ok, try the 2-step approach
            InstanceType emptyInstance = objectClass.newInstance();
            emptyInstance.loadFromXML(dataNode);
            return emptyInstance;
        }
        catch (IllegalAccessException iae)
        {
            throw new RuntimeException("Programming Error.  "
                                           + loaderClassName
                                           + " requires that the either the"
                                           + " default constructor or the"
                                           + " constructor with (Node)"
                                           + " arguments be accessible in"
                                           + " the " + objectClass
                                           + " class.",
                                       iae);
        }
        catch (InvocationTargetException ite)
        {
            Throwable cause = ite.getCause();
            if (cause != null)
            {
                if (cause instanceof XMLException)
                {
                    throw (XMLException)cause;
                }
                if (cause instanceof InitializationException)
                {
                    throw (InitializationException)cause;
                }
                throw new RuntimeException("Programming Error.  The "
                                           + objectClass.getName()
                                           + " constructor with (Node)"
                                           + " arguments threw an"
                                           + " unexpected exception.", cause);
            }
            throw new RuntimeException("Programming Error.  The "
                                       + objectClass.getName()
                                       + " constructor with (Node) arguments"
                                       + " threw an unexpected exception.");
        }
        catch (InstantiationException ie)
        {
            throw new RuntimeException("Programming Error. "
                                       + loaderClassName
                                       + " requires that the "
                                       + objectClass.getName()
                                       + " class be instanciable.", ie);
        }
    }

    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredSelfDescribingObject(Element, Object)}, which does
     * the same job as this method but in a more general fashion. This method's
     * parameter bounds enforce an exact match between the configuration type of
     * the loaded object and the provided <code>configuration</code> object.
     * <code>loadConfiguredSelfDescribingObject()</code> allows for
     * subclassing (and has a different name from non-configured loaders that
     * prevents confusion between configuration objects and class objects).
     * 
     * @param <Configuration>
     *        the type of configuration data to be used in loading the object
     * @param <S>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Configuration, S extends XMLConfiguredLoadable<Configuration>> S loadSelfDescribingObject(Element dataElement,
                                                                                                             Configuration configuration)
        throws XMLException,
            InitializationException
    {
        return loadConfiguredSelfDescribingObject(dataElement, configuration);
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute}that
     * specifies the name of the class (an implementor of the
     * <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> interface) that
     * can be used to load the data contained within the <code>Element</code>.
     * The type of the returned object is determined by how the caller uses it;
     * if such use is incompatible with the class read from the data (i.e. the
     * return type is not a superclass of the discovered class), a
     * <code>RuntimeException</code> will be thrown.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredSelfDescribingObject(Element dataElement,
                                                                                                                           C configuration)
        throws XMLException,
            InitializationException
    {
        // It's not clear why, but there is bounds mismatch when attempting to call the method with the loaderClass argument
        //return loadConfiguredSelfDescribingObject(dataElement, configuration, (Class)null);
        // So we'll do it the copy-and-paste way...
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              null);

        try
        {
            return loadConfiguredObject((Class< ? extends R>)objectClass,
                                        dataElement,
                                        configuration,
                                        null);
        }
        catch (ClassCastException cce)
        {
            String loaderClassName = getLoaderClassName();
            throw new RuntimeException("Programming Error:  the specified"
                                       + " self-describing data type "
                                       + objectClass.getName()
                                       + " doesn't subclass the class or"
                                       + " interface required by "
                                       + loaderClassName + ".", cce);
        }
    }

    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredSelfDescribingObject(Element, Object, Class)},
     * which does the same job as this method but in a more general fashion.
     * This method's parameter bounds enforce an exact match between the
     * configuration type of the loaded object and the provided
     * <code>configuration</code> object.
     * <code>loadConfiguredSelfDescribingObject()</code> allows for
     * subclassing (and has a different name from non-configured loaders that
     * prevents confusion between configuration objects and class objects).
     * 
     * @param <Configuration>
     *        the type of configuration data to be used in loading the object
     * @param <S>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Configuration, S extends XMLConfiguredLoadable<Configuration>> S loadSelfDescribingObject(Element dataElement,
                                                                                                             Configuration configuration,
                                                                                                             Class loaderClass)
        throws XMLException,
            InitializationException
    {
        return loadConfiguredSelfDescribingObject(dataElement,
                                                  configuration,
                                                  loaderClass);
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute}that
     * specifies the name of the class (an implementor of the
     * <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> interface) that
     * can be used to load the data contained within the <code>Element</code>.
     * The type of the returned object is determined by how the caller uses it;
     * if such use is incompatible with the class read from the data (i.e. the
     * return type is not a superclass of the discovered class), a
     * <code>RuntimeException</code> will be thrown.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param dataElement
     *        the self-describing XML data from which an object will be loaded
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredSelfDescribingObject(Element dataElement,
                                                                                                                           C configuration,
                                                                                                                           Class loaderClass)
        throws XMLException,
            InitializationException
    {
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              loaderClass);

        try
        {
            return loadConfiguredObject((Class< ? extends R>)objectClass,
                                        dataElement,
                                        configuration,
                                        loaderClass);
        }
        catch (ClassCastException cce)
        {
            String loaderClassName = (loaderClass == null
                ? getLoaderClassName()
                : loaderClass.getName());
            throw new RuntimeException("Programming Error:  the specified"
                                       + " self-describing data type "
                                       + objectClass.getName()
                                       + " doesn't subclass the class or"
                                       + " interface required by "
                                       + loaderClassName + ".", cce);
        }
    }
    
    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code>. An <code>Element</code> is self-describing iff
     * it contains the
     * {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME appropriate attribute} that
     * specifies the name of the class that can be used to load the data
     * contained within the <code>Element</code>.
     * <p>
     * If the provided data is not an element, or if the self-describing
     * attribute is missing, or if the class so specified is not assignable to
     * <code>requiredSuperclass</code> (which itself must be assignable to the
     * implicit return type of the method), the element is deemed to be
     * non-self-describing, and <code>missingMeaning</code> is returned
     * instead of attempting to load a self-describing object.
     * <p>
     * The type of the returned object is determined by how the caller uses it;
     * if such use is incompatible with the class specified as the required
     * superclass (i.e. the return type is not assignable from
     * <code>requiredSuperclass</code>), a <code>RuntimeException</code>
     * will be thrown.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param <M>
     *        the class of the provided object to be returned if the desired
     *        object is not found
     * @param requiredSuperclass
     *        the class to which the loaded object must be assignable; if the
     *        class specified by the self-desc attribute is not assignable to
     *        this class, the self-describing data is ignored, and the
     *        <code>missingMeaning</code> is returned.
     * @param dataNode
     *        the self-describing XML data from which an object will be loaded
     * @param configuration
     *        the configuration object to be used when loading the
     *        self-describing object
     * @param missingMeaning
     *        the object to return if the element is found to be not
     *        self-describing (or not describing the correct class of object).
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config, M extends R> R loadConfiguredSelfDescribingObjectIfPresent(Class< ? extends R> requiredSuperclass,
                                                                                                                                                 Node dataNode,
                                                                                                                                                 C configuration,
                                                                                                                                                 M missingMeaning)
        throws XMLException,
            InitializationException
    {
        if (dataNode.getNodeType() != Node.ELEMENT_NODE)
        {
            return missingMeaning;
        }
        Element dataElement = (Element)dataNode;
        if (!isSelfDescribingObject(dataElement))
        {
            return missingMeaning;
        }
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              null);
        if (!requiredSuperclass.isAssignableFrom(objectClass))
        {
            return missingMeaning;
        }
        
        try
        {
            return loadConfiguredObject((Class< ? extends R>)objectClass,
                                        dataElement,
                                        configuration);
        }
        catch (ClassCastException cce)
        {
            // This should not occur, since we checked above that
            // requiredSuperclass is assignable from the loaded class
            throw new RuntimeException("Programming Error:  the specified"
                                           + " self-describing data type "
                                           + objectClass.getName()
                                           + " doesn't subclass "
                                           + requiredSuperclass.getName() + ".",
                                       cce);
        }
    }

    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code> that is a child of the provided parent node.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param configuration
     *        the configuration object to be used when loading the
     *        self-describing object
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the specified child cannot be found, or if the self-describing
     *         class attribute could not be loaded, or if the instanciated
     *         object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredSelfDescribingObjectFromChild(Node parentNode,
                                                                                                                                    String elementName,
                                                                                                                                    C configuration,
                                                                                                                                    Class loaderClass)
        throws XMLException,
            InitializationException
    {
        Element dataElement = getRequiredElement(parentNode,
                                                 elementName,
                                                 loaderClass);
        Class objectClass = getClassAttribute(dataElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              loaderClass);

        try
        {
            return loadConfiguredObject((Class< ? extends R>)objectClass,
                                        dataElement,
                                        configuration,
                                        loaderClass);
        }
        catch (ClassCastException cce)
        {
            String loaderClassName = (loaderClass == null
                ? getLoaderClassName()
                : loaderClass.getName());
            throw new RuntimeException("Programming Error:  the specified"
                                       + " self-describing data type "
                                       + objectClass.getName()
                                       + " doesn't subclass the class or"
                                       + " interface required by "
                                       + loaderClassName + ".", cce);
        }
    }

    /**
     * Instanciates and loads an object from a self-describing XML
     * <code>Element</code> that is a child of the provided parent node. If
     * the specified element does not exist, <code>missingMeaning</code> is
     * returned instead.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param <M>
     *        the class of the provided object to be returned if the desired
     *        object is not found
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param configuration
     *        the configuration object to be used when loading the
     *        self-describing object
     * @param missingMeaning
     *        the object to return if the specified child is found to be not
     *        self-describing (or not describing the correct class of object).
     * @return the loaded object
     * @throws XMLException
     *         if the self-describing class attribute could not be loaded, or if
     *         the instanciated object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config, M extends R> R loadConfiguredSelfDescribingObjectFromChildIfPresent(Node parentNode,
                                                                                                                                                          String elementName,
                                                                                                                                                          C configuration,
                                                                                                                                                          M missingMeaning)
        throws XMLException,
            InitializationException
    {
        Element childElement = getFirstChildElementByTagName(parentNode, elementName);
        if (childElement == null)
        {
            return missingMeaning;
        }
        if (!isSelfDescribingObject(childElement))
        {
            return missingMeaning;
        }
        Class objectClass = getClassAttribute(childElement,
                                              SELF_DESC_CLASS_ATTRIBUTE_NAME,
                                              null);

        try
        {
            return loadConfiguredObject((Class< ? extends R>)objectClass,
                                        childElement,
                                        configuration);
        }
        catch (ClassCastException cce)
        {
            // This should not occur, since we checked above that
            // requiredSuperclass is assignable from the loaded class
            throw new RuntimeException("Programming Error:  the specified"
                                       + " self-describing data type "
                                       + objectClass.getName()
                                       + " doesn't subclass the required"
                                       + " return type.", cce);
        }
    }

    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredObject(Class, Node, Object)}, which does the same
     * job as this method but in a more general fashion. This methods parameter
     * bounds enforce exact matches between the <code>objectClass</code> and
     * the return type and between the configuration type of the loaded object
     * and the provided <code>configuration</code> object.
     * <code>loadConfiguredObject()</code> allows for subclassing (and has a
     * different name from non-configured loaders that prevents confusion
     * between configuration objects and class objects).
     * 
     * @param <Configuration>
     *        the type of configuration data to be used in loading the object
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see #loadConfiguredObject(Class, Node, Object, Class)
     */
    public static <Configuration, InstanceType extends XMLConfiguredLoadable<Configuration>> InstanceType loadObject(Class<InstanceType> objectClass,
                                                                                                                                                                      Node dataNode,
                                                                                                                     Configuration configuration)
        throws XMLException,
            InitializationException
    {
        return loadObject(objectClass, dataNode, configuration, null);
    }

    /**
     * Instanciates and loads the object represented by the specified XML data,
     * possibly guided by the provided <code>configuration</code> information.
     * The loaded object will be of the specified class (which must implement
     * the <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> interface).
     * If the specified class has a constructor that takes (<code>Node</code>,
     * <code>Configuration</code>) as arguments, that method of instanciation
     * and loading will be preferred; otherwise, the class's default constructor
     * will be used to instanciate the object, and
     * {@link XMLConfiguredLoadable#loadFromXML(Node, Object)}will be used to
     * load the data.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of <code>R</code>).
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see XMLConfiguredLoadable
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredObject(Class< ? extends R> objectClass,
                                                                                                             Node dataNode,
                                                                                                             C configuration)
        throws XMLException,
            InitializationException
    {
        return loadConfiguredObject(objectClass,
                                    dataNode,
                                    configuration,
                                    (Class)null);
    }

    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredObject(Class, Node, Object, Class)}, which does
     * the same job as this method but in a more general fashion. This methods
     * parameter bounds enforce exact matches between the
     * <code>objectClass</code> and the return type and between the
     * configuration type of the loaded object and the provided
     * <code>configuration</code> object. <code>loadConfiguredObject()</code>
     * allows for subclassing (and has a different name from non-configured
     * loaders that prevents confusion between configuration objects and class
     * objects).
     * 
     * @param <Configuration>
     *        the type of configuration data to be used in loading the object
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see #loadConfiguredObject(Class, Node, Object, Class)
     */
    @SuppressWarnings(
    {
        "unchecked"
    })
    // Because Class.getConstructors returns non-parameterized constructors
    public static <Configuration, InstanceType extends XMLConfiguredLoadable<Configuration>> InstanceType loadObject(Class<InstanceType> objectClass,
                                                                                                                     Node dataNode,
                                                                                                                     Configuration configuration,
                                                                                                                     Class loaderClass)
        throws XMLException,
            InitializationException
    {
        return loadConfiguredObject(objectClass,
                                    dataNode,
                                    configuration,
                                    loaderClass);
    }
    
    /**
     * Instanciates and loads the object represented by the specified XML data,
     * possibly guided by the provided <code>configuration</code> information.
     * The loaded object will be of the specified class (which must implement
     * the <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> interface).
     * If the specified class has a constructor that takes (<code>Node</code>,
     * <code>Configuration</code>) as arguments, that method of instanciation
     * and loading will be preferred; otherwise, the class's default constructor
     * will be used to instanciate the object, and
     * {@link XMLConfiguredLoadable#loadFromXML(Node, Object)}will be used to
     * load the data.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of <code>R</code>).
     * @param dataNode
     *        the XML data from which the object will be loaded
     * @param configuration
     *        the <code>Configuration</code> object that guides the object in
     *        how to load itself
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the instanciated object cannot be loaded from the specified
     *         XML data
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @see XMLConfiguredLoadable
     */
    @SuppressWarnings(
    {
        "unchecked"
    })
    // Because Class.getConstructors returns non-parameterized constructors
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredObject(Class< ? extends R> objectClass,
                                                                                                             Node dataNode,
                                                                                                             C configuration,
                                                                                                             Class loaderClass)
        throws XMLException,
            InitializationException
    {
        String loaderClassName = (loaderClass == null
            ? getLoaderClassName()
            : loaderClass.getName());
        try
        {
            // Try the one-step loading approach
            Constructor< ? extends R> oneStepConstructor = null;
            Constructor< ? extends R>[] constructors = (Constructor< ? extends R>[])objectClass
                .getConstructors();
            for (int i = 0; i < constructors.length; i++)
            {
                Class< ? >[] parameters = constructors[i].getParameterTypes();
                if (parameters.length == 2
                    && parameters[0].isAssignableFrom(Node.class)
                    && parameters[1].isAssignableFrom(configuration.getClass()))
                {
                    oneStepConstructor = constructors[i];
                    break;
                }
            }
            if (oneStepConstructor != null)
            {
                try
                {
                    return oneStepConstructor.newInstance(new Object[]
                    {
                        dataNode, configuration
                    });
                }
                catch (IllegalAccessException iae)
                {
                    // We'll try the two-step method
                }
            }

            // Ok, try the 2-step approach
            R emptyInstance = objectClass.newInstance();
            emptyInstance.loadFromXML(dataNode, configuration);
            return emptyInstance;
        }
        catch (IllegalAccessException iae)
        {
            throw new RuntimeException("Programming Error.  " + loaderClassName
                                       + " requires that the either the"
                                       + " default constructor or the"
                                       + " constructor with (Node, "
                                       + configuration.getClass().getName()
                                       + ")" + " arguments be accessible in"
                                       + " the " + objectClass + " class.", iae);
        }
        catch (InvocationTargetException ite)
        {
            Throwable cause = ite.getCause();
            if (cause != null)
            {
                if (cause instanceof XMLException)
                {
                    throw (XMLException)cause;
                }
                if (cause instanceof InitializationException)
                {
                    throw (InitializationException)cause;
                }
                throw new RuntimeException("Programming Error.  The "
                                           + objectClass.getName()
                                           + " constructor with (Node, "
                                           + configuration.getClass().getName()
                                           + ")" + " arguments threw an"
                                           + " unexpected exception.", cause);
            }
            throw new RuntimeException("Programming Error.  The "
                                       + objectClass.getName()
                                       + " constructor with (Node, "
                                       + configuration.getClass().getName()
                                       + ") arguments"
                                       + " threw an unexpected exception.");
        }
        catch (InstantiationException ie)
        {
            throw new RuntimeException("Programming Error. " + loaderClassName
                                       + " requires that the "
                                       + objectClass.getName()
                                       + " class be instanciable.\n", ie);
        }
    }

    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object from the self-describing root
     * <code>Element</code> of the document.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, if the self-describing class
     *         attribute could not be loaded, or if the instanciated object
     *         could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingFromRoot(String filename)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadSelfDescribingFromRoot(filename, null);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object from the self-describing root
     * <code>Element</code> of the document.
     * 
     * @param <Superclass>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, if the self-describing
     *         class attribute could not be loaded, or if the instanciated
     *         object could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <Superclass extends XMLLoadable> Superclass loadSelfDescribingFromRoot(String filename,
                                                                                         Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        Document document = openAsDOM(filename);
        return loadSelfDescribingObject(document.getDocumentElement(),
                                        loaderClass);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object from the self-describing root
     * <code>Element</code> of the document.
     * 
     * @param <C>
     *        the type of configuration object used to load the object
     * @param <L>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, if the self-describing class
     *         attribute could not be loaded, or if the instanciated object
     *         could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <C, L extends XMLConfiguredLoadable<C>> L loadSelfDescribingFromRoot(String filename,
                                                                                       C config)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadSelfDescribingFromRoot(filename, config, null);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object from the self-describing root
     * <code>Element</code> of the document.
     * 
     * @param <C>
     *        the type of configuration object used to load the object
     * @param <L>
     *        the class of the object that will be returned; the actual object
     *        may be a subclass of this class
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, if the self-describing class
     *         attribute could not be loaded, or if the instanciated object
     *         could not be loaded from the data.
     * @throws InitializationException
     *         if the self-describing class cannot be found, or if the
     *         instanciated object could not be initialized.
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadSelfDescribingObject(Element, Class)
     */
    public static <C, L extends XMLConfiguredLoadable<C>> L loadSelfDescribingFromRoot(String filename,
                                                                                       C config,
                                                                                       Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        Document document = openAsDOM(filename);
        return loadConfiguredSelfDescribingObject(document.getDocumentElement(),
                                                  config,
                                                  loaderClass);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object represented by the root
     * <code>Element</code> of the document.
     * 
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <InstanceType extends XMLLoadable> InstanceType loadObjectFromRoot(Class<InstanceType> objectClass,
                                                                                     String filename)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadObjectFromRoot(objectClass, filename, null);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object represented by the root
     * <code>Element</code> of the document.
     * 
     * @param <InstanceType>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <InstanceType extends XMLLoadable> InstanceType loadObjectFromRoot(Class<InstanceType> objectClass,
                                                                                     String filename,
                                                                                     Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        Document document = openAsDOM(filename);
        return loadObject(objectClass,
                          document.getDocumentElement(),
                          loaderClass);
    }
    
    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredObjectFromRoot(Class, String, Object)}, which does
     * the same job as this method but in a more general fashion. This method's
     * parameter bounds enforce exact matches between the
     * <code>objectClass</code> and the return type and between the
     * configuration type of the loaded object and the provided
     * <code>configuration</code> object.
     * <code>loadConfiguredObjectFromRoot()</code> allows for subclassing (and
     * has a different name from non-configured loaders that prevents confusion
     * between configuration objects and class objects).
     * 
     * @param <C>
     *        the type of configuration object used to load the object
     * @param <L>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <C, L extends XMLConfiguredLoadable<C>> L loadObjectFromRoot(Class<L> objectClass,
                                                                               String filename,
                                                                               C config)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadConfiguredObjectFromRoot(objectClass, filename, config);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object represented by the root
     * <code>Element</code> of the document.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of <code>R</code>).
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredObjectFromRoot(Class< ? extends R> objectClass,
                                                                                                                     String filename,
                                                                                                                     C config)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadConfiguredObjectFromRoot(objectClass,
                                            filename,
                                            config,
                                            (Class)null);
    }

    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object represented by the root
     * <code>Element</code> of the document.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of <code>R</code>).
     * @param url
     *        the {@link URL} to open.
     * @param config
     *        the object used to configure the loaded object
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredObjectFromRoot(Class< ? extends R> objectClass,
                                                                                                                     URL url,
                                                                                                                     C config,
                                                                                                                     Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        Document document = openAsDOM(url);
        return loadConfiguredObject(objectClass,
                                    document.getDocumentElement(),
                                    config,
                                    loaderClass);
    }
    
    /**
     * This method has <em>almost</em> been deprecated in favour of
     * {@link #loadConfiguredObjectFromRoot(Class, String, Object, Class)},
     * which does the same job as this method but in a more general fashion.
     * This method's parameter bounds enforce exact matches between the
     * <code>objectClass</code> and the return type and between the
     * configuration type of the loaded object and the provided
     * <code>configuration</code> object.
     * <code>loadConfiguredObjectFromRoot()</code> allows for subclassing (and
     * has a different name from non-configured loaders that prevents confusion
     * between configuration objects and class objects).
     * 
     * @param <C>
     *        the type of configuration object used to load the object
     * @param <L>
     *        the type of object to be loaded
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of the <code>XMLLoadable</code> interface.
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <C, L extends XMLConfiguredLoadable<C>> L loadObjectFromRoot(Class<L> objectClass,
                                                                               String filename,
                                                                               C config,
                                                                               Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        return loadConfiguredObjectFromRoot(objectClass,
                                            filename,
                                            config,
                                            loaderClass);
    }
    
    /**
     * Opens the specified XML file, parses it into a DOM <code>Document</code>,
     * then instanciates and loads an object represented by the root
     * <code>Element</code> of the document.
     * 
     * @param <Config>
     *        the configuration type specified in the returned type's
     *        {@link XMLConfiguredLoadable} interface the type of configuration
     *        data to be used in loading the object
     * @param <R>
     *        the type of object to be loaded (an implementation/extension of
     *        the <code>XMLLoadable</code> interface).
     * @param <C>
     *        the type of the configuration object provided (a subclass of
     *        <code>Config</code>).
     * @param objectClass
     *        the class of the object to be loaded (an implementation/extension
     *        of <code>R</code>).
     * @param filename
     *        the name of the file to open ({@link File#File(String)})
     * @param config
     *        the object used to configure the loaded object
     * @param loaderClass
     *        the class that is attempting to load the object from XML (used
     *        when reporting errors)
     * @return the loaded object
     * @throws XMLException
     *         if the file cannot be parsed as XML, or if the instanciated
     *         object cannot be loaded from the specified XML data.
     * @throws InitializationException
     *         if the instanciated object cannot be initialized
     * @throws IOException
     *         if the file cannot be opened
     * @see #loadObject(Class, Node, Class)
     */
    public static <Config, R extends XMLConfiguredLoadable<Config>, C extends Config> R loadConfiguredObjectFromRoot(Class< ? extends R> objectClass,
                                                                                                                     String filename,
                                                                                                                     C config,
                                                                                                                     Class loaderClass)
        throws XMLException,
            InitializationException,
            IOException
    {
        Document document = openAsDOM(filename);
        return loadConfiguredObject(objectClass,
                                    document.getDocumentElement(),
                                    config,
                                    loaderClass);
    }
    
    /**
     * Marks the element (that is going to be exported, we hope) as being a
     * self-describing element that can be loaded by the specified class.
     * 
     * @param exportElement
     *        the element that is to be marked as loadable by the specified
     *        class
     * @throws XMLException
     *         if the element's
     *         {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME self-describing attribute}
     *         cannot be set.
     */
    public static void setAsSelfDescribing(Element exportElement)
        throws XMLException
    {
        exportAttribute(exportElement,
                        SELF_DESC_CLASS_ATTRIBUTE_NAME,
                        (Class)null);
    }
    
    /**
     * Marks the element (that is going to be exported, we hope) as being a
     * self-describing element that can be loaded by the specified class.
     * 
     * @param exportElement
     *        the element that is to be marked as loadable by the specified
     *        class
     * @param describedClass
     *        the class that can load the element
     * @throws XMLException
     *         if the element's
     *         {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME self-describing attribute}
     *         cannot be set.
     */
    public static void setAsSelfDescribing(Element exportElement,
                                           Class describedClass)
        throws XMLException
    {
        exportAttribute(exportElement,
                        SELF_DESC_CLASS_ATTRIBUTE_NAME,
                        describedClass);
    }
    
    /**
     * Marks the element (that is going to be exported, we hope) as being a
     * self-describing element that can be loaded by the specified class.
     * 
     * @param exportNode
     *        the element that is to be marked as loadable by the specified
     *        class
     * @throws XMLException
     *         if <code>exportElement</code> is not actually an element, or if
     *         the element's
     *         {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME self-describing attribute}
     *         cannot be set.
     */
    public static void setAsSelfDescribing(Node exportNode)
        throws XMLException
    {
        setAsSelfDescribing(verifyNodeAsElement(exportNode), null);
    }
    
    /**
     * Marks the element (that is going to be exported, we hope) as being a
     * self-describing element that can be loaded by the specified class.
     * 
     * @param exportNode
     *        the element that is to be marked as loadable by the specified
     *        class
     * @param describedClass
     *        the class that can load the element
     * @throws XMLException
     *         if <code>exportElement</code> is not actually an element, or if
     *         the element's
     *         {@linkplain #SELF_DESC_CLASS_ATTRIBUTE_NAME self-describing attribute}
     *         cannot be set.
     */
    public static void setAsSelfDescribing(Node exportNode,
                                           Class describedClass)
        throws XMLException
    {
        setAsSelfDescribing(verifyNodeAsElement(exportNode), describedClass);
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, an exception is thrown.
     * 
     * @param dataElement the element from which the attribute is to be fetched
     * @param attributeName the name of the attribute to be fetched
     * @return the fetched attribute.
     * @throws XMLException if the specified attribute is not present in the
     *                      provided <code>Element</code>.
     */
    public static Attr getRequiredAttribute(Element dataElement,
            String attributeName) throws XMLException
    {
        return getRequiredAttribute(dataElement, attributeName, null);
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, an exception is thrown.
     * 
     * @param dataElement the element from which the attribute is to be fetched
     * @param attributeName the name of the attribute to be fetched
     * @param loaderClass the class that is attempting to load the attribute
     *                    from XML (used when reporting errors)
     * @return the fetched attribute.
     * @throws XMLException if the specified attribute is not present in the
     *                      provided <code>Element</code>.
     */
    public static Attr getRequiredAttribute(Element dataElement,
            String attributeName, Class loaderClass) throws XMLException
    {
        String loaderClassName = 
            (loaderClass == null ? getLoaderClassName() : loaderClass.getName());
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null) { throw new XMLException("Invalid Node structure.\n"
                + loaderClassName + " requires that the " + attributeName
                + " attribute be present."); }
        return attr;
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Class</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Class</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>.
     * @throws InitializationException
     *         if the class specified by the loaded attribute cannot be located.
     */
    public static Class<?> getClassAttribute(Element dataElement,
                                          String attributeName)
        throws XMLException,
            InitializationException
    {
        return getClassAttribute(dataElement, attributeName, null);
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Class</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Class</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>.
     * @throws InitializationException
     *         if the class specified by the loaded attribute cannot be located.
     */
    public static Class<?> getClassAttribute(Element dataElement,
                                          String attributeName,
                                          Class loaderClass)
        throws XMLException,
            InitializationException
    {
        String className = getStringAttribute(dataElement,
                                              attributeName,
                                              loaderClass);
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new InitializationException("Could not find the class: "
                                              + className);
        }
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>boolean</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>boolean</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>boolean</code>.
     */
    public static boolean getBooleanAttribute(Element dataElement,
                                              String attributeName)
        throws XMLException
    {
        return getBooleanAttribute(dataElement, attributeName, null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>boolean</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>boolean</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>boolean</code>.
     */
    public static boolean getBooleanAttribute(Element dataElement,
                                              String attributeName,
                                              Class loaderClass)
        throws XMLException
    {
        XMLBoolean booleanXML = new XMLBoolean();
        booleanXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass));
        return booleanXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>boolean</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>boolean</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>boolean</code>.
     */
    public static boolean getBooleanAttribute(Element dataElement,
                                              String attributeName,
                                              boolean missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLBoolean booleanXML = new XMLBoolean();
        booleanXML.loadFromXML(attr);
        return booleanXML.getContents();
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as an
     * <code>N</code>, an exception is thrown.
     * 
     * @param <N>
     *        the type of number to be fetched
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param exampleInstance
     *        an example of the type of number to be fetched (from which the
     *        return type can be inferred)
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double</code>.
     */
    public static <N extends Number> N getNumberAttribute(Element dataElement,
                                                          String attributeName,
                                                          N exampleInstance,
                                                          Class loaderClass)
        throws XMLException
    {
        Attr attr = getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass);
        String numberString = attr.getValue();
        try
        {
            return NumberHelper.parse(numberString, exampleInstance);
        }
        catch (NumberFormatException nfe)
        {
            throw new XMLException("Invalid Node value.  " + numberString
                                   + " is not a valid representation of a "
                                   + exampleInstance.getClass() + ".");
        }
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as an
     * <code>N</code>, an exception is thrown.
     * 
     * @param <N>
     *        the type of number to be fetched
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param numberClass
     *        the concrete type of number to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double</code>.
     */
    public static <N extends Number> N getNumberAttribute(Element dataElement,
                                                          String attributeName,
                                                          Class<N> numberClass,
                                                          Class loaderClass)
        throws XMLException
    {
        Attr attr = getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass);
        String numberString = attr.getValue();
        N retval;
        try
        {
            retval = NumberHelper.parse(numberString, numberClass);
        }
        catch (NumberFormatException nfe)
        {
            throw new XMLException("Invalid Node value.  " + numberString
                                   + " is not a valid representation of a "
                                   + numberClass.getName() + ".");
        }
        if (retval == null)
        {
            throw new XMLException("Invalid Node value.  " + numberString
                                   + " could not be parsed as a "
                                   + numberClass.getName() + ".");
        }
        return retval;
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as an <code>N</code>, an exception
     * is thrown.
     * 
     * @param <N>
     *        the type of number to be fetched
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>boolean</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>N</code>.
     */
    public static <N extends Number> N getNumberAttribute(Element dataElement,
                                                          String attributeName,
                                                          N missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        String numberString = attr.getValue();
        N retval;
        try
        {
            retval = NumberHelper.parse(numberString, missingMeaning);
        }
        catch (NumberFormatException nfe)
        {
            throw new XMLException("Invalid Node value.  " + numberString
                                   + " is not a valid representation of a "
                                   + missingMeaning.getClass() + ".");
        }
        if (retval == null)
        {
            throw new XMLException("Invalid Node value.  " + numberString
                                   + " could not be parsed as a "
                                   + missingMeaning.getClass().getName() + ".");
        }
        return retval;
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>double</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double</code>.
     */
    public static double getDoubleAttribute(Element dataElement,
                                            String attributeName)
        throws XMLException
    {
        return getDoubleAttribute(dataElement, attributeName, null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>double</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double</code>.
     */
    public static double getDoubleAttribute(Element dataElement,
                                            String attributeName,
                                            Class loaderClass)
        throws XMLException
    {
        XMLDouble doubleXML = new XMLDouble();
        doubleXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   loaderClass));
        return doubleXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>double</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>double</code>.
     */
    public static double getDoubleAttribute(Element dataElement,
                                            String attributeName,
                                            double missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLDouble doubleXML = new XMLDouble();
        doubleXML.loadFromXML(attr);
        return doubleXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>float</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>float</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>float</code>.
     */
    public static float getFloatAttribute(Element dataElement,
                                          String attributeName)
        throws XMLException
    {
        XMLFloat floatXML = new XMLFloat();
        floatXML.loadFromXML(getRequiredAttribute(dataElement,
                                                  attributeName,
                                                  null));
        return floatXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>float</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>float</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>float</code>.
     */
    public static float getFloatAttribute(Element dataElement,
                                          String attributeName,
                                          Class loaderClass)
        throws XMLException
    {
        XMLFloat floatXML = new XMLFloat();
        floatXML.loadFromXML(getRequiredAttribute(dataElement,
                                                  attributeName,
                                                  loaderClass));
        return floatXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>float</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>float</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>float</code>.
     */
    public static float getFloatAttribute(Element dataElement,
                                          String attributeName,
                                          float missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLFloat floatXML = new XMLFloat();
        floatXML.loadFromXML(attr);
        return floatXML.getContents();
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>int</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>int</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>int</code>.
     */
    public static int getIntAttribute(Element dataElement,
                                      String attributeName) throws XMLException
    {
        XMLInt intXML = new XMLInt();
        intXML.loadFromXML(getRequiredAttribute(dataElement,
                                                attributeName,
                                                null));
        return intXML.getContents();
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>int</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>int</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>int</code>.
     */
    public static int getIntAttribute(Element dataElement,
                                      String attributeName,
                                      Class loaderClass) throws XMLException
    {
        XMLInt intXML = new XMLInt();
        intXML.loadFromXML(getRequiredAttribute(dataElement,
                                                attributeName,
                                                loaderClass));
        return intXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as an <code>int</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>int</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>int</code>.
     */
    public static int getIntAttribute(Element dataElement,
                                      String attributeName,
                                      int missingMeaning) throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLInt intXML = new XMLInt();
        intXML.loadFromXML(attr);
        return intXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>long</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>long</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>long</code>.
     */
    public static long getLongAttribute(Element dataElement,
                                        String attributeName,
                                        Class loaderClass) throws XMLException
    {
        XMLLong longXML = new XMLLong();
        longXML.loadFromXML(getRequiredAttribute(dataElement,
                                                 attributeName,
                                                 loaderClass));
        return longXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>long</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>long</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>long</code>.
     */
    public static long getLongAttribute(Element dataElement,
                                        String attributeName,
                                        long missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLLong longXML = new XMLLong();
        longXML.loadFromXML(attr);
        return longXML.getContents();
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Point2d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Point2d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Point2d</code>.
     */
    public static Point2d getPoint2dAttribute(Element dataElement,
                                              String attributeName,
                                              Class loaderClass)
        throws XMLException
    {
        XMLPoint2d point2dXML = new XMLPoint2d();
        point2dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass));
        return point2dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Point3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Point3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Point3d</code>.
     */
    public static Point3d getPoint3dAttribute(Element dataElement,
                                              String attributeName)
        throws XMLException
    {
        XMLPoint3d point3dXML = new XMLPoint3d();
        point3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    null));
        return point3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Point3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Point3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Point3d</code>.
     */
    public static Point3d getPoint3dAttribute(Element dataElement,
                                              String attributeName,
                                              Class loaderClass)
        throws XMLException
    {
        XMLPoint3d point3dXML = new XMLPoint3d();
        point3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass));
        return point3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>Point3d</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>Point3d</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as a
     *         <code>Point3d</code>.
     */
    public static Point3d getPoint3dAttribute(Element dataElement,
                                              String attributeName,
                                              Point3d missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLPoint3d point3dXML = new XMLPoint3d();
        point3dXML.loadFromXML(attr);
        return point3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Quat4d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Quat4d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Quat4d</code>.
     */
    public static Quat4d getQuat4dAttribute(Element dataElement,
                                            String attributeName)
        throws XMLException
    {
        XMLQuat4d quat4dXML = new XMLQuat4d();
        quat4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   null));
        return quat4dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Quat4d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Quat4d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Quat4d</code>.
     */
    public static Quat4d getQuat4dAttribute(Element dataElement,
                                            String attributeName,
                                            Class loaderClass)
        throws XMLException
    {
        XMLQuat4d quat4dXML = new XMLQuat4d();
        quat4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   loaderClass));
        return quat4dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>
     * into the provided <code>Tuple3d</code>. If the attribute is not
     * present, or its value is not valid as a <code>Tuple3d</code>, an
     * exception is thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple3d</code>
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param retval
     *        the <code>Tuple3d</code> instance into which the fetched data is
     *        to be loaded
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Tuple3d</code>.
     */
    public static <T extends Tuple3d> void getTuple3dAttribute(Element dataElement,
                                                               String attributeName,
                                                               T retval,
                                                               Class loaderClass)
        throws XMLException
    {
        XMLTuple3d<T> tuple3dXML = new XMLTuple3d<T>(retval);
        tuple3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass));
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>
     * into the provided <code>Tuple4d</code>. If the attribute is not
     * present, or its value is not valid as a <code>Tuple4d</code>, an
     * exception is thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple4d</code>
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param retval
     *        the <code>Tuple4d</code> instance into which the fetched data is
     *        to be loaded
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Tuple4d</code>.
     */
    public static <T extends Tuple4d> void getTuple4dAttribute(Element dataElement,
                                                               String attributeName,
                                                               T retval)
        throws XMLException
    {
        XMLTuple4d<T> tuple4dXML = new XMLTuple4d<T>(retval);
        tuple4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    null));
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>
     * into the provided <code>Tuple4d</code>. If the attribute is not
     * present, or its value is not valid as a <code>Tuple4d</code>, an
     * exception is thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple4d</code>
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param retval
     *        the <code>Tuple4d</code> instance into which the fetched data is
     *        to be loaded
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Tuple4d</code>.
     */
    public static <T extends Tuple4d> void getTuple4dAttribute(Element dataElement,
                                                               String attributeName,
                                                               T retval,
                                                               Class loaderClass)
        throws XMLException
    {
        XMLTuple4d<T> tuple4dXML = new XMLTuple4d<T>(retval);
        tuple4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                    attributeName,
                                                    loaderClass));
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Vector3f</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Vector3f</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Vector3f</code>.
     */
    public static Vector3f getVector3fAttribute(Element dataElement,
                                                String attributeName)
        throws XMLException
    {
        return getVector3fAttribute(dataElement, attributeName, null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Vector3f</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Vector3f</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Vector3f</code>.
     */
    public static Vector3f getVector3fAttribute(Element dataElement,
                                                String attributeName,
                                                Class loaderClass)
        throws XMLException
    {
        XMLVector3f vector3fXML = new XMLVector3f();
        vector3fXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     loaderClass));
        return vector3fXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Vector3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Vector3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Vector3d</code>.
     */
    public static Vector3d getVector3dAttribute(Element dataElement,
                                                String attributeName)
        throws XMLException
    {
        return getVector3dAttribute(dataElement, attributeName, null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Vector3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Vector3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Vector3d</code>.
     */
    public static Vector3d getVector3dAttribute(Element dataElement,
                                                String attributeName,
                                                Class loaderClass)
        throws XMLException
    {
        XMLVector3d vector3dXML = new XMLVector3d();
        vector3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     loaderClass));
        return vector3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Matrix4d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Matrix4d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Matrix4d</code>.
     */
    public static Matrix4d getMatrix4dAttribute(Element dataElement,
                                                String attributeName)
        throws XMLException
    {
        XMLMatrix4d matrix4dXML = new XMLMatrix4d();
        matrix4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     null));
        return matrix4dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Matrix4d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Matrix4d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Matrix4d</code>.
     */
    public static Matrix4d getMatrix4dAttribute(Element dataElement,
                                                String attributeName,
                                                Class loaderClass)
        throws XMLException
    {
        XMLMatrix4d matrix4dXML = new XMLMatrix4d();
        matrix4dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     loaderClass));
        return matrix4dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Matrix3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Matrix3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Matrix3d</code>.
     */
    public static Matrix3d getMatrix3dAttribute(Element dataElement,
                                                String attributeName)
        throws XMLException
    {
        XMLMatrix3d matrix3dXML = new XMLMatrix3d();
        matrix3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     null));
        return matrix3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Matrix3d</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Matrix3d</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Matrix3d</code>.
     */
    public static Matrix3d getMatrix3dAttribute(Element dataElement,
                                                String attributeName,
                                                Class loaderClass)
        throws XMLException
    {
        XMLMatrix3d matrix3dXML = new XMLMatrix3d();
        matrix3dXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     loaderClass));
        return matrix3dXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>double[]</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>double[]</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double[]</code>.
     */
    public static double[] getDoubleArrayAttribute(Element dataElement,
                                                   String attributeName)
        throws XMLException
    {
        XMLDoubleArray doubleArrayXML = new XMLDoubleArray();
        doubleArrayXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     null));
        return doubleArrayXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>double[]</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>double[]</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>double[]</code>.
     */
    public static double[] getDoubleArrayAttribute(Element dataElement,
                                                   String attributeName,
                                                   Class loaderClass)
        throws XMLException
    {
        XMLDoubleArray doubleArrayXML = new XMLDoubleArray();
        doubleArrayXML.loadFromXML(getRequiredAttribute(dataElement,
                                                     attributeName,
                                                     loaderClass));
        return doubleArrayXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>double[]</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>double</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>double</code>.
     */
    public static double[] getDoubleArrayAttribute(Element dataElement,
                                                   String attributeName,
                                                   double[] missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLDoubleArray doubleXML = new XMLDoubleArray();
        doubleXML.loadFromXML(attr);
        return doubleXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>float[]</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>float[]</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>float[]</code>.
     */
    public static float[] getFloatArrayAttribute(Element dataElement,
                                                 String attributeName)
        throws XMLException
    {
        XMLFloatArray floatArrayXML = new XMLFloatArray();
        floatArrayXML.loadFromXML(getRequiredAttribute(dataElement,
                                                       attributeName,
                                                       null));
        return floatArrayXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>float[]</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>float[]</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>float[]</code>.
     */
    public static float[] getFloatArrayAttribute(Element dataElement,
                                                 String attributeName,
                                                 Class loaderClass)
        throws XMLException
    {
        XMLFloatArray floatArrayXML = new XMLFloatArray();
        floatArrayXML.loadFromXML(getRequiredAttribute(dataElement,
                                                       attributeName,
                                                       loaderClass));
        return floatArrayXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>float[]</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>float[]</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as an
     *         <code>float[]</code>.
     */
    public static float[] getFloatArrayAttribute(Element dataElement,
                                                 String attributeName,
                                                 float[] missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLFloatArray floatXML = new XMLFloatArray();
        floatXML.loadFromXML(attr);
        return floatXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Color</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Color</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Color</code>.
     */

    public static Color getColourAttribute(Element dataElement,
                                           String attributeName)
        throws XMLException
    {
        return getColourAttribute(dataElement, attributeName, (Class)null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Color</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Color</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Color</code>.
     */

    public static Color getColourAttribute(Element dataElement,
                                           String attributeName,
                                           Class loaderClass)
        throws XMLException
    {
        XMLColour colourXML = new XMLColour();
        colourXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   loaderClass));
        return colourXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>Color</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>Color</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as a
     *         <code>Color</code>.
     */
    public static Color getColourAttribute(Element dataElement,
                                           String attributeName,
                                           Color missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLColour colourXML = new XMLColour();
        colourXML.loadFromXML(attr);
        return colourXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Color</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>Color3f</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Color</code>.
     */

    public static Color3f getColor3fAttribute(Element dataElement,
                                              String attributeName)
        throws XMLException
    {
        return getColor3fAttribute(dataElement, attributeName, (Class)null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, or its value is not valid as a
     * <code>Color</code>, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>Color3f</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>, or if its value is not valid as a
     *         <code>Color</code>.
     */

    public static Color3f getColor3fAttribute(Element dataElement,
                                              String attributeName,
                                              Class loaderClass)
        throws XMLException
    {
        XMLColor3f colourXML = new XMLColor3f();
        colourXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   loaderClass));
        return colourXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned). If the attribute <em>is</em>
     * present, but its value is not valid as a <code>Color</code>, an
     * exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's <code>Color3f</code> value.
     * @throws XMLException
     *         if the specified attribute is present in the provided
     *         <code>Element</code>, but its value is not valid as a
     *         <code>Color</code>.
     */
    public static Color3f getColor3fAttribute(Element dataElement,
                                              String attributeName,
                                              Color3f missingMeaning)
        throws XMLException
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLColor3f colourXML = new XMLColor3f();
        colourXML.loadFromXML(attr);
        return colourXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @return the fetched attribute's <code>String</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>.
     */
    public static String getStringAttribute(Element dataElement,
                                            String attributeName)
        throws XMLException
    {
        return getStringAttribute(dataElement, attributeName, (Class)null);
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, an exception is thrown.
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>String</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>.
     */
    public static String getStringAttribute(Element dataElement,
                                            String attributeName,
                                            Class loaderClass)
        throws XMLException
    {
        XMLString stringXML = new XMLString();
        stringXML.loadFromXML(getRequiredAttribute(dataElement,
                                                   attributeName,
                                                   loaderClass));
        return stringXML.getContents();
    }

    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned).
     * 
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's value.
     */
    public static String getStringAttribute(Element dataElement,
                                            String attributeName,
                                            String missingMeaning)
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLString stringXML = new XMLString();
        stringXML.loadFromXML(attr);
        return stringXML.getContents();
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, an exception is thrown.
     * 
     * @param <E>
     *        the type of <code>enum</code>
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param enumClass
     *        the class of the <code>enum</code> from which the read value
     *        will be looked up
     * @param loaderClass
     *        the class that is attempting to load the attribute from XML (used
     *        when reporting errors)
     * @return the fetched attribute's <code>String</code> value.
     * @throws XMLException
     *         if the specified attribute is not present in the provided
     *         <code>Element</code>.
     */
    public static <E extends Enum<E>> E getEnumAttribute(Element dataElement,
                                                         String attributeName,
                                                         Class<E> enumClass,
                                                         Class loaderClass)
        throws XMLException
    {
        String enumString = getStringAttribute(dataElement,
                                               attributeName,
                                               loaderClass);
        return Enum.valueOf(enumClass, enumString);
    }
    
    /**
     * Fetches the specified attribute from the provided <code>Element</code>.
     * If the attribute is not present, then the <code>missingMeaning</code>
     * is inferred (and, thus, returned).
     * 
     * @param <E>
     *        the type of <code>enum</code>
     * @param dataElement
     *        the element from which the attribute is to be fetched
     * @param attributeName
     *        the name of the attribute to be fetched
     * @param missingMeaning
     *        the value that is returned if the attribute is not present
     * @return the fetched attribute's value.
     */
    public static <E extends Enum<E>> E getEnumAttribute(Element dataElement,
                                                         String attributeName,
                                                         E missingMeaning)
    {
        Attr attr = dataElement.getAttributeNode(attributeName);
        if (attr == null)
        {
            return missingMeaning;
        }
        XMLString stringXML = new XMLString();
        stringXML.loadFromXML(attr);
        Class<E> enumClass = missingMeaning.getDeclaringClass();
        return Enum.valueOf(enumClass, stringXML.getContents());
    }
    
    /**
     * Fetches the specified <code>Element</code> from the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is returned.
     * 
     * @param parentNode the parent node on which to search for the specified
     *                   element
     * @param elementName the name of the element to be retrieved
     * @return the fetched element.
     * @throws XMLException if the specified element is not present as a direct
     *                      child of the provided <code>Node</code>
     */
    public static Element getRequiredElement(Node parentNode, String elementName) throws XMLException
    {
        Element requiredElement = getFirstChildElementByTagName(parentNode, elementName);
        if (requiredElement == null)
        {
            String loaderClass = getLoaderClassName();
            throw new XMLException("Invalid Node structure.\n" + loaderClass + " requires that the " + elementName + " tag be present.");
        }
        return requiredElement;
    }
    
    /**
     * Fetches the specified <code>Element</code> from the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is returned.
     * 
     * @param parentNode the parent node on which to search for the specified
     *                   element
     * @param elementName the name of the element to be retrieved
     * @param loaderClass the class that is attempting to load the element (used
     *                    when reporting errors)
     * @return the fetched element.
     * @throws XMLException if the specified element is not present as a direct
     *                      child of the provided <code>Node</code>
     */
    public static Element getRequiredElement(Node parentNode, String elementName, Class loaderClass) throws XMLException
    {
        Element requiredElement = getFirstChildElementByTagName(parentNode, elementName);
        if (requiredElement == null)
        {
            throw new XMLException("Invalid Node structure.\n" + loaderClass.getName() + " requires that the " + elementName + " tag be present.");
        }
        return requiredElement;
    }
    
    /**
     * Fetches the <code>Point2d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Point2d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Point2d</code>.
     */
    public static Point2d getPoint2dElement(Node parentNode,
                                            String elementName,
                                            Class loaderClass)
        throws XMLException
    {
        XMLPoint2d point2dXML = new XMLPoint2d();
        point2dXML.loadFromXML(getRequiredElement(parentNode,
                                                  elementName,
                                                  loaderClass));
        return point2dXML.getContents();
    }

    /**
     * Fetches the <code>Point3d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Point3d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Point3d</code>.
     */
    public static Point3d getPoint3dElement(Node parentNode,
                                            String elementName,
                                            Class loaderClass)
        throws XMLException
    {
        XMLPoint3d point3dXML = new XMLPoint3d();
        point3dXML.loadFromXML(getRequiredElement(parentNode,
                                                  elementName,
                                                  loaderClass));
        return point3dXML.getContents();
    }

    /**
     * Fetches the <code>Quat4d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Quat4d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Quat4d</code>.
     */
    public static Quat4d getQuat4dElement(Node parentNode,
                                          String elementName,
                                          Class loaderClass)
        throws XMLException
    {
        XMLQuat4d quat4dXML = new XMLQuat4d();
        quat4dXML.loadFromXML(getRequiredElement(parentNode,
                                                 elementName,
                                                 loaderClass));
        return quat4dXML.getContents();
    }

    /**
     * Fetches the <code>Tuple3d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code> into the provided <code>Tuple3d</code>. If no such
     * element is present, an exception is thrown. If more than one element with
     * the specified name is present, the first of them is loaded as a
     * <code>Tuple3d</code>; if it is not of a valid format, an exception is
     * thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple3d</code>
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param retval
     *        the <code>Tuple3d</code> instance into which the fetched data is
     *        to be loaded
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Tuple3d</code>.
     */
    public static <T extends Tuple3d> void getTuple3dElement(Node parentNode,
                                                             String elementName,
                                                             T retval,
                                                             Class loaderClass)
        throws XMLException
    {
        XMLTuple3d<T> tuple3dXML = new XMLTuple3d<T>(retval);
        tuple3dXML.loadFromXML(getRequiredElement(parentNode,
                                                  elementName,
                                                  loaderClass));
    }

    /**
     * Fetches the <code>Tuple4d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code> into the provided <code>Tuple4d</code>. If no such
     * element is present, an exception is thrown. If more than one element with
     * the specified name is present, the first of them is loaded as a
     * <code>Tuple4d</code>; if it is not of a valid format, an exception is
     * thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple4d</code>
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param retval
     *        the <code>Tuple4d</code> instance into which the fetched data is
     *        to be loaded
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Tuple4d</code>.
     */
    public static <T extends Tuple4d> void getTuple4dElement(Node parentNode,
                                                             String elementName,
                                                             T retval)
        throws XMLException
    {
        XMLTuple4d<T> tuple4dXML = new XMLTuple4d<T>(retval);
        tuple4dXML.loadFromXML(getRequiredElement(parentNode,
                                                  elementName,
                                                  null));
    }

    /**
     * Fetches the <code>Tuple4d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code> into the provided <code>Tuple4d</code>. If no such
     * element is present, an exception is thrown. If more than one element with
     * the specified name is present, the first of them is loaded as a
     * <code>Tuple4d</code>; if it is not of a valid format, an exception is
     * thrown.
     * 
     * @param <T>
     *        the actual type of the <code>Tuple4d</code>
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param retval
     *        the <code>Tuple4d</code> instance into which the fetched data is
     *        to be loaded
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Tuple4d</code>.
     */
    public static <T extends Tuple4d> void getTuple4dElement(Node parentNode,
                                                             String elementName,
                                                             T retval,
                                                             Class loaderClass)
        throws XMLException
    {
        XMLTuple4d<T> tuple4dXML = new XMLTuple4d<T>(retval);
        tuple4dXML.loadFromXML(getRequiredElement(parentNode,
                                                  elementName,
                                                  loaderClass));
    }

    /**
     * Fetches the <code>Vector3d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Vector3d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Vector3d</code>.
     */
    public static Vector3d getVector3dElement(Node parentNode,
                                              String elementName)
        throws XMLException
    {
        XMLVector3d vector3dXML = new XMLVector3d();
        vector3dXML.loadFromXML(getRequiredElement(parentNode,
                                                   elementName,
                                                   null));
        return vector3dXML.getContents();
    }

    /**
     * Fetches the <code>Vector3d</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Vector3d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Vector3d</code>.
     */
    public static Vector3d getVector3dElement(Node parentNode,
                                              String elementName,
                                              Class loaderClass)
        throws XMLException
    {
        XMLVector3d vector3dXML = new XMLVector3d();
        vector3dXML.loadFromXML(getRequiredElement(parentNode,
                                                   elementName,
                                                   loaderClass));
        return vector3dXML.getContents();
    }

    /**
     * Exports the provided <code>XMLExportable</code> object as a child element
     * of the specified <code>Node</code>.
     * 
     * @param parentNode the node to which the exportable object should be
     *                   attached as a child element
     * @param elementName the name by which the exportable object will be
     *                    attached to <code>parentNode</code>
     * @param exportable the object to be exported
     * @throws XMLException if <code>exportable</code> fales to be exported as
     *                      an element.
     */
    public static void exportElement(Node parentNode, String elementName, XMLExportable exportable) throws XMLException
    {
        Element exportElement = parentNode.getOwnerDocument().createElement(elementName);
        parentNode.appendChild(exportElement);
        exportable.exportAsXML(exportElement);
    }
    
    /**
     * Exports the provided value as a child element of the specified
     * <code>Node</code>.
     * 
     * @param parentNode
     *        the node to which the exportable object should be attached as a
     *        child element
     * @param elementName
     *        the name by which the exportable object will be attached to
     *        <code>parentNode</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static void exportElement(Node parentNode,
                                     String elementName,
                                     Point2d value) throws XMLException
    {
        XMLPoint2d valueXML = new XMLPoint2d(value);
        exportElement(parentNode, elementName, valueXML);
    }

    /**
     * Exports the provided value as a child element of the specified
     * <code>Node</code>.
     * 
     * @param parentNode
     *        the node to which the exportable object should be attached as a
     *        child element
     * @param elementName
     *        the name by which the exportable object will be attached to
     *        <code>parentNode</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static void exportElement(Node parentNode,
                                     String elementName,
                                     Point3d value) throws XMLException
    {
        XMLPoint3d valueXML = new XMLPoint3d(value);
        exportElement(parentNode, elementName, valueXML);
    }

    /**
     * Exports the provided value as a child element of the specified
     * <code>Node</code>.
     * 
     * @param parentNode
     *        the node to which the exportable object should be attached as a
     *        child element
     * @param elementName
     *        the name by which the exportable object will be attached to
     *        <code>parentNode</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static void exportElement(Node parentNode,
                                     String elementName,
                                     Quat4d value) throws XMLException
    {
        XMLQuat4d valueXML = new XMLQuat4d(value);
        exportElement(parentNode, elementName, valueXML);
    }

    /**
     * Exports the provided value as a child element of the specified
     * <code>Node</code>.
     * 
     * @param parentNode
     *        the node to which the exportable object should be attached as a
     *        child element
     * @param elementName
     *        the name by which the exportable object will be attached to
     *        <code>parentNode</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static void exportElement(Node parentNode,
                                     String elementName,
                                     Vector3d value) throws XMLException
    {
        XMLVector3d valueXML = new XMLVector3d(value);
        exportElement(parentNode, elementName, valueXML);
    }

    /**
     * Exports the provided <code>XMLExportable</code> object as an attribute of
     * the specified <code>Element</code>.
     * 
     * @param exportElement the element to which the exportable object should be
     *                      attached as an attribute
     * @param attributeName the name by which the exportable object will be
     *                      attached to <code>exportElement</code>
     * @param exportable the object to be exported
     * @throws XMLException if <code>exportable</code> fails to be exported as
     *                      an attribute.
     */
    public static void exportAttribute(Element exportElement, String attributeName, XMLExportable exportable) throws XMLException
    {
        Attr attribute = exportElement.getOwnerDocument().createAttribute(attributeName);
        exportElement.setAttributeNode(attribute);
        exportable.exportAsXML(attribute);
    }
    
    /**
     * This helper method is merely a disambiguator for
     * {@link #exportAttribute(Element, String, XMLExportable)}. It is exactly
     * equivalent to calling:
     * 
     * <pre>
     * exportAttribute(exportElement, attributeName, (XMLExportable)exportable)
     * </pre>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param exportable
     *        the object to be exported
     * @throws XMLException
     *         if <code>exportable</code> fails to be exported as an
     *         attribute.
     */
    public static void exportAttributeX(Element exportElement,
                                        String attributeName,
                                        XMLExportable exportable)
        throws XMLException
    {
        exportAttribute(exportElement, attributeName, exportable);
    }
    
    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Class value) throws XMLException
    {
        exportAttribute(exportElement, attributeName, value.getName());
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       boolean value) throws XMLException
    {
        XMLBoolean valueXML = new XMLBoolean(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Number value)
    {
        Attr attribute = exportElement.getOwnerDocument()
            .createAttribute(attributeName);
        exportElement.setAttributeNode(attribute);
        attribute.setNodeValue(value.toString());
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       double value) throws XMLException
    {
        XMLDouble valueXML = new XMLDouble(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       float value) throws XMLException
    {
        XMLFloat valueXML = new XMLFloat(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       int value) throws XMLException
    {
        XMLInt valueXML = new XMLInt(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       long value) throws XMLException
    {
        XMLLong valueXML = new XMLLong(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Point2d value) throws XMLException
    {
        XMLPoint2d valueXML = new XMLPoint2d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Point3d value) throws XMLException
    {
        XMLPoint3d valueXML = new XMLPoint3d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Quat4d value) throws XMLException
    {
        XMLQuat4d valueXML = new XMLQuat4d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       String value) throws XMLException
    {
        XMLString valueXML = new XMLString(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Vector3d value) throws XMLException
    {
        XMLVector3d valueXML = new XMLVector3d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Tuple4d value) throws XMLException
    {
        XMLTuple4d valueXML = new XMLTuple4d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Matrix3d value) throws XMLException
    {
        XMLMatrix3d valueXML = new XMLMatrix3d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Matrix4d value) throws XMLException
    {
        XMLMatrix4d valueXML = new XMLMatrix4d(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }

    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       double[] value) throws XMLException
    {
        XMLDoubleArray valueXML = new XMLDoubleArray(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }
    
    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       float[] value) throws XMLException
    {
        XMLFloatArray valueXML = new XMLFloatArray(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }
    
    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Color value) throws XMLException
    {
        XMLColour valueXML = new XMLColour(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }
    
    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static void exportAttribute(Element exportElement,
                                       String attributeName,
                                       Color3f value) throws XMLException
    {
        XMLColor3f valueXML = new XMLColor3f(value);
        exportAttribute(exportElement, attributeName, valueXML);
    }
    
    /**
     * Exports the provided value as an attribute of the specified
     * <code>Element</code>.
     * 
     * @param <E>
     *        the type of the enumeration to which the value belongs
     * @param exportElement
     *        the element to which the exportable object should be attached as
     *        an attribute
     * @param attributeName
     *        the name by which the exportable object will be attached to
     *        <code>exportElement</code>
     * @param value
     *        the value to be exported
     * @throws XMLException
     *         if the value fails to be exported as an attribute.
     */
    public static <E extends Enum> void exportAttribute(Element exportElement,
                                                        String attributeName,
                                                        E value)
        throws XMLException
    {
        exportAttribute(exportElement, attributeName, value.name());
    }
    
    /**
     * Fetches the <code>Map</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Point2d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param <K>
     *        the key type of the map
     * @param <V>
     *        the value type of the map
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Map</code>.
     * @throws InitializationException
     *         if the map cannot be loaded
     */
    public static <K extends XMLMappable, V extends XMLMappable> Map<K, V> getMapElement(Node parentNode,
                                                                                         String elementName,
                                                                                         Class loaderClass)
        throws XMLException,
            InitializationException
    {
        return loadMap(getRequiredElement(parentNode, elementName, loaderClass));
    }

    /**
     * Fetches the <code>Map</code> that is stored in the provided
     * <code>Node</code>.
     * 
     * @param <K>
     *        the key type of the map
     * @param <V>
     *        the value type of the map
     * @param dataNode
     *        the node from which to load the map
     * @return the loaded map.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Map</code>.
     * @throws InitializationException
     *         if the map cannot be loaded
     */
    public static <K extends XMLMappable, V extends XMLMappable> Map<K, V> loadMap(Node dataNode)
        throws XMLException,
            InitializationException
    {
        XMLMap<K, V> mapXML = new XMLMap<K, V>();
        mapXML.loadFromXML(dataNode);
        return mapXML.getContents();
    }

    /**
     * Exports the provided map as a child element of the specified
     * <code>Node</code>.
     * 
     * @param <K>
     *        the key type of the map
     * @param <V>
     *        the value type of the map
     * @param parentNode
     *        the node to which the map should be attached as a child element
     * @param elementName
     *        the name by which the map will be attached to
     *        <code>parentNode</code>
     * @param map
     *        the map to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static <K extends XMLMappable, V extends XMLMappable> void exportMapElement(Node parentNode,
                                                                                       String elementName,
                                                                                       Map<K, V> map)
        throws XMLException
    {
        exportMap(XMLHelper.createChildElement(parentNode, elementName), map);
    }

    /**
     * Exports the provided map in the specified <code>Node</code>.
     * 
     * @param <K>
     *        the key type of the map
     * @param <V>
     *        the value type of the map
     * @param exportNode
     *        the node to which the map shall be exported
     * @param map
     *        the map to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static <K extends XMLMappable, V extends XMLMappable> void exportMap(Node exportNode,
                                                                                Map<K, V> map)
        throws XMLException
    {
        XMLMap<K, V> mapXML = new XMLMap<K, V>(map);
        mapXML.exportAsXML(exportNode);
    }

    /**
     * Fetches the <code>Map</code> that is stored as a child
     * <code>Element</code> (with the specified name) of the provided
     * <code>Node</code>. If no such element is present, an exception is
     * thrown. If more than one element with the specified name is present, the
     * first of them is loaded as a <code>Point2d</code>; if it is not of a
     * valid format, an exception is thrown.
     * 
     * @param <V>
     *        the value type of the map
     * @param parentNode
     *        the parent node on which to search for the specified element
     * @param elementName
     *        the name of the element to be retrieved
     * @param loaderClass
     *        the class that is attempting to load the element (used when
     *        reporting errors)
     * @return the value fetched from the element.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Map</code>.
     * @throws InitializationException
     *         if the map cannot be loaded
     */
    public static <V extends XMLMappable> Map<String, V> getNameMapElement(Node parentNode,
                                                                           String elementName,
                                                                           Class loaderClass)
        throws XMLException,
            InitializationException
    {
        return loadNameMap(getRequiredElement(parentNode,
                                              elementName,
                                              loaderClass));
    }

    /**
     * Fetches the <code>Map</code> that is stored in the provided
     * <code>Node</code>.
     * 
     * @param <V>
     *        the value type of the map
     * @param dataNode
     *        the node from which to load the map
     * @return the loaded map.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Map</code>.
     * @throws InitializationException
     *         if the map cannot be loaded
     */
    public static <V extends XMLLoadable> Map<String, V> loadNameMap(Node dataNode)
        throws XMLException,
            InitializationException
    {
        XMLNameMapLoader<V> mapXML = new XMLNameMapLoader<V>();
        mapXML.loadFromXML(dataNode);
        return mapXML.getContents();
    }
    
    /**
     * Fetches the <code>Map</code> that is stored in the provided
     * <code>Node</code> (where the contents of the map need
     * <code>config</code> to load themselves).
     * 
     * @param <C>
     *        the type of configuration needed to load the map's contents
     * @param <V>
     *        the value type of the map
     * @param dataNode
     *        the node from which to load the map
     * @param config 
     *        the configuration needed to load the map's contents
     * @return the loaded map.
     * @throws XMLException
     *         if the specified element is not present as a direct child of the
     *         provided <code>Node</code>, or if the first matching child
     *         element cannot be loaded as a <code>Map</code>.
     * @throws InitializationException
     *         if the map cannot be loaded
     */
    public static <C, V extends XMLConfiguredLoadable<C>> Map<String, V> loadNameMap(Node dataNode,
                                                                                     C config)
        throws XMLException,
            InitializationException
    {
        XMLNameMapConfiguredLoader<C, V> mapXML = new XMLNameMapConfiguredLoader<C, V>();
        mapXML.loadFromXML(dataNode, config);
        return mapXML.getContents();
    }

    /**
     * Exports the provided map as a child element of the specified
     * <code>Node</code>.
     * 
     * @param <V>
     *        the value type of the map
     * @param parentNode
     *        the node to which the map should be attached as a child element
     * @param elementName
     *        the name by which the map will be attached to
     *        <code>parentNode</code>
     * @param map
     *        the map to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static <V extends XMLExportable> void exportElement(Node parentNode,
                                                               String elementName,
                                                               Map<String, V> map)
        throws XMLException
    {
        exportNameMap(XMLHelper.createChildElement(parentNode, elementName),
                      map);
    }

    /**
     * Exports the provided map in the specified <code>Node</code>.
     * 
     * @param <V>
     *        the value type of the map
     * @param exportNode
     *        the node to which the map shall be exported
     * @param map
     *        the map to be exported
     * @throws XMLException
     *         if the value fails to be exported as an element.
     */
    public static <V extends XMLExportable> void exportNameMap(Node exportNode,
                                                             Map<String, V> map)
        throws XMLException
    {
        XMLNameMapExporter<V> mapXML = new XMLNameMapExporter<V>(map);
        mapXML.exportAsXML(exportNode);
    }
    
    /**
     * @return The name of the class that called the XMLHelper.
     */
    private static String getLoaderClassName()
    {
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        
        // Get the first stack trace that isn't from XMLHelper or Thread.
        for(StackTraceElement ste : stes)
        {
            String className = ste.getClassName();
            if(!className.equals(XMLHelper.class.getName()) &&
               !className.equals(Thread.class.getName()))
            {
                return className;
            }
        }
        return "(couldn't find caller name)";
    }
    
    /**
     * The attribute used to identify a tag as self-describing (whose contents
     * gives the class name)
     */
    public static final String SELF_DESC_CLASS_ATTRIBUTE_NAME = "selfDescClass";
    
    static final String MAP_ENTRY_TAG_NAME = "Entry";
    static final String MAP_KEY_ATTRIBUTE_NAME = "key";
    static final String MAP_VALUE_TAG_NAME = "Value";
}
