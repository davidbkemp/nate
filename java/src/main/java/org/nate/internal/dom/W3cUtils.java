package org.nate.internal.dom;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nate.encoder.NateNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class W3cUtils {

	private W3cUtils() {
	}

	public static List<Node> asNodeList(NodeList nodes) {
		int length = nodes.getLength();
		List<Node> result = new ArrayList<Node>(length);
		for(int i = 0; i < length; i++) {
			result.add(nodes.item(i));
		}
		return result;
	}


	public static void convertNodeToString(Node node, Result result) {
		try {
			Source source = new DOMSource((Node) node);
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty("method", "html");
			xformer.setOutputProperty("omit-xml-declaration", "yes");
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public static String asString(Node node) {
		Writer stringWriter = new StringWriter();
		W3cUtils.convertNodeToString(node, new StreamResult(stringWriter));
		return stringWriter.toString();
	}

	// TODO: this does not belong here.
	static List<NateNode> convertToNateElements(Collection<Element> elements) {
		List<NateNode> result = new ArrayList<NateNode>(elements.size());
		for (Element element : elements) {
			result.add(new NateDomElement(element));
		}
		return result;
	}


}
