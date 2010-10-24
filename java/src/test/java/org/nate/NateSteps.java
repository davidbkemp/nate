package org.nate;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.xml.sax.SAXException;

public class NateSteps {


	private Engine nate;
	private Encoder encoder;
	private String transformedHtml;

	static {
		// Needed to allow us to evaluate Ruby code without interfering with Cucumber!
		System.setProperty("org.jruby.embed.localcontext.scope", "singlethread");
	}

	@Given("^the HTML fragment \"([^\"]*)\"$")
	public void setHtml(String html) {
		encoder = Engine.encoders().encoderFor("HTML");
		nate = Engine.newWith(html, encoder);
	}

	@Given("^the file \"([^\"]*)\"$")
	public void theFile(String filename) {
		nate = Engine.newWith(new File(filename));
	}

	@When("^([^\"]*) is injected$")
	public void inject(String data) throws ScriptException {
		transformedHtml = nate.inject(parseRubyExpression(data));
	}

	@Then("^the HTML fragment is (.*)$")
	public void test(String expectedHtml) throws Exception {
		assertXmlFragmentsEqual(expectedHtml.trim(), transformedHtml.trim());
	}

	private void assertXmlFragmentsEqual(String expected, String actual) throws SAXException, IOException {
		// Wrap in fake roots in case the xml has multiple roots, otherwise you get a parser exception
		assertXMLEqual(wrapInFakeRoot(expected), wrapInFakeRoot(actual));
	}

	private String wrapInFakeRoot(String fragment) {
		return "<fake>" + fragment + "</fake>";
	}

	// This method is needed because the features express the data used to fill in the templates using ruby syntax like:
	// { 'h2' => 'Monkey' } 
	private Object parseRubyExpression(String rubyString) throws ScriptException {
		ScriptEngine rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
		defineRubyConstants(rubyEngine);
		Object result = rubyEngine.eval(rubyString, new SimpleScriptContext());
		return convertToOrdinaryJavaClasses(result);
	}

	private void defineRubyConstants(ScriptEngine rubyEngine) throws ScriptException {
		// This is so that the features can use Nate::Engine::CONTENT_ATTRIBUTE
		rubyEngine.eval("module Nate\n class Engine\n CONTENT_ATTRIBUTE = '*content*'\n end\n end\n");
	}

	// Would actually not need to do this, except that RubyHash and RubyArray seem to have been loaded
	// in a different class loader (at least I think that is the case!)!!!!
	private Object convertToOrdinaryJavaClasses(Object rubyObject) {
		if (rubyObject == null || isAnAcceptableJavaType(rubyObject)) {
			return rubyObject;
		}
		if (rubyObject instanceof RubyHash) {
			return convertToJavaMap((RubyHash) rubyObject);
		}
		if (rubyObject instanceof RubyArray) {
			return convertToJavaList((RubyArray) rubyObject);
		}
		throw new IllegalStateException("Cannot handle " + rubyObject.getClass());
	}

	@SuppressWarnings("unchecked")
	private List convertToJavaList(RubyArray rubyArray) {
		List result = new ArrayList();
		for (Object object : rubyArray) {
			result.add(convertToOrdinaryJavaClasses(object));
		}
		return result;
	}

	private boolean isAnAcceptableJavaType(Object rubyObject) {
		return rubyObject instanceof String || rubyObject instanceof Number;
	}

	private Map<String, Object> convertToJavaMap(RubyHash hash) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Object key : hash.keySet()) {
			if (!(key instanceof String)) {
				throw new IllegalStateException("Keys must be strings, but got:" + key.getClass());
			}
			result.put((String) key, convertToOrdinaryJavaClasses(hash.get(key)));
		}
		return result;
	}

}