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

package com.liferay.portal.search.test.util.query;

import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.DeleteDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.internal.document.DocumentBuilderImpl;
import com.liferay.portal.search.query.MoreLikeThisQuery;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Wade Cao
 */
public abstract class BaseMoreLikeThisQueryTestCase
	extends BaseIndexingTestCase {

	@Test
	public void testLegacyMoreLikeThisWithFieldAndLikeText() throws Exception {
		addDocuments("java eclipse", "eclipse liferay", "java liferay eclipse");

		com.liferay.portal.kernel.search.generic.MoreLikeThisQuery
			moreLikeThisQuery =
				new com.liferay.portal.kernel.search.generic.MoreLikeThisQuery(
					getCompanyId());

		moreLikeThisQuery.addField(_FIELD_TITLE);
		moreLikeThisQuery.setLikeText("java");

		assertSearch(
			moreLikeThisQuery,
			Arrays.asList("java eclipse", "java liferay eclipse"));
	}

	@Test
	public void testMoreLikeThisWithDocumentIdentifier() throws Exception {
		DocumentBuilder documentBuilder = new DocumentBuilderImpl();

		documentBuilder.setValue(_FIELD_TITLE, "java");

		IndexDocumentRequest indexDocumentRequest = new IndexDocumentRequest(
			String.valueOf(getCompanyId()), "1", documentBuilder.build());

		indexDocumentRequest.setType("LiferayDocumentType");

		SearchEngineAdapter searchEngineAdapter = getSearchEngineAdapter();

		searchEngineAdapter.execute(indexDocumentRequest);

		addDocuments("java eclipse", "eclipse liferay", "java liferay eclipse");

		MoreLikeThisQuery.DocumentIdentifier documentIdentifier =
			queries.documentIdentifier(
				String.valueOf(getCompanyId()), "LiferayDocumentType", "1");

		MoreLikeThisQuery moreLikeThisQuery = queries.moreLikeThis(
			Collections.singleton(documentIdentifier));

		assertSearch(
			moreLikeThisQuery,
			Arrays.asList("java eclipse", "java liferay eclipse"));

		DeleteDocumentRequest deleteDocumentRequest = new DeleteDocumentRequest(
			String.valueOf(getCompanyId()), "LiferayDocumentType", "1");

		searchEngineAdapter.execute(deleteDocumentRequest);
	}

	@Test
	public void testMoreLikeThisWithFieldAndLikeText() throws Exception {
		addDocuments("java eclipse", "eclipse liferay", "java liferay eclipse");

		MoreLikeThisQuery moreLikeThisQuery = queries.moreLikeThis(
			Collections.singletonList(_FIELD_TITLE), "java");

		assertSearch(
			moreLikeThisQuery,
			Arrays.asList("java eclipse", "java liferay eclipse"));
	}

	@Test
	public void testMoreLikeThisWithMultipleFields() throws Exception {
		addDocuments("alpha charlie", "delta echo");

		addDocuments(
			value -> DocumentCreationHelpers.singleText(
				_FIELD_DESCRIPTION, value),
			Arrays.asList("bravo charlie"));

		String[] fields = {_FIELD_TITLE};

		MoreLikeThisQuery moreLikeThisQuery = queries.moreLikeThis(
			fields, "alpha", "bravo");

		moreLikeThisQuery.addField(_FIELD_DESCRIPTION);

		assertSearch(
			moreLikeThisQuery, Arrays.asList("alpha charlie", "bravo charlie"));
	}

	@Test
	public void testMoreLikeThisWithoutFields() throws Exception {
		addDocuments("java eclipse", "eclipse liferay", "java liferay eclipse");

		MoreLikeThisQuery moreLikeThisQuery = queries.moreLikeThis(
			Collections.emptyList(), "java");

		assertSearch(moreLikeThisQuery, Collections.emptyList());
	}

	protected void addDocuments(String... values) throws Exception {
		addDocuments(
			value -> DocumentCreationHelpers.singleText(_FIELD_TITLE, value),
			Arrays.asList(values));
	}

	protected void assertSearch(
		com.liferay.portal.kernel.search.generic.MoreLikeThisQuery
			legacyMoreLikeThisQuery,
		List<String> expectedValues) {

		legacyMoreLikeThisQuery.setMinDocFrequency(Integer.valueOf(1));
		legacyMoreLikeThisQuery.setMinTermFrequency(Integer.valueOf(1));

		assertSearch(legacyMoreLikeThisQuery, null, expectedValues);
	}

	protected void assertSearch(
		com.liferay.portal.kernel.search.generic.MoreLikeThisQuery
			legacyMoreLikeThisQuery,
		MoreLikeThisQuery moreLikeThisQuery, List<String> expectedValues) {

		assertSearch(
			indexingTestHelper -> {
				SearchSearchRequest searchSearchRequest =
					new SearchSearchRequest();

				searchSearchRequest.setIndexNames(
					String.valueOf(getCompanyId()));
				searchSearchRequest.setQuery(legacyMoreLikeThisQuery);
				searchSearchRequest.setQuery(moreLikeThisQuery);
				searchSearchRequest.setSize(30);

				SearchEngineAdapter searchEngineAdapter =
					getSearchEngineAdapter();

				SearchSearchResponse searchSearchResponse =
					searchEngineAdapter.execute(searchSearchRequest);

				SearchHits searchHits = searchSearchResponse.getSearchHits();

				Assert.assertEquals(
					"Total hits", expectedValues.size(),
					searchHits.getTotalHits());

				List<SearchHit> searchHitsList = searchHits.getSearchHits();

				Assert.assertEquals(
					"Retrieved hits", expectedValues.size(),
					searchHitsList.size());

				List<String> actualValues = new ArrayList<>();

				searchHitsList.forEach(
					searchHit -> {
						Document document = searchHit.getDocument();

						String titleValue = document.getString(_FIELD_TITLE);

						if (titleValue != null) {
							actualValues.add(titleValue);
						}

						String descriptionValue = document.getString(
							_FIELD_DESCRIPTION);

						if (descriptionValue != null) {
							actualValues.add(descriptionValue);
						}
					});

				Assert.assertEquals(
					"Retrieved hits ->" + actualValues,
					expectedValues.toString(), actualValues.toString());
			});
	}

	protected void assertSearch(
		MoreLikeThisQuery moreLikeThisQuery, List<String> expectedValues) {

		moreLikeThisQuery.setMinDocFrequency(Integer.valueOf(1));
		moreLikeThisQuery.setMinTermFrequency(Integer.valueOf(1));

		assertSearch(null, moreLikeThisQuery, expectedValues);
	}

	private static final String _FIELD_DESCRIPTION = "description";

	private static final String _FIELD_TITLE = "title";

}