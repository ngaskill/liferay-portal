/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import React, {useState, useEffect, useRef} from 'react';
import PropTypes from 'prop-types';
import ClayModal, {useModal} from '@clayui/modal';
import ClayButton from '@clayui/button';
import {SplitPicker} from './SplitPicker/SplitPicker.es';
import {SliderWithLabel} from './SliderWithLabel.es';
import {SegmentsVariantType} from '../types.es';
import {percentageNumberToIndex} from '../util/percentages.es';
import BusyButton from './BusyButton/BusyButton.es';

function ReviewExperimentModal({onRun, variants, visible, setVisible}) {
	const [busy, setBusy] = useState(false);
	const [confidenceLevel, setConfidenceLevel] = useState(50);
	const [draftVariants, setDraftVariants] = useState(
		variants.map(variant => {
			return {...variant, split: parseInt(100 / variants.length, 10)};
		})
	);

	const {observer, onClose} = useModal({
		onClose: () => setVisible(false)
	});

	const mounted = useRef();

	useEffect(() => {
		mounted.current = true;
		return () => {
			mounted.current = false;
		};
	});

	return (
		visible && (
			<ClayModal observer={observer} size="lg">
				<ClayModal.Header>
					{Liferay.Language.get('review-and-run-test')}
				</ClayModal.Header>
				<ClayModal.Body>
					<h3 className="sheet-subtitle border-bottom-0 text-secondary">
						{Liferay.Language.get('traffic-split')}
					</h3>

					<SplitPicker
						onChange={variants => {
							setDraftVariants(variants);
						}}
						variants={draftVariants}
					/>

					<hr />

					<h3 className="sheet-subtitle border-bottom-0 text-secondary">
						{Liferay.Language.get('confidence-level')}
					</h3>

					<SliderWithLabel
						label={Liferay.Language.get(
							'confidence-level-required'
						)}
						onValueChange={setConfidenceLevel}
						value={confidenceLevel}
					/>
				</ClayModal.Body>

				<ClayModal.Footer
					last={
						<ClayButton.Group spaced>
							<ClayButton
								displayType="secondary"
								onClick={onClose}
							>
								{Liferay.Language.get('Cancel')}
							</ClayButton>

							<BusyButton
								busy={busy}
								disabled={busy}
								onClick={_handleRun}
							>
								{Liferay.Language.get('Run')}
							</BusyButton>
						</ClayButton.Group>
					}
				/>
			</ClayModal>
		)
	);

	/**
	 * Creates a splitVariantsMap `{ [segmentsExperimentRelId]: splitValue, ... }`
	 * and bubbles up `onRun` with `confidenceLevel` and `splitVariantsMap`
	 */
	function _handleRun() {
		const splitVariantsMap = draftVariants.reduce((acc, v) => {
			return {
				...acc,
				[v.segmentsExperimentRelId]: percentageNumberToIndex(v.split)
			};
		}, {});

		setBusy(true);

		onRun({
			confidenceLevel: percentageNumberToIndex(confidenceLevel),
			splitVariantsMap
		}).then(() => {
			if (mounted.current) {
				setBusy(false);
				setVisible(false);
			}
		});
	}
}

ReviewExperimentModal.propTypes = {
	onRun: PropTypes.func.isRequired,
	setVisible: PropTypes.func.isRequired,
	variants: PropTypes.arrayOf(SegmentsVariantType),
	visible: PropTypes.bool.isRequired
};

export {ReviewExperimentModal};
