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
package hu.bme.mit.gamma.xsts.transformation

import hu.bme.mit.gamma.statechart.model.composite.BroadcastChannel
import hu.bme.mit.gamma.statechart.model.composite.CompositeComponent
import hu.bme.mit.gamma.statechart.model.composite.SimpleChannel
import hu.bme.mit.gamma.util.GammaEcoreUtil
import hu.bme.mit.gamma.xsts.model.model.XSTS

import static com.google.common.base.Preconditions.checkState

import static extension hu.bme.mit.gamma.statechart.model.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.xsts.transformation.util.Namings.*

class EventConnector {
	// Auxiliary objects
	protected extension GammaEcoreUtil expressionUtil = new GammaEcoreUtil
	
	def void connectEventsThroughChannels(XSTS xSts, CompositeComponent component) {
		for (channel : component.channels) {
			val providedPort = channel.providedPort.port
			val requiredPorts = newArrayList
			if (channel instanceof SimpleChannel) {
				requiredPorts += channel.requiredPort.port
			}
			else if (channel instanceof BroadcastChannel) {
				requiredPorts += channel.requiredPorts.map[it.port]
			}
			// Connection
			val providedSimplePorts = providedPort.allConnectedSimplePorts
			checkState(providedSimplePorts.size == 1)
			val providedSimplePort = providedSimplePorts.head
			val providedStatechart = providedSimplePort.containingStatechart
			val providedInstance = providedStatechart.referencingComponentInstance
			for (requiredPort : requiredPorts) {
				for (requiredSimplePort : requiredPort.allConnectedSimplePorts) {
					val requiredStatechart = requiredSimplePort.containingStatechart
					val requiredInstance = requiredStatechart.referencingComponentInstance
					// In events on required port
					for (event : requiredSimplePort.inputEvents) {
						val requiredInEventName = event.customizeInputName(requiredSimplePort, requiredInstance)
						val xStsInEventVariable = xSts.variableDeclarations.findFirst[it.name == requiredInEventName]
						val providedOutEventName = event.customizeOutputName(providedSimplePort, providedInstance)
						val xStsOutEventVariable = xSts.variableDeclarations.findFirst[it.name == providedOutEventName]
						if (xStsInEventVariable !== null && xStsOutEventVariable !== null) // Can be null due to optimization
						xStsInEventVariable.changeAndDelete(xStsOutEventVariable, xSts)
						// In-parameters
						for (parameter : event.parameterDeclarations) {
							val requiredInParamaterName = parameter.customizeInName(requiredSimplePort, requiredInstance)
							val xStsInParameterVariable = xSts.variableDeclarations.findFirst[it.name == requiredInParamaterName]
							val providedOutParamaterName = parameter.customizeOutName(providedSimplePort, providedInstance)
							val xStsOutParameterVariable = xSts.variableDeclarations.findFirst[it.name == providedOutParamaterName]
							if (xStsInEventVariable !== null && xStsOutEventVariable !== null) // Can be null due to optimization
							xStsInParameterVariable.changeAndDelete(xStsOutParameterVariable, xSts)
						}
					}
					// Out events on required port
					for (event : requiredSimplePort.outputEvents) {
						val requiredOutEventName = event.customizeOutputName(requiredSimplePort, requiredInstance)
						val xStsOutEventVariable = xSts.variableDeclarations.findFirst[it.name == requiredOutEventName]
						val providedInEventName = event.customizeInputName(providedSimplePort, providedInstance)
						val xStsInEventVariable = xSts.variableDeclarations.findFirst[it.name == providedInEventName]
						if (xStsInEventVariable !== null && xStsOutEventVariable !== null) // Can be null due to optimization
						xStsOutEventVariable.changeAndDelete(xStsInEventVariable, xSts)
						// Out-parameters
						for (parameter : event.parameterDeclarations) {
							val requiredOutParamaterName = parameter.customizeOutName(requiredSimplePort, requiredInstance)
							val xStsOutParameterVariable = xSts.variableDeclarations.findFirst[it.name == requiredOutParamaterName]
							val providedInParamaterName = parameter.customizeInName(providedSimplePort, providedInstance)
							val xStsInParameterVariable = xSts.variableDeclarations.findFirst[it.name == providedInParamaterName]
							if (xStsInEventVariable !== null && xStsOutEventVariable !== null) // Can be null due to optimization
							xStsOutParameterVariable.changeAndDelete(xStsInParameterVariable, xSts)
						}
					}
				}
			}
		}
	}
	
}