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

package com.liferay.portal.remote.soap.example;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.osgi.service.component.annotations.Component;

/**
 * @author Carlos Sierra Andrés
 */
@Component(
	immediate = true, property = "jaxws=true", service = Calculator.class
)
@WebService
public class Calculator {

	@WebMethod
	public int divide(int a, int b) {
		return a / b;
	}

	@WebMethod
	public int multiply(int a, int b) {
		return a * b;
	}

	@WebMethod
	public int substract(int a, int b) {
		return a - b;
	}

	@WebMethod
	public int sum(int a, int b) {
		return a + b;
	}

}