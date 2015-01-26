package org.semanticweb.elk.reasoner.incremental;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClass;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.SaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.SaturationStatistics;
import org.semanticweb.elk.reasoner.saturation.SaturationUtils;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.RuleVisitor;
import org.semanticweb.elk.reasoner.saturation.rules.contextinit.LinkedContextInitRule;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.IndexedClassDecomposition;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.LinkedSubsumerRule;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.SubsumerDecompositionRule;
import org.semanticweb.elk.util.concurrent.computation.BaseInputProcessor;
import org.semanticweb.elk.util.concurrent.computation.InputProcessor;
import org.semanticweb.elk.util.concurrent.computation.InputProcessorFactory;
import org.semanticweb.elk.util.logging.CachedTimeThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
class ContextInitializationFactory
		implements
		InputProcessorFactory<ArrayList<Context>, InputProcessor<ArrayList<Context>>> {

	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(ContextInitializationFactory.class);

	private final SaturationState<?> saturationState_;
	private final IndexedClassExpression[] changedComposedSubsumers_;
	private final IndexedClass[] changedDecomposedSubsumers_;
	private final LinkedContextInitRule changedGlobalRuleHead_;
	private final Map<? extends IndexedClassExpression, ? extends LinkedSubsumerRule> changedCompositionRules_;
	private AtomicInteger compositionRuleHits_ = new AtomicInteger(0);
	private AtomicInteger decompositionRuleHits_ = new AtomicInteger(0);
	private final SaturationStatistics stageStatistics_;

	private final SubsumerDecompositionRule<IndexedClass> classDecomposition_;

	public ContextInitializationFactory(
			SaturationState<?> state,
			LinkedContextInitRule changedGlobalRuleHead,
			Map<? extends IndexedClassExpression, ? extends LinkedSubsumerRule> changedCompositionRules,
			final Map<? extends IndexedClass, ? extends IndexedClassExpression> changedDefinitions,
			SaturationStatistics stageStats) {

		saturationState_ = state;
		changedCompositionRules_ = changedCompositionRules;
		changedComposedSubsumers_ = new IndexedClassExpression[changedCompositionRules
				.keySet().size()];
		changedCompositionRules.keySet().toArray(changedComposedSubsumers_);
		changedDecomposedSubsumers_ = new IndexedClass[changedDefinitions
				.keySet().size()];
		changedDefinitions.keySet().toArray(changedDecomposedSubsumers_);
		changedGlobalRuleHead_ = changedGlobalRuleHead;
		stageStatistics_ = stageStats;

		classDecomposition_ = new IndexedClassDecomposition() {
			private final Map<? extends IndexedClass, ? extends IndexedClassExpression> changedDefinitions_ = changedDefinitions;

			@Override
			protected IndexedClassExpression getDefinedClassExpression(
					IndexedClass premise) {
				// get the premise from the changed definitions
				return changedDefinitions_.get(premise);
			}
		};
	}

	@Override
	public InputProcessor<ArrayList<Context>> getEngine() {
		return getEngine(getBaseContextProcessor());
	}

	private ContextProcessor getBaseContextProcessor() {

		final SaturationStatistics localStatistics = new SaturationStatistics();

		final RuleVisitor ruleAppVisitor = SaturationUtils
				.getStatsAwareRuleVisitor(localStatistics.getRuleStatistics());
		final SaturationStateWriter<?> saturationStateWriter = SaturationUtils
				.getStatAwareWriter(
						saturationState_.getContextModifyingWriter(),
						localStatistics);

		localStatistics.getConclusionStatistics().startMeasurements();

		return new ContextProcessor() {

			private int localCompsitionRuleHits_ = 0;
			private int localDecompositionRuleHits_ = 0;

			@Override
			public void process(Context context) {
				// apply all changed context initialization rules
				// TODO: do the initialization using the context initialization
				// conclusion
				LinkedContextInitRule nextGlobalRule = changedGlobalRuleHead_;
				while (nextGlobalRule != null) {
					if (LOGGER_.isTraceEnabled())
						LOGGER_.trace(context + ": applying rule "
								+ nextGlobalRule.getName());
					nextGlobalRule.accept(ruleAppVisitor, null, context,
							saturationStateWriter);
					nextGlobalRule = nextGlobalRule.next();
				}
				// apply all changed composition rules for composed subsumers
				Set<IndexedClassExpression> composedSubsumers = context
						.getComposedSubsumers();
				if (composedSubsumers.size() > changedComposedSubsumers_.length >> 2) {
					// iterate over changes, check subsumers
					for (int j = 0; j < changedComposedSubsumers_.length; j++) {
						IndexedClassExpression changedICE = changedComposedSubsumers_[j];
						if (composedSubsumers.contains(changedICE)) {
							applyCompositionRules(context, changedICE);
						}
					}
				} else {
					// iterate over subsumers, check changes
					for (IndexedClassExpression changedICE : composedSubsumers) {
						applyCompositionRules(context, changedICE);
					}
				}
				// apply all definition expansion rules for decomposed subsumers
				Set<IndexedClassExpression> decomposedSubsumers = context
						.getDecomposedSubsumers();
				if (decomposedSubsumers.size() > changedDecomposedSubsumers_.length >> 2) {
					// iterate over changes, check subsumers
					for (int j = 0; j < changedDecomposedSubsumers_.length; j++) {
						IndexedClass changedIC = changedDecomposedSubsumers_[j];
						if (decomposedSubsumers.contains(changedIC)) {
							applyDecompositionRules(context, changedIC);
						}
					}
				} else {
					// iterate over subsumers, check changes
					for (IndexedClassExpression changedICE : decomposedSubsumers) {
						if (changedICE instanceof IndexedClass)
							applyDecompositionRules(context,
									(IndexedClass) changedICE);
					}
				}
			}

			@Override
			public void finish() {
				stageStatistics_.add(localStatistics);
				compositionRuleHits_.addAndGet(localCompsitionRuleHits_);
				decompositionRuleHits_.addAndGet(localDecompositionRuleHits_);
			}

			private void applyCompositionRules(Context context,
					IndexedClassExpression changedICE) {
				LinkedSubsumerRule nextLocalRule = changedCompositionRules_
						.get(changedICE);
				if (nextLocalRule != null) {
					localCompsitionRuleHits_++;

					LOGGER_.trace("{}: applying composition rules for {}",
							context, changedICE);
				}
				while (nextLocalRule != null) {
					nextLocalRule.accept(ruleAppVisitor, changedICE, context,
							saturationStateWriter);
					nextLocalRule = nextLocalRule.next();
				}
			}

			private void applyDecompositionRules(Context context,
					IndexedClass changedICE) {
				localDecompositionRuleHits_++;
				LOGGER_.trace("{}: applying decomposition rules for {}",
						context, changedICE);
				classDecomposition_.accept(ruleAppVisitor, changedICE, context,
						saturationStateWriter);
			}

		};
	}

	private InputProcessor<ArrayList<Context>> getEngine(
			final ContextProcessor baseProcessor) {
		if (SaturationUtils.COLLECT_PROCESSING_TIMES) {
			return new TimedContextCollectionProcessor(baseProcessor,
					stageStatistics_.getIncrementalProcessingStatistics());
		}
		// else
		return new ContextCollectionProcessor(baseProcessor);

	}

	@Override
	public void finish() {
		if (LOGGER_.isDebugEnabled()) {
			LOGGER_.debug("Composition rule hits: "
					+ compositionRuleHits_.get());
			LOGGER_.debug("Decomposition rule hits: "
					+ decompositionRuleHits_.get());
		}
	}

	/**
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	private static class ContextCollectionProcessor extends
			BaseInputProcessor<ArrayList<Context>> {

		private final ContextProcessor contextProcessor_;

		ContextCollectionProcessor(ContextProcessor contextProcessor) {
			contextProcessor_ = contextProcessor;
		}

		@Override
		protected void process(ArrayList<Context> contexts) {
			for (Context context : contexts) {
				contextProcessor_.process(context);
			}
		}

		@Override
		public void finish() {
			super.finish();
			contextProcessor_.finish();
		}

	}

	/**
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	static interface ContextProcessor {

		public void process(Context context);

		public void finish();
	}

	/**
	 * Measures time it takes to init changes for a context
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	static class TimedContextProcessor implements ContextProcessor {

		private final IncrementalProcessingStatistics localStats_;

		private final ContextProcessor processor_;

		TimedContextProcessor(ContextProcessor p,
				IncrementalProcessingStatistics localStats) {
			processor_ = p;
			localStats_ = localStats;
			localStats_.startMeasurements();
		}

		@Override
		public void process(Context context) {
			long ts = CachedTimeThread.getCurrentTimeMillis();

			processor_.process(context);

			localStats_.changeInitContextProcessingTime += (CachedTimeThread
					.getCurrentTimeMillis() - ts);
		}

		@Override
		public void finish() {
			processor_.finish();
			// no need to add the local stats to the stage-level stats, as it's
			// done by the caller
		}
	}

}
