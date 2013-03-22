package com.bkahlert.nebula.information;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.bkahlert.nebula.SourceProvider;

public class InformationControlManagerUtils {

	public static IEvaluationContext getEvaluationContext() {
		IEvaluationService service = (IEvaluationService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getService(IEvaluationService.class);
		IEvaluationContext evaluationContext = service.getCurrentState();
		return evaluationContext;
	}

	@SuppressWarnings("unchecked")
	public static InformationControlManager<?, InformationControl<?>> getCurrentManager() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object control = evaluationContext.getVariable(SourceProvider.CONTROL);
		if (control instanceof InformationControlManager) {
			return (InformationControlManager<?, InformationControl<?>>) control;
		} else {
			return null;
		}
	}

	public static InformationControl<?> getCurrentControl() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object control = evaluationContext.getVariable(SourceProvider.CONTROL);
		if (control instanceof InformationControl) {
			return (InformationControl<?>) control;
		} else {
			return null;
		}
	}

	public static Object getCurrentInput() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object input = evaluationContext.getVariable(SourceProvider.INPUT);
		if (input != SourceProvider.NULL_INPUT) {
			return input;
		} else {
			return null;
		}
	}

}
