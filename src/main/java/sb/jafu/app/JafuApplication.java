package sb.jafu.app;

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.MessageSourceInitializer;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.autoconfigure.jackson.JacksonInitializer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.JafuWebMvcRegistrations;
import org.springframework.context.support.ServletWebServerApplicationContextWithoutSpel;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctionDsl;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import sb.jafu.app.handler.JafuApplicationRestHandler;
import sb.jafu.app.handler.error.CommonErrorResponse;
import sb.jafu.app.handler.error.ErrorResultUtil;
import sb.jafu.app.handler.error.JafuResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RouterFunctions.route;

public class JafuApplication {

	private final ApplicationContextInitializer<GenericApplicationContext> initializer;

	private ServerProperties serverProperties = new ServerProperties();

	private HttpProperties httpProperties = new HttpProperties();

	private WebMvcProperties webMvcProperties = new WebMvcProperties();

	private ResourceProperties resourceProperties = new ResourceProperties();

	protected JafuApplication() {
		this.initializer = context -> {
			new MessageSourceInitializer().initialize(context);
			context.registerBean(CommandLineRunner.class, () -> args -> System.out.println("jafu running!"));

			JafuApplicationRestHandler handler = new JafuApplicationRestHandler();
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(RouterFunctionDsl.class.getName(), context), RouterFunction.class
					, () ->	route()
							.onError((ex-> ex instanceof HttpMessageNotReadableException), (t, request) -> {
								HttpMessageNotReadableException ee = (HttpMessageNotReadableException) t;
								String instanceDetails = "http message not readable: " + ee.getMostSpecificCause().getMessage();
								String instanceDebugDetails = "-";
								CommonErrorResponse resp = ErrorResultUtil.logAndGetCommonErrorResponse(ee, instanceDetails, instanceDebugDetails);
								return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(APPLICATION_JSON).body(resp);
							})
							.GET("/text", accept(TEXT_PLAIN), handler::getTextResponse)
							.GET("/json", accept(APPLICATION_JSON), handler::getMessageJsonResponse)
                            .POST("/json", accept(APPLICATION_JSON).and(contentType(APPLICATION_JSON)), handler::postMessageJson)
							.build());

			serverProperties.setPort(8080);
			new StringConverterInitializer().initialize(context);
			new ResourceConverterInitializer().initialize(context);

			new JacksonInitializer(new JacksonProperties()).initialize(context);
			new JacksonJsonConverterInitializer().initialize(context);


			context.registerBean(WebMvcRegistrations.class, JafuWebMvcRegistrations::new);
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(JafuResponseEntityExceptionHandler.class.getName(), context)
					, JafuResponseEntityExceptionHandler.class, () -> new JafuResponseEntityExceptionHandler(context.getEnvironment()));
			webMvcProperties.setThrowExceptionIfNoHandlerFound(true);
			new ServletWebServerInitializer(serverProperties, httpProperties, webMvcProperties, resourceProperties).initialize(context);
		};
	}

	public ConfigurableApplicationContext run() {
		return run("", new String[0]);
	}

	public ConfigurableApplicationContext run(String profiles) {
		return run(profiles, new String[0]);
	}

	public ConfigurableApplicationContext run(String[] args) {
		return run("", args);
	}

	public ConfigurableApplicationContext run(String profiles, String[] args) {
		SpringApplication app = new SpringApplication(JafuApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
			}
		};
		initializeWebApplicationContext(app);
		if (!profiles.isEmpty()) {
			app.setAdditionalProfiles(Arrays.stream(profiles.split(",")).map(it -> it.trim()).toArray(String[]::new));
		}
		app.addInitializers(this.initializer);
		System.setProperty("spring.backgroundpreinitializer.ignore", "true");
		return app.run(args);
	}

	protected void initializeWebApplicationContext(SpringApplication app) {
		app.setWebApplicationType(WebApplicationType.SERVLET);
		app.setApplicationContextClass(ServletWebServerApplicationContextWithoutSpel.class);
	}

	public static void main(String[] args) throws InterruptedException {
		new JafuApplication().run(args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
