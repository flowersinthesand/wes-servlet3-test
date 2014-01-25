/*
 * Copyright 2013-2014 Donghwan Kim
 *
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
 */
package io.github.flowersinthesand.wes.servlet;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import io.github.flowersinthesand.wes.Action;
import io.github.flowersinthesand.wes.ServerHttpExchange;
import io.github.flowersinthesand.wes.test.ServerHttpExchangeTestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.Ignore;
import org.junit.Test;

public class ServerHttpExchangeTest extends ServerHttpExchangeTestTemplate {

	Tomcat tomcat;

	@Override
	protected void startServer() throws Exception {
		tomcat = new Tomcat();
		tomcat.setPort(port);
		Context context = tomcat.addWebapp("/", new File("src/test").getAbsolutePath());
		context.addServletContainerInitializer(new ServletContainerInitializer() {
			@Override
			public void onStartup(Set<Class<?>> c, ServletContext ctx) {
				new ServletBridge(ctx, "/test").httpAction(new Action<ServerHttpExchange>() {
					@Override
					public void on(ServerHttpExchange http) {
						performer.serverAction().on(http);
					}
				});
			}
		}, Collections.<Class<?>> emptySet());
		tomcat.start();
	}

	@Test
	public void unwrap() {
		performer.serverAction(new Action<ServerHttpExchange>() {
			@Override
			public void on(ServerHttpExchange http) {
				assertThat(http.unwrap(HttpServletRequest.class), instanceOf(HttpServletRequest.class));
				assertThat(http.unwrap(HttpServletResponse.class), instanceOf(HttpServletResponse.class));
				performer.start();
			}
		})
		.send();
	}

	@Override
	protected void stopServer() throws Exception {
		tomcat.getServer().stop();
	}

	
	@Override
	@Test
	@Ignore
	public void closeAction_by_client() {}

}
