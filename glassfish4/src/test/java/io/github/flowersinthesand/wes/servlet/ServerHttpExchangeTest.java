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
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ServerHttpExchangeTest extends ServerHttpExchangeTestTemplate {

	static AtomicReference<Performer> performerRef = new AtomicReference<>();
	static GlassFishRuntime runtime;
	GlassFish glassfish;
	
	@BeforeClass
	public static void setup() throws GlassFishException {
		// Due to GlassFishException: Already bootstrapped
		runtime = GlassFishRuntime.bootstrap();
	}

	@Override
	protected void startServer() throws Exception {
		GlassFishProperties props = new GlassFishProperties();
		props.setPort("http-listener", port);

		glassfish = runtime.newGlassFish(props);
		glassfish.start();
		
		Deployer deployer = glassfish.getDeployer();
		ScatteredArchive archive = new ScatteredArchive("testapp", ScatteredArchive.Type.WAR);
		archive.addClassPath(new File("target", "test-classes"));
		deployer.deploy(archive.toURI(), "--contextroot=/");
		
		performerRef.set(performer);
	}

	@Override
	protected void stopServer() throws Exception {
		glassfish.dispose();
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

	@WebListener
	public static class GlassfishServletContextListener implements ServletContextListener {

		@Override
		public void contextInitialized(ServletContextEvent event) {
			new ServletBridge(event.getServletContext(), "/test").httpAction(new Action<ServerHttpExchange>() {
				@Override
				public void on(ServerHttpExchange http) {
					Performer performer = performerRef.getAndSet(null);
					performer.serverAction().on(http);
				}
			});
		}

		@Override
		public void contextDestroyed(ServletContextEvent event) {}
		
	}
	
	@Override
	@Test
	@Ignore
	public void closeAction_by_client() {}

}
