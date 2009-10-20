// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.db;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Utility class for dealing with Xml.
 *
 */
public class XmlUtils {

  // This class should not be initialized.
  private XmlUtils() {}

  /**
   * Converts a DB row into its xml representation. E.g., A following DB row of
   * test database:
   * <pre>
   *    [{id=1, lastName=last_01}
   * </pre>
   * is converted to the following xml :
   * <pre>
   *    &lt;html&gt;
   *    &lt;body&gt;
   *    &lt;title&gt;Database Connector Result id=1 lastName=last_01 
   *    &lt;/title&gt;
   *    &lt;table border="1"&gt;
   *    &lt;tr bgcolor="#9acd32"&gt;
   *    &lt;th&gt;id&lt;/th&gt;&lt;th&gt;lastName&lt;/th&gt;&lt;th&gt;email
   *    &lt;/th&gt;&lt;th&gt;firstName&lt;/th&gt;
   *    &lt;/tr&gt;
   *    &lt;tr&gt;
   *    &lt;td&gt;1&lt;/td&gt;&lt;td&gt;last_01&lt;/td&gt;
   *    &lt;td&gt;01@google.com&lt;/td&gt;&lt;td&gt;first_01&lt;/td&gt;
   *    &lt;/tr&gt;
   *    &lt;/table&gt;
   *    &lt;/body&gt;
   *    &lt;/html&gt;
   * </pre>
   *
   * @param dbName name of the database.
   * @param row row to convert to its xml representation.
   * @return xml string.
   * @throws DBException
   */
  public static String getXMLRow(String dbName, Map<String, Object> row,
      String[] primaryKeys, String xslt) throws DBException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    org.w3c.dom.Document doc;
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new DBException("Unable to get XML for row.");
    }

    Element top = doc.createElement(dbName);
    doc.appendChild(top);
    Element title = doc.createElement("title");
    title.appendChild(doc.createTextNode(Util.getTitle(primaryKeys, row)));
    top.appendChild(title);
    TreeSet<String> sortedKeySet = new TreeSet<String>(row.keySet());
    Iterator<String> it = sortedKeySet.iterator();
    while (it.hasNext()) {
      String key = it.next();
      Element keyElement = doc.createElement(key);
      Object value = row.get(key);
      if (null == value) {
        value = "";
      }
      keyElement.appendChild(doc.createTextNode(value.toString()));
      top.appendChild(keyElement);
    }
    String xmlString;
    try {
      if (null == xslt) {
        xmlString = getStringFromDomDocument(doc, null);
      } else if (xslt.isEmpty()) {
        xmlString = getStringFromDomDocument(doc,
            getDomDocFromXslt(getDefaultStyleSheet(dbName, row)));
      } else {
        xmlString = getStringFromDomDocument(doc, getDomDocFromXslt(xslt));
      }
    } catch (TransformerException e) {
      throw new DBException("Unable to create XML string from the DOM document");
    }
    return xmlString;
  }

  private static String getDefaultStyleSheet(String dbName, Map<String, Object> row) {
    StringBuffer buf = new StringBuffer();
    Set<String> columnNames = row.keySet();
    buf.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<xsl:stylesheet version=\"1.0\" "
        + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
        + "<xsl:template match=\"/\"><html><body><xsl:for-each select=\"");
    buf.append(dbName).append("\">");
    buf.append("<title><xsl:value-of select=\"title\"/></title>"
        + "</xsl:for-each><table border=\"1\"><tr bgcolor=\"#9acd32\">");
    for (String column : columnNames){
      buf.append("<th>").append(column).append("</th>");
    }
    buf.append("</tr><xsl:for-each select=\"");
    buf.append(dbName).append("\"><tr>");
    for (String column : columnNames){
      buf.append("<td><xsl:value-of select=\"").append(column).append("\"/></td>");
    }
    buf.append("</tr></xsl:for-each></table></body></html>"
        + "</xsl:template></xsl:stylesheet>");
    return buf.toString();
  }

  /**
   * Converts a DOM document to an xml String.
   *
   * @param doc DOM document
   * @return xml String.
   * @throws TransformerException
   */
  public static String getStringFromDomDocument(org.w3c.dom.Document doc,
      org.w3c.dom.Document xslt) throws TransformerException {
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer;
    if (null != xslt) {
      DOMSource dxsltsource = new DOMSource(xslt);
      transformer = tf.newTransformer(dxsltsource);
    } else {
      transformer = tf.newTransformer();
    }
    transformer.transform(domSource, result);
    return writer.toString();
  }

  /**
   * Converts a String(xslt) into a DOM document.
   *
   * @param xslt string xslt.
   * @return DOM document.
   * @throws DBException
   */
  public static org.w3c.dom.Document getDomDocFromXslt(String xslt) throws
      DBException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      org.w3c.dom.Document document = builder.parse(
          new InputSource(new StringReader(xslt)));
      return document;
    } catch (ParserConfigurationException e1) {
      throw new DBException("Cannot get xslt DOM document", e1);
    } catch (SAXException e2) {
      throw new DBException("Cannot get xslt DOM document", e2);
    } catch (IOException e3) {
      throw new DBException("Cannot get xslt DOM document", e3);
    }
  }
}
