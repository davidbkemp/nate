package org.nate.internal.transformer;

import java.util.Map;

import org.nate.internal.NateDocumentBackedEngine;

public class NateTransformers {

	@SuppressWarnings("unchecked")
	public static NateTransformer from(Object data) {
		if (data instanceof Iterable) {
			return TransformationSequence.fromObjectSequence((Iterable) data);
		}
		if (data instanceof Map) {
			return TransformationMap.fromObjectMap((Map) data);
		}
		if (data instanceof NateDocumentBackedEngine) {
			return new EngineInjector((NateDocumentBackedEngine) data);
		}
		if (data == null) {
			return new NullDataInjector();
		}
		return new TextInjector(data.toString());
	}

}
