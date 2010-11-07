package org.nate.html;

import java.util.List;

public interface Html {

	List<Html> selectNodes(String selector);

	void setTextContent(String value);

	Html getParentNode();

	void removeChild(Html child);

	Html cloneFragment();

	void appendChild(Html newNode);

	String toHtml();

	boolean hasAttribute(String name);

	void setAttribute(String name, Object value);

	void replaceChildren(Html template);

}