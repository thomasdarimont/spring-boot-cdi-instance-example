package demo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import sun.reflect.annotation.AnnotationParser;

public class QualifierUtils {

	public static <Q extends Annotation> Q qualifier(Class<Q> qualifierCandidate) {

		Assert.isTrue(qualifierCandidate.isAnnotationPresent(Qualifier.class),
				"Given annotation type is not a Qualifier! " + qualifierCandidate);

		return createAnnotationInstance(Collections.emptyMap(), qualifierCandidate);
	}

	private static <A extends Annotation> A createAnnotationInstance(Map<String, Object> customValues,
			Class<A> annotationType) {

		Map<String, Object> values = new HashMap<>();

		// Extract default values from annotation
		for (Method method : annotationType.getDeclaredMethods()) {
			values.put(method.getName(), method.getDefaultValue());
		}

		// Populate required values
		values.putAll(customValues);

		return (A) AnnotationParser.annotationForMap(annotationType, values);
	}
}
