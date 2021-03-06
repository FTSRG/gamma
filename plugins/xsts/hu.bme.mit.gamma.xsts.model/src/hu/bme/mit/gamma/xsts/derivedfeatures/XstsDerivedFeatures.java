/********************************************************************************
 * Copyright (c) 2018-2020 Contributors to the Gamma project
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-1.0
 ********************************************************************************/
package hu.bme.mit.gamma.xsts.derivedfeatures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import hu.bme.mit.gamma.expression.derivedfeatures.ExpressionModelDerivedFeatures;
import hu.bme.mit.gamma.expression.model.Declaration;
import hu.bme.mit.gamma.expression.model.DirectReferenceExpression;
import hu.bme.mit.gamma.expression.model.EqualityExpression;
import hu.bme.mit.gamma.expression.model.Expression;
import hu.bme.mit.gamma.expression.model.ReferenceExpression;
import hu.bme.mit.gamma.expression.model.VariableDeclaration;
import hu.bme.mit.gamma.xsts.model.Action;
import hu.bme.mit.gamma.xsts.model.AssignmentAction;
import hu.bme.mit.gamma.xsts.model.AssumeAction;
import hu.bme.mit.gamma.xsts.model.AtomicAction;
import hu.bme.mit.gamma.xsts.model.CompositeAction;
import hu.bme.mit.gamma.xsts.model.EmptyAction;
import hu.bme.mit.gamma.xsts.model.NonDeterministicAction;
import hu.bme.mit.gamma.xsts.model.ParallelAction;
import hu.bme.mit.gamma.xsts.model.PrimedVariable;
import hu.bme.mit.gamma.xsts.model.SequentialAction;
import hu.bme.mit.gamma.xsts.model.VariableDeclarationAction;
import hu.bme.mit.gamma.xsts.model.XSTS;
import hu.bme.mit.gamma.xsts.model.XSTSModelFactory;

public class XstsDerivedFeatures extends ExpressionModelDerivedFeatures {

	protected static XSTSModelFactory xStsFactory = XSTSModelFactory.eINSTANCE;
	
	public static XSTS getContainingXSTS(EObject object) {
		return ecoreUtil.getSelfOrContainerOfType(object, XSTS.class);
	}
	
	public static boolean isLocal(Declaration variable) {
		EObject container = variable.eContainer();
		return container instanceof VariableDeclarationAction;
	}
	
	public static SequentialAction getInitializingAction(XSTS xSts) {
		SequentialAction sequentialAction = xStsFactory.createSequentialAction();
		final Action variableInitializingAction = xSts.getVariableInitializingAction();
		if (!(variableInitializingAction instanceof EmptyAction)) {
			sequentialAction.getActions().add(ecoreUtil.clone(variableInitializingAction));
		}
		final Action configurationInitializingAction = xSts.getConfigurationInitializingAction();
		if (!(configurationInitializingAction instanceof EmptyAction)) {
			sequentialAction.getActions().add(ecoreUtil.clone(configurationInitializingAction));
		}
		final Action entryEventAction = xSts.getEntryEventAction();
		if (!(entryEventAction instanceof EmptyAction)) {
			sequentialAction.getActions().add(ecoreUtil.clone(entryEventAction));
		}
		return sequentialAction;
	}
	
	public static SequentialAction getEnvironmentalAction(XSTS xSts) {
		SequentialAction sequentialAction = xStsFactory.createSequentialAction();
		sequentialAction.getActions().add(ecoreUtil.clone(xSts.getInEventAction()));
		sequentialAction.getActions().add(ecoreUtil.clone(xSts.getOutEventAction()));
		return sequentialAction;
	}
	
	public static Declaration getOriginalVariable(Declaration variable) {
		if (variable instanceof PrimedVariable) {
			VariableDeclaration primedVariable = ((PrimedVariable) variable).getPrimedVariable();
			return getOriginalVariable(primedVariable);
		} else {
			return variable;
		}
	}

	public static boolean isFinalPrimedVariable(VariableDeclaration variable) {
		XSTS xSts = (XSTS) variable.eContainer();
		return xSts.getVariableDeclarations().stream()
				.noneMatch(it -> it instanceof PrimedVariable &&
						((PrimedVariable) it).getPrimedVariable() == variable);
	}

	public static int getPrimeCount(VariableDeclaration variable) {
		if (!(variable instanceof PrimedVariable)) {
			return 0;
		}
		PrimedVariable primedVariable = (PrimedVariable) variable;
		return getPrimeCount(primedVariable.getPrimedVariable()) + 1;
	}

	public static boolean isTrivialAssignment(SequentialAction action) {
		List<Action> xStsSubactions = action.getActions();
		if (xStsSubactions.stream().filter(it -> it instanceof AssumeAction).count() == 1
				&& xStsSubactions.stream().filter(it -> it instanceof AssignmentAction).count() == 1) {
			return isTrivialAssignment(
					(AssumeAction) xStsSubactions.stream()
						.filter(it -> it instanceof AssumeAction).findFirst().get(),
					(AssignmentAction) xStsSubactions.stream()
						.filter(it -> it instanceof AssignmentAction).findFirst()
							.get());
		}
		return false;
	}
	
	public static boolean isTrivialAssignment(AssumeAction assumeAction, AssignmentAction action) {
		Expression xStsAssumption = assumeAction.getAssumption();
		if (xStsAssumption instanceof EqualityExpression) {
			return isTrivialAssignment((EqualityExpression) xStsAssumption, action);
		}
		return false;
	}
	
	public static AtomicAction getFirstAtomicAction(Action action) {
		if (action instanceof AtomicAction) {
			return (AtomicAction) action;
		}
		CompositeAction compositeAction = (CompositeAction) action;
		List<Action> actions = compositeAction.getActions();
		if (actions.isEmpty()) {
			return null;
		}
		Action firstAction = actions.get(0);
		return getFirstAtomicAction(firstAction);
	}

	private static boolean isTrivialAssignment(EqualityExpression expression, AssignmentAction action) {
		Expression xStsLeftOperand = expression.getLeftOperand();
		Expression xStsRightOperand = expression.getRightOperand();
		Declaration xStsDeclaration = ((DirectReferenceExpression)action.getLhs()).getDeclaration();
		Expression xStsAssignmentRhs = action.getRhs();
		// region_name == state_name
		if (xStsLeftOperand instanceof ReferenceExpression) {
			if (((DirectReferenceExpression) xStsLeftOperand).getDeclaration() == xStsDeclaration
					&& ecoreUtil.helperEquals(xStsRightOperand, xStsAssignmentRhs)) {
				return true;
			}
		}
		// state_name == region_name
		if (xStsRightOperand instanceof ReferenceExpression) {
			if (((DirectReferenceExpression) xStsRightOperand).getDeclaration() == xStsDeclaration
					&& ecoreUtil.helperEquals(xStsLeftOperand, xStsAssignmentRhs)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDefinitelyTrueAssumeAction(AssumeAction action) {
		Expression expression = action.getAssumption();
		return expressionUtil.isDefinitelyTrueExpression(expression);
	}

	public static boolean isDefinitelyFalseAssumeAction(AssumeAction action) {
		Expression expression = action.getAssumption();
		return expressionUtil.isDefinitelyFalseExpression(expression);
	}

	// Read-write

	private static Set<VariableDeclaration> _getReadVariables(final AssumeAction action) {
		return expressionUtil.getReferredVariables(action.getAssumption());
	}

	private static Set<VariableDeclaration> _getReadVariables(final AssignmentAction action) {
		return expressionUtil.getReferredVariables(action.getRhs());
	}
	
	private static Set<VariableDeclaration> _getReadVariables(final VariableDeclarationAction action) {
		VariableDeclaration variable = action.getVariableDeclaration();
		Expression initialValue = variable.getExpression();
		if (initialValue != null) {
			return expressionUtil.getReferredVariables(initialValue);
		}
		return Collections.emptySet();
	}

	private static Set<VariableDeclaration> _getReadVariables(final EmptyAction action) {
		return Collections.emptySet();
	}

	private static Set<VariableDeclaration> _getReadVariables(final NonDeterministicAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _readVariables = getReadVariables(containedAction);
			variableList.addAll(_readVariables);
		}
		return variableList;
	}

	private static Set<VariableDeclaration> _getReadVariables(final ParallelAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _readVariables = getReadVariables(containedAction);
			variableList.addAll(_readVariables);
		}
		return variableList;
	}

	private static Set<VariableDeclaration> _getReadVariables(final SequentialAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _readVariables = getReadVariables(containedAction);
			variableList.addAll(_readVariables);
		}
		return variableList;
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final AssumeAction action) {
		return Collections.emptySet();
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final AssignmentAction action) {
		return expressionUtil.getReferredVariables(action.getLhs());
	}
	
	private static Set<VariableDeclaration> _getWrittenVariables(final VariableDeclarationAction action) {
//		VariableDeclaration variable = action.getVariableDeclaration(); // Or this should be an empty set?
		return Collections.emptySet(); // Empty, as this is a declaration, not a "writing"
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final EmptyAction action) {
		return Collections.emptySet();
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final NonDeterministicAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _writtenVariables = getWrittenVariables(containedAction);
			variableList.addAll(_writtenVariables);
		}
		return variableList;
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final ParallelAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _writtenVariables = getWrittenVariables(containedAction);
			variableList.addAll(_writtenVariables);
		}
		return variableList;
	}

	private static Set<VariableDeclaration> _getWrittenVariables(final SequentialAction action) {
		final Set<VariableDeclaration> variableList = new HashSet<VariableDeclaration>();
		List<Action> _actions = action.getActions();
		for (final Action containedAction : _actions) {
			Collection<VariableDeclaration> _writtenVariables = getWrittenVariables(containedAction);
			variableList.addAll(_writtenVariables);
		}
		return variableList;
	}

	public static Set<VariableDeclaration> getReadVariables(final Action action) {
		if (action instanceof AssignmentAction) {
			return _getReadVariables((AssignmentAction) action);
		} else if (action instanceof VariableDeclarationAction) {
			return _getReadVariables((VariableDeclarationAction) action);
		} else if (action instanceof AssumeAction) {
			return _getReadVariables((AssumeAction) action);
		} else if (action instanceof EmptyAction) {
			return _getReadVariables((EmptyAction) action);
		} else if (action instanceof NonDeterministicAction) {
			return _getReadVariables((NonDeterministicAction) action);
		} else if (action instanceof ParallelAction) {
			return _getReadVariables((ParallelAction) action);
		} else if (action instanceof SequentialAction) {
			return _getReadVariables((SequentialAction) action);
		} else {
			throw new IllegalArgumentException("Unhandled action type: " + action);
		}
	}

	public static Set<VariableDeclaration> getWrittenVariables(final Action action) {
		if (action instanceof AssignmentAction) {
			return _getWrittenVariables((AssignmentAction) action);
		} else if (action instanceof VariableDeclarationAction) {
			return _getWrittenVariables((VariableDeclarationAction) action);
		} else if (action instanceof AssumeAction) {
			return _getWrittenVariables((AssumeAction) action);
		} else if (action instanceof EmptyAction) {
			return _getWrittenVariables((EmptyAction) action);
		} else if (action instanceof NonDeterministicAction) {
			return _getWrittenVariables((NonDeterministicAction) action);
		} else if (action instanceof ParallelAction) {
			return _getWrittenVariables((ParallelAction) action);
		} else if (action instanceof SequentialAction) {
			return _getWrittenVariables((SequentialAction) action);
		} else {
			throw new IllegalArgumentException("Unhandled action type: " + action);
		}
	}
	
	public static Set<VariableDeclaration> getReferredVariables(final Action action) {
		Set<VariableDeclaration> referredVariables = getReadVariables(action);
		referredVariables.addAll(getWrittenVariables(action));
		return referredVariables;
	}

}
