package org.nate;

import java.io.InputStream;

import org.nate.html.Html;

public interface Encoder {
	boolean isNullEncoder();
	String type();
	Html encode(String source);
	Html encode(InputStream source);
}